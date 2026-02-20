package fr.lucwaw.utou.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.lucwaw.utou.data.repository.UserRepository

@HiltWorker
class SyncUsersWorker @AssistedInject constructor(
    private val repository: UserRepository,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.refreshUsers()
            Result.success()
        } catch (e: Exception) {
            Log.d("WORKER USERS", e.toString())

            Result.retry()
        }
    }

}