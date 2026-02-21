package fr.lucwaw.utou.data.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleOneTimeRegisterSync(userId: Long, userName: String) {
        val request = OneTimeWorkRequestBuilder<SyncRegisterWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).setInputData(
                workDataOf(
                    "USER_ID" to userId,
                    "USER_NAME" to userName
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "sync_register",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncUsersWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    "Type" to "Periodic")
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "users_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun scheduleOnetimeRefreshSync() {
        val request = OneTimeWorkRequestBuilder<SyncUsersWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    "Type" to "Refresh")
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
            "sync_register",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleUpdateToken() {
        val saveRequest =
            PeriodicWorkRequestBuilder<UpdateTokenWorker>(730, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "save_request", ExistingPeriodicWorkPolicy.UPDATE, saveRequest
        )
    }

}
