package com.technicallyfunctional.digitalheartbeat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.net.HttpURLConnection
import java.net.URL

class BackgroundService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            ping(context)
        }
    }

    fun ping(context: Context?)
    {
        Thread {
            try {
                var locX = 0;
                var locY = 0;
                val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val urlString = defaultSharedPreferences.getString("server_hostname", "")
                val portString = defaultSharedPreferences.getString("server_port", "6060")
                var query = "{\"device\": \"${defaultSharedPreferences.getString("device_name", Build.MODEL)}\""
                if (defaultSharedPreferences.getBoolean("sync_location", false))
                {
                    // TODO: get location
                    query += ",\"locationX\": \"${locX}\", \"locationY\": \"${locY}\""
                }
                if (defaultSharedPreferences.getBoolean("sync_battery", false))
                {
                    TODO("get battery level")
                }
                query += "}"
                Log.i("BGService-Ping", "Data collected")
                val url = URL("${urlString}:${portString}/api/beat")
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doInput = true
                connection.doOutput = true
                connection.setRequestProperty("Auth", defaultSharedPreferences.getString("server_authentication_code", ""))
                connection.setRequestProperty("Device", defaultSharedPreferences.getString("device_name", Build.MODEL))
                connection.connect()
                Log.i("BGService-Ping", "Connected")
                val writer: Writer = OutputStreamWriter(connection.outputStream)
                val reader: Reader = InputStreamReader(connection.inputStream)
                writer.write(query)
                Log.i("BGService-Ping", "Data written")
                writer.flush()
                Log.i("BGService-Response", reader.readText())
                writer.close()
                Log.i("BGService-Ping", "Ping SUCCESS!!")
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                Log.e("BGService-Ping", e.toString())
            }
        }.start()
    }
}