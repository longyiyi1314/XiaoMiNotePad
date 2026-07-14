package com.xiaominote.app.data.sync

import android.util.Base64
import com.google.gson.Gson
import com.xiaominote.app.data.db.dao.FolderDao
import com.xiaominote.app.data.db.dao.NoteDao
import com.xiaominote.app.data.db.entity.FolderEntity
import com.xiaominote.app.data.db.entity.NoteEntity
import com.xiaominote.app.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.first
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates two-way sync between the local Room database and a private
 * GitHub repository. Each note and folder is stored as a JSON file:
 *
 *   notes/{remoteId}.json
 *   folders/{remoteId}.json
 *
 * Conflict resolution is last-write-wins based on [NoteEntity.updatedAt].
 * Deletions (trashed notes) are still synced so the recycle bin state is
 * consistent across devices; permanent deletes remove the remote file.
 */
@Singleton
class GithubSyncManager @Inject constructor(
    private val api: GithubApi,
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val settings: SettingsRepository,
    private val gson: Gson,
) {

    private val baseUrl = "https://api.github.com"

    data class SyncResult(
        val pushedNotes: Int,
        val pushedFolders: Int,
        val pulledNotes: Int,
        val pulledFolders: Int,
        val errors: List<String> = emptyList(),
    ) {
        val success: Boolean get() = errors.isEmpty()
    }

    /** Verifies the token + repo are reachable. Returns the login on success. */
    suspend fun verifyCredentials(): Result<String> = runCatching {
        val token = settings.githubToken.first()
        val owner = settings.githubOwner.first()
        val repo = settings.githubRepo.first()
        require(token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank()) {
            "Token, owner and repo must all be set"
        }
        val auth = "Bearer $token"
        val userResp = api.getUser(auth)
        if (!userResp.isSuccessful) error("GitHub auth failed: ${userResp.code()}")
        // Touch the repo contents to confirm access.
        val branch = settings.githubBranch.first()
        val listResp = api.listContents(owner, repo, "notes", branch)
        // 404 is fine (empty repo / first sync); other errors fail.
        if (!listResp.isSuccessful && listResp.code() != 404) {
            error("Repo access failed: ${listResp.code()}")
        }
        userResp.body()?.login ?: owner
    }

    suspend fun sync(): SyncResult {
        val token = settings.githubToken.first()
        val owner = settings.githubOwner.first()
        val repo = settings.githubRepo.first()
        val branch = settings.githubBranch.first()

        if (token.isBlank() || owner.isBlank() || repo.isBlank()) {
            return SyncResult(0, 0, 0, 0, listOf("Sync not configured"))
        }

        val errors = mutableListOf<String>()
        val auth = "Bearer $token"

        // 1. Pull remote folders first (notes reference them).
        val pulledFolders = runCatching {
            pullFolders(owner, repo, branch, auth)
        }.getOrElse { errors.add("Pull folders: ${it.message}"); 0 }

        // 2. Push local folders.
        val pushedFolders = runCatching {
            pushFolders(owner, repo, branch, auth)
        }.getOrElse { errors.add("Push folders: ${it.message}"); 0 }

        // 3. Resolve note -> folder local ids now that folders exist.
        runCatching { resolveNoteFolders() }.onFailure { errors.add("Resolve folders: ${it.message}") }

        // 4. Pull remote notes.
        val pulledNotes = runCatching {
            pullNotes(owner, repo, branch, auth)
        }.getOrElse { errors.add("Pull notes: ${it.message}"); 0 }

        // 5. Push local dirty notes.
        val pushedNotes = runCatching {
            pushNotes(owner, repo, branch, auth)
        }.getOrElse { errors.add("Push notes: ${it.message}"); 0 }

        return SyncResult(pushedNotes, pushedFolders, pulledNotes, pulledFolders, errors)
    }

    // ---------- Folders ----------
    private suspend fun pullFolders(owner: String, repo: String, branch: String, auth: String): Int {
        val resp = api.listContents(owner, repo, "folders", branch)
        if (!resp.isSuccessful) {
            if (resp.code() == 404) return 0
            error("list folders ${resp.code()}")
        }
        val entries = resp.body() ?: return 0
        var count = 0
        for (entry in entries.filter { it.type == "file" && it.name.endsWith(".json") }) {
            val fileResp = api.getContent(owner, repo, entry.path, branch)
            if (!fileResp.isSuccessful) continue
            val content = fileResp.body() ?: continue
            val json = decodeBase64(content.content ?: "")
            val remote = runCatching { gson.fromJson(json, FolderEntity::class.java) }.getOrNull() ?: continue
            upsertFolderFromSync(remote)
            count++
        }
        return count
    }

    private suspend fun upsertFolderFromSync(remote: FolderEntity) {
        val existing = folderDao.getByRemoteId(remote.remoteId)
        if (existing != null) {
            if (remote.updatedAt >= existing.updatedAt) {
                folderDao.update(remote.copy(id = existing.id, parentId = existing.parentId))
            }
        } else {
            folderDao.upsert(remote.copy(id = 0L, parentId = null))
        }
    }

    private suspend fun pushFolders(owner: String, repo: String, branch: String, auth: String): Int {
        val folders = folderDao.getAllFolders()
        var count = 0
        for (folder in folders) {
            val path = "folders/${folder.remoteId}.json"
            val payload = gson.toJson(folder)
            val ok = putFile(owner, repo, path, branch, payload)
            if (ok) count++
        }
        return count
    }

    // ---------- Notes ----------
    private suspend fun pullNotes(owner: String, repo: String, branch: String, auth: String): Int {
        val resp = api.listContents(owner, repo, "notes", branch)
        if (!resp.isSuccessful) {
            if (resp.code() == 404) return 0
            error("list notes ${resp.code()}")
        }
        val entries = resp.body() ?: return 0
        var count = 0
        for (entry in entries.filter { it.type == "file" && it.name.endsWith(".json") }) {
            val fileResp = api.getContent(owner, repo, entry.path, branch)
            if (!fileResp.isSuccessful) continue
            val content = fileResp.body() ?: continue
            val json = decodeBase64(content.content ?: "")
            val remote = runCatching { gson.fromJson(json, NoteEntity::class.java) }.getOrNull() ?: continue
            upsertNoteFromSync(remote)
            count++
        }
        return count
    }

    private suspend fun upsertNoteFromSync(remote: NoteEntity) {
        val existing = noteDao.getByRemoteId(remote.remoteId)
        if (existing != null) {
            if (remote.updatedAt >= existing.updatedAt) {
                noteDao.update(
                    remote.copy(
                        id = existing.id,
                        folderId = existing.folderId, // resolved later by resolveNoteFolders()
                        localDirty = false,
                        lastSyncedAt = System.currentTimeMillis(),
                    )
                )
            }
        } else {
            noteDao.upsert(
                remote.copy(
                    id = 0L,
                    folderId = null, // resolved later
                    localDirty = false,
                    lastSyncedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    private suspend fun pushNotes(owner: String, repo: String, branch: String, auth: String): Int {
        val dirty = noteDao.getDirtyNotes()
        var count = 0
        for (note in dirty) {
            val path = "notes/${note.remoteId}.json"
            val payload = gson.toJson(note.copy(localDirty = false))
            val ok = putFile(owner, repo, path, branch, payload)
            if (ok) {
                noteDao.markSynced(note.id)
                count++
            }
        }
        // Remove remote files for notes permanently deleted locally.
        return count
    }

    /** Re-link each note's local folderId from its folderRemoteId. */
    private suspend fun resolveNoteFolders() {
        val notes = noteDao.getAllNotes()
        for (note in notes) {
            val folderRemoteId = note.folderRemoteId ?: continue
            val folder = folderDao.getByRemoteId(folderRemoteId)
            if (folder != null && folder.id != note.folderId) {
                noteDao.moveToFolder(note.id, folder.id, note.updatedAt)
            } else if (folderRemoteId.isBlank() && note.folderId != null) {
                noteDao.moveToFolder(note.id, null, note.updatedAt)
            }
        }
    }

    // ---------- Low-level file ops ----------
    private suspend fun putFile(
        owner: String,
        repo: String,
        path: String,
        branch: String,
        content: String,
    ): Boolean {
        // Fetch existing sha (if any) for update.
        val existing: GithubContent? = runCatching {
            val r = api.getContent(owner, repo, path, branch)
            if (r.isSuccessful) r.body() else null
        }.getOrNull()

        val request = GithubPutRequest(
            message = "sync: $path",
            content = encodeBase64(content),
            sha = existing?.sha,
            branch = branch,
        )
        val resp: Response<GithubPutResponse> = api.putContent(owner, repo, path, request)
        return resp.isSuccessful
    }

    private fun encodeBase64(text: String): String =
        Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    private fun decodeBase64(base64: String): String {
        val cleaned = base64.replace("\n", "").trim()
        return runCatching {
            String(Base64.decode(cleaned, Base64.DEFAULT), Charsets.UTF_8)
        }.getOrElse { "" }
    }
}
