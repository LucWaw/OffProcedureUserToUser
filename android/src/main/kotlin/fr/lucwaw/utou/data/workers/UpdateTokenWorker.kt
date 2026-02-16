package fr.lucwaw.utou.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import fr.lucwaw.utou.data.repository.UserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UpdateTokenWorker @Inject constructor(
    private val repository: UserRepository,
    appContext: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val token = Firebase.messaging.token.await()
            repository.registerDevice(token)

            Result.success()
        } catch (e: Exception){
            Log.d("WORKER TOKEN", e.toString())

            Result.retry()
        }
    }
}