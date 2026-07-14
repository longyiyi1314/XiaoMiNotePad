package com.xiaominote.app.data.repository

import com.xiaominote.app.data.db.dao.FolderDao
import com.xiaominote.app.data.db.entity.FolderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
) {
    fun observeByParent(parentId: Long?): Flow<List<FolderEntity>> =
        folderDao.observeByParent(parentId)

    fun observeAll(): Flow<List<FolderEntity>> = folderDao.observeAll()

    suspend fun getById(id: Long): FolderEntity? = folderDao.getById(id)

    suspend fun createFolder(name: String, color: Long, parentId: Long? = null): Long {
        val now = System.currentTimeMillis()
        return folderDao.upsert(
            FolderEntity(name = name, color = color, parentId = parentId, createdAt = now, updatedAt = now)
        )
    }

    suspend fun rename(id: Long, name: String) {
        val folder = folderDao.getById(id) ?: return
        folderDao.update(folder.copy(name = name, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateColor(id: Long, color: Long) {
        val folder = folderDao.getById(id) ?: return
        folderDao.update(folder.copy(color = color, updatedAt = System.currentTimeMillis()))
    }

    /** Deletes the folder and moves its notes to the root (folderId = null). */
    suspend fun delete(id: Long) {
        folderDao.detachNotes(id)
        folderDao.delete(id)
    }
}
