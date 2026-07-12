package com.example.skim.data.repository

import com.example.skim.data.local.RecordingCacheDao
import com.example.skim.data.local.RecordingCacheEntity
import com.example.skim.model.Recording
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SkimLocalDataSource {
    fun observe(): Flow<List<Recording>>
    suspend fun replace(recordings: List<Recording>)
}

class RoomSkimLocalDataSource(
    private val dao: RecordingCacheDao,
    private val gson: Gson = Gson(),
) : SkimLocalDataSource {
    override fun observe(): Flow<List<Recording>> = dao.observe().map { values ->
        values.map { value -> gson.fromJson<Recording>(value.payload, recordingType) }
    }

    override suspend fun replace(recordings: List<Recording>) {
        dao.replace(recordings.map { recording -> RecordingCacheEntity(recording.id, gson.toJson(recording), recording.createdAt) })
    }

    private companion object {
        val recordingType = object : TypeToken<Recording>() {}.type
    }
}
