package fr.lucwaw.utou.data

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import fr.lucwaw.utou.data.NotificationService
import fr.lucwaw.utou.data.repository.UserRepository
import fr.lucwaw.utou.di.UserRepositoryEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FMService: FirebaseMessagingService() {
    private val userRepository: UserRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            UserRepositoryEntryPoint::class.java
        ).userRepository()
    }

    private lateinit var notificationService: NotificationService

    override fun onCreate() {
        super.onCreate()
        notificationService = NotificationService(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseMessagingService", "Refreshed token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = userRepository.generatedUserId
                if (userId.isNotBlank()) {
                    userRepository.registerDevice(token)
                    Log.d("FCM", "Token envoyé au backend")
                } else {
                    userRepository.lastTokenGenerated = token
                }
            } catch (e: Exception) {
                Log.e("FCM", "Impossible d'envoyer le token", e)
            }
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FirebaseMessagingService", "From: ${message.notification?.title}, channel: ${message.notification?.channelId}")

        val notification = message.notification
        if (notification != null) {
            notificationService.sendNotification(notification.title, notification.body)
        }
    }
}