package com.technicallyfunctional.digitalheartbeat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager

class OnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null)
        {
            if (context != null)
            {
                val defaultSharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context)
                if (defaultSharedPreferences.getBoolean("start_on_boot", false)) {
                    val intent2 = Intent(context, ForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.applicationContext.startForegroundService(intent2)
                    else
                        context.applicationContext.startService(intent2)
                }
            }
        }
    }

}