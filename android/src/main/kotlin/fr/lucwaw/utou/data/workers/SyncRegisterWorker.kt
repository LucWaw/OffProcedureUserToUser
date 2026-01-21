package fr.lucwaw.utou.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.lucwaw.utou.data.repository.UserRepository
import javax.inject.Inject

class SyncRegisterWorker @Inject constructor(
    private val repository: UserRepository,
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userNameInput =
                inputData.getString("USER_NAME") ?: return Result.failure()
            repository.syncRegisteredUser(userNameInput)
            Result.success()
        } catch (_: Exception){
            Result.retry()
        }
    }
}