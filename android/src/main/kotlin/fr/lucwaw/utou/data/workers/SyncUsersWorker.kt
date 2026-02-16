package fr.lucwaw.utou.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.lucwaw.utou.data.repository.UserRepository
import javax.inject.Inject

class SyncUsersWorker @Inject constructor(
    private val repository: UserRepository,
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.refreshUsers()
            Result.success()
        } catch (e: Exception){
            Log.d("WORKER USERS", e.toString())

            Result.retry()
        }
    }
}