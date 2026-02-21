package fr.lucwaw.utou.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.lucwaw.utou.data.repository.UserRepository
import kotlinx.coroutines.tasks.await

@HiltWorker
class UpdateTokenWorker @AssistedInject constructor(
    private val repository: UserRepository,
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("WORKER TOKEN", "Starting")

            val token = Firebase.messaging.token.await()
            repository.registerDevice(token)

            Result.success(
                workDataOf(Pair("Token", "periodic"))
            )
        } catch (e: Exception){
            Log.d("WORKER TOKEN", e.toString())

            Result.retry()
        }
    }
}