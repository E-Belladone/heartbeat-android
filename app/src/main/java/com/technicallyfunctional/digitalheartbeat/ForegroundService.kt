package com.technicallyfunctional.digitalheartbeat

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import androidx.preference.PreferenceManager
import java.io.FileDescriptor
import java.time.Instant

class ForegroundService: Service() {
    class TRANSACTION
    {
        companion object {
            val GET_STATUS: Int = 1
            val GET_LAST_PING: Int = 2
            val GET_LAST_LOCATION: Int = 3
            val GET_LAST_BATTERY: Int = 4
            val GET_LAST_ERROR: Int = 5
        }
    }

    var running: Boolean = false
    var since: Instant? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Instant.now()
    } else {
        null
    }

    private val bgService = BackgroundService()

    var lastLocation: Location? = null
    var lastError: String = ""

    var context: Context? = null

    private var notificationChannel: NotificationChannel? = null
    private var notification: Notification? = null
    private var notificationManager: NotificationManager? = null

    private var defaultSharedPreferences: SharedPreferences? = null

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        context = baseContext
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (notificationChannel == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
        if (notification == null)
            createNotification()
        startForeground(1, notification)

        context?.registerReceiver(bgService, IntentFilter(Intent.ACTION_TIME_TICK))
        context?.registerReceiver(bgService, IntentFilter(Intent.ACTION_SCREEN_ON))
        context?.registerReceiver(bgService, IntentFilter(Intent.ACTION_SCREEN_OFF))
        context?.registerReceiver(bgService, IntentFilter(Intent.ACTION_USER_PRESENT))
        running = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            since = Instant.now()
            bgService.since = since
        }
        return START_STICKY
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        val name = getString(R.string.fgservice_notification_channel_name)
        val descriptionText = getString(R.string.fgservice_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        notificationChannel = NotificationChannel("fgservice", name, importance)
        notificationChannel!!.description = descriptionText
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.createNotificationChannel(notificationChannel!!)
    }

    private fun createNotification() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(defaultSharedPreferences?.getString("server_hostname", "https://hb.l1v.in/"))
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(context, "fgservice")
                .setContentTitle(getText(R.string.fgservice_notification_title))
                .setContentText(getText(R.string.fgservice_notification_content_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            notification = Notification.Builder(context)
                .setContentTitle(getText(R.string.fgservice_notification_title))
                .setContentText(getText(R.string.fgservice_notification_content_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(bgService)
        stopSelf()
    }

    inner class Binder : IBinder {
        public fun getService() : ForegroundService {
            return this@ForegroundService
        }

        override fun getInterfaceDescriptor(): String {
            return "HeartbeatService"
        }

        override fun pingBinder(): Boolean {
            return this@ForegroundService.running
        }

        override fun isBinderAlive(): Boolean {
            return this@ForegroundService.running
        }

        override fun queryLocalInterface(descriptor: String): IInterface? {
            return null
        }

        override fun dump(fileDescriptor: FileDescriptor, args: Array<out String>?) {
            throw NotImplementedError()
        }

        override fun dumpAsync(fileDescriptor: FileDescriptor, args: Array<out String>?) {
            throw NotImplementedError()
        }

        override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            when (code)
            {
                TRANSACTION.GET_STATUS -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (running)
                            reply?.writeString("Running\nsince ${since.toString()}")
                        else
                            reply?.writeString("Stopped\nsince ${since.toString()}")
                    }
                    else {
                        if (running)
                            reply?.writeString("Running")
                        else
                            reply?.writeString("Stopped")
                    }
                    reply?.setDataPosition(0)
                    return true
                }

                TRANSACTION.GET_LAST_PING -> {
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //if (lastPing != null) {
                        //    val formatter: DateTimeFormatter =
                        //        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        //    reply?.writeString(formatter.format(lastPing))
                        //}
                        reply?.writeInt(bgService.lastUpdateTimestamp)
                    //} else {
                    //    reply?.writeString("Unknown (API Version too low)")
                    //}
                    reply?.setDataPosition(0)
                    return true
                }

                TRANSACTION.GET_LAST_BATTERY -> {
                    reply?.writeString(bgService.lastBattery)
                    reply?.setDataPosition(0)
                    return true

                }

                TRANSACTION.GET_LAST_LOCATION -> {
                    if (bgService.lastLocation != null)
                        reply?.writeString(bgService.lastLocation.toString())
                    else
                        reply?.writeString("Unknown!")
                    reply?.setDataPosition(0)
                    return true
                }

                TRANSACTION.GET_LAST_ERROR -> {
                    return if(lastError != "") {
                        reply?.writeString(lastError)
                        reply?.setDataPosition(0)
                        true
                    } else false
                }

                else ->  return false
            }
        }

        override fun linkToDeath(deathRecipient: IBinder.DeathRecipient, flags: Int) {
            TODO("Not yet implemented")
        }

        override fun unlinkToDeath(deathRecipient: IBinder.DeathRecipient, flags: Int): Boolean {
            TODO("Not yet implemented")
        }
    }
}