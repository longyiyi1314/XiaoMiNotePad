package com.xiaominote.app.importing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.xiaominote.app.data.db.entity.AttachmentEntity
import com.xiaominote.app.data.db.entity.AttachmentType
import com.xiaominote.app.data.repository.NoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Imports documents (PDF / PPT / Word / image) into a note.
 *
 * PDFs are rasterised page-by-page using the platform [PdfRenderer] so that
 * handwriting can be drawn over each page. PPT/Word files are stored as
 * attachments (opened with an external viewer) since Android has no built-in
 * renderer for those formats; a PDF workflow is recommended for annotation.
 */
@Singleton
class DocumentImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
) {

    suspend fun importToNote(noteId: Long, uri: Uri, mime: String?): ImportResult =
        withContext(Dispatchers.IO) {
            val type = resolveType(mime, uri)
            val displayName = queryDisplayName(uri) ?: "import_${System.currentTimeMillis()}"
            val dir = File(context.filesDir, "attachments/$noteId").apply { mkdirs() }

            when (type) {
                AttachmentType.PDF -> importPdf(noteId, uri, displayName, dir)
                AttachmentType.IMAGE -> importImage(noteId, uri, displayName, dir)
                else -> importOfficeFile(noteId, uri, displayName, type, dir)
            }
        }

    private suspend fun importPdf(
        noteId: Long,
        uri: Uri,
        displayName: String,
        dir: File,
    ): ImportResult {
        val pdfFile = File(dir, "$displayName.pdf").also { copy(uri, it) }
        val pagesDir = File(dir, "${displayName}_pages").apply { mkdirs() }

        var pageCount = 0
        val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(uri, "r")
        pfd?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                pageCount = renderer.pageCount
                for (i in 0 until pageCount) {
                    renderer.openPage(i).use { page ->
                        val scale = 2 // 2x for crispness
                        val width = page.width * scale
                        val height = page.height * scale
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        File(pagesDir, "page_${i + 1}.png").outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        bitmap.recycle()
                    }
                }
            }
        }

        val attachment = AttachmentEntity(
            noteId = noteId,
            type = AttachmentType.PDF,
            originalName = displayName,
            storedPath = "attachments/$noteId/${displayName}_pages",
            pageCount = pageCount,
        )
        val id = noteRepository.addAttachment(attachment)
        return ImportResult(id, pageCount, AttachmentType.PDF, pagesDir.absolutePath)
    }

    private suspend fun importImage(
        noteId: Long,
        uri: Uri,
        displayName: String,
        dir: File,
    ): ImportResult {
        val ext = if (displayName.endsWith(".png")) "png" else "jpg"
        val file = File(dir, "$displayName.$ext").also { copy(uri, it) }
        val attachment = AttachmentEntity(
            noteId = noteId,
            type = AttachmentType.IMAGE,
            originalName = displayName,
            storedPath = "attachments/$noteId/$displayName.$ext",
            pageCount = 1,
        )
        val id = noteRepository.addAttachment(attachment)
        return ImportResult(id, 1, AttachmentType.IMAGE, file.absolutePath)
    }

    private suspend fun importOfficeFile(
        noteId: Long,
        uri: Uri,
        displayName: String,
        type: AttachmentType,
        dir: File,
    ): ImportResult {
        val ext = when (type) {
            AttachmentType.PPT -> "pptx"
            AttachmentType.WORD -> "docx"
            else -> "bin"
        }
        val file = File(dir, "$displayName.$ext").also { copy(uri, it) }
        val attachment = AttachmentEntity(
            noteId = noteId,
            type = type,
            originalName = displayName,
            storedPath = "attachments/$noteId/$displayName.$ext",
            pageCount = 0,
        )
        val id = noteRepository.addAttachment(attachment)
        return ImportResult(id, 0, type, file.absolutePath)
    }

    private fun resolveType(mime: String?, uri: Uri): AttachmentType {
        val m = mime?.lowercase().orEmpty()
        return when {
            m.contains("pdf") -> AttachmentType.PDF
            m.contains("powerpoint") || m.contains("presentation") -> AttachmentType.PPT
            m.contains("word") || m.contains("wordprocessing") -> AttachmentType.WORD
            m.startsWith("image/") -> AttachmentType.IMAGE
            else -> guessFromName(uri)
        }
    }

    private fun guessFromName(uri: Uri): AttachmentType {
        val name = uri.lastPathSegment?.lowercase().orEmpty()
        return when {
            name.endsWith(".pdf") -> AttachmentType.PDF
            name.endsWith(".ppt") || name.endsWith(".pptx") -> AttachmentType.PPT
            name.endsWith(".doc") || name.endsWith(".docx") -> AttachmentType.WORD
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") -> AttachmentType.IMAGE
            else -> AttachmentType.OTHER
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
            }
        }.getOrNull()
    }

    private fun copy(src: Uri, dest: File) {
        context.contentResolver.openInputStream(src)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
    }

    data class ImportResult(
        val attachmentId: Long,
        val pageCount: Int,
        val type: AttachmentType,
        val firstPagePath: String,
    )
}
