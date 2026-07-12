package com.example.skim

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.skim.data.local.SkimDatabase
import com.example.skim.data.remote.SkimApi
import com.example.skim.data.repository.DefaultSkimRepository
import com.example.skim.data.repository.RetrofitSkimRemoteDataSource
import com.example.skim.data.repository.RoomSkimLocalDataSource
import com.example.skim.data.repository.SkimRepository
import com.example.skim.ui.main.SkimMainViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SkimAppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        SkimDatabase::class.java,
        "skim.db",
    ).addMigrations(SkimDatabase.migration1To2).build()

    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.SKIM_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SkimApi::class.java)

    val repository: SkimRepository = DefaultSkimRepository(
        remote = RetrofitSkimRemoteDataSource(api),
        local = RoomSkimLocalDataSource(database.recordingCacheDao()),
    )
}

class SkimViewModelFactory(
    private val repository: SkimRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SkimMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SkimMainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
