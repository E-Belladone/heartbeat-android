package com.technicallyfunctional.digitalheartbeat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class OnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null)
        {
            if (context != null)
            {
                val intent2 = Intent(context, ForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.applicationContext.startForegroundService(intent2)
                else
                    context.applicationContext.startService(intent2)
            }
        }
    }

}