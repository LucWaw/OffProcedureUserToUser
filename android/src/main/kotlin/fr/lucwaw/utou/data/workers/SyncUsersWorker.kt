package fr.lucwaw.utou.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
        val typeInput =
            inputData.getString("Type") ?: return Result.failure()

        return try {
            Log.d("SyncUsers", "Starting worker $inputData")
            repository.refreshUsers()
            Result.success(
                workDataOf(Pair("Sync", "$typeInput successfully sync"))
            )
        } catch (e: Exception) {
            Log.d("WORKER USERS", e.toString())

            Result.retry()
        }
    }

}