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
class PingUser @AssistedInject constructor(
    private val repository: UserRepository,
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userGUID =
                inputData.getString("USER_GUID") ?: return Result.failure()

            repository.sendPing(userGUID)
            Result.success()
        } catch (e: Exception){
            Log.d("WORKER REGISTER", e.toString())
            Result.retry()
        }
    }
}