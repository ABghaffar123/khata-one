package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.CaptureDao
import com.khatabook.app.data.entity.CaptureEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureRepository @Inject constructor(
    private val captureDao: CaptureDao
) {
    fun getAllCaptures(): Flow<List<CaptureEntity>> = captureDao.getAllCaptures()

    fun getCaptureById(id: Long): Flow<CaptureEntity?> = captureDao.getCaptureById(id)

    suspend fun getCaptureByIdOnce(id: Long): CaptureEntity? = captureDao.getCaptureByIdOnce(id)

    suspend fun insertCapture(capture: CaptureEntity): Long = captureDao.insertCapture(capture)

    suspend fun updateCapture(capture: CaptureEntity) = captureDao.updateCapture(capture)

    suspend fun deleteCapture(capture: CaptureEntity) = captureDao.deleteCapture(capture)
}
