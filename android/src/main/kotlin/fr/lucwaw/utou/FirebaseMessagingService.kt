package fr.lucwaw.utou

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FMService: FirebaseMessagingService() {

    private lateinit var notificationService: NotificationService


    override fun onCreate() {
        super.onCreate()
        notificationService = NotificationService(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseMessagingService", "Refreshed token: $token")
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