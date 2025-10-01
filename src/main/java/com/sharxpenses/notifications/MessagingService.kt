package com.sharxpenses.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sharxpenses.R
import com.sharxpenses.data.remote.DevicesApi
import com.sharxpenses.data.remote.dto.DeviceTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    @Inject
    @Named("retrofitAuthed")
    lateinit var retrofitAuthed: Retrofit

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo token: ")
        // Tenta enviar ao backend (falhas são logadas; não quebram o app)
        ioScope.launch {
            try {
                val api = retrofitAuthed.create(DevicesApi::class.java)
                api.registerDevice(DeviceTokenRequest(token = token))
                Log.d("FCM", "Token enviado ao backend com sucesso")
            } catch (t: Throwable) {
                Log.w("FCM", "Falha ao enviar token ao backend: {t.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"] ?: "SharXpenses"
        val body = data["body"] ?: "Você tem uma atualização"
        val route = NotificationRouter.resolveRoute(data)

        val channelId = "sharxpenses_default"
        createChannelIfNeeded(channelId, "SharXpenses", "Notificações gerais")

        val pending = NotificationRouter.buildPendingIntent(this, route)
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    private fun createChannelIfNeeded(id: String, name: String, desc: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = desc
            }
            mgr.createNotificationChannel(channel)
        }
    }
}