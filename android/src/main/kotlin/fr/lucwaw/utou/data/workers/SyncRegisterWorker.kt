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
class SyncRegisterWorker @AssistedInject constructor(
    private val repository: UserRepository,
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userNameInput =
                inputData.getString("USER_NAME") ?: return Result.failure()
            val userIdInput =
                inputData.getLong("USER_ID", -2L)
            if(userIdInput == -2L){
                return Result.failure()
            }
            repository.syncRegisteredUser(userNameInput, userIdInput)
            Result.success()
        } catch (e: Exception){
            Log.d("WORKER REGISTER", e.toString())
            Result.retry()
        }
    }
}