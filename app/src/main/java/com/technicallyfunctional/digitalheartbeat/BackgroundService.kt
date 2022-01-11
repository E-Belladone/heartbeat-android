package com.technicallyfunctional.digitalheartbeat

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class BackgroundService : BroadcastReceiver() {
    var screenOn = true

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            screenOn = when (intent.action)
            {
                ACTION_SCREEN_ON -> true
                ACTION_USER_PRESENT -> true
                ACTION_SCREEN_OFF -> false
                else -> screenOn
            }
            val screenStatus = if (screenOn) "Screen is on, continuing with ping" else "Screen is off, skipping ping"
            Log.v("ScreenStatus", screenStatus)
            if (screenOn)
                ping(context)
        }
    }

    var since: Instant? = null
    var lastUpdateTimestamp: Int = 0
    var lastLocation: Location? = null
    var lastBattery: String = ""

    var notification: Notification? = null
    var notificationChannel: NotificationChannel? = null
    var notificationManager: NotificationManager? = null

    override fun peekService(myContext: Context?, service: Intent?): IBinder {
        return super.peekService(myContext, service)
    }

    private fun ping(context: Context?) {
        Thread {
            try {
                val defaultSharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context)
                var latitude = 0.0;
                var longitude = 0.0;
                var altitude = 0.0;
                if (defaultSharedPreferences.getBoolean("sync_location", false)) {
                    var location: Location?;

                    val fusedLocationClient = context?.let {
                        LocationServices.getFusedLocationProviderClient(
                            it
                        )
                    }

                    if (context?.let {
                            ActivityCompat.checkSelfPermission(
                                it,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        } != PackageManager.PERMISSION_GRANTED && context?.let {
                            ActivityCompat.checkSelfPermission(
                                it,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        } != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Thread
                    }
                    if (fusedLocationClient != null) {
                        location = Tasks.await(fusedLocationClient.lastLocation)
                        if (location != null) {
                            if (location.latitude == 0.0 && location.longitude == 0.0 && location.altitude == 0.0) {
                                Log.i("BGService-Ping", "Getting location actively")
                                val tokenSource =
                                    com.google.android.gms.tasks.CancellationTokenSource()
                                location = Tasks.await(
                                    fusedLocationClient.getCurrentLocation(
                                        PRIORITY_BALANCED_POWER_ACCURACY,
                                        tokenSource.token
                                    )
                                )
                            }
                            latitude = location.latitude
                            longitude = location.longitude
                            altitude = location.altitude
                            lastLocation = location
                        }
                    }
                }
                val urlString = defaultSharedPreferences.getString("server_hostname", "")
                val portString = defaultSharedPreferences.getString("server_port", "6060")
                var query = "{\"device\": \"${
                    defaultSharedPreferences.getString(
                        "device_name",
                        Build.MODEL
                    )
                }\""
                if (defaultSharedPreferences.getBoolean("sync_location", false)) {
                    query += ", \"latitude\": ${latitude}, \"longitude\": ${longitude}, \"altitude\": \"${altitude}\""
                }
                if (defaultSharedPreferences.getBoolean("sync_battery", false)) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        val bm =
                            context?.getSystemService(BATTERY_SERVICE) as BatteryManager
                        // Get the battery percentage and store it in a INT variable
                        val batLevel: Int =
                            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (bm.isCharging) {
                                query += ", \"battery\": \"C${batLevel}\""
                                lastBattery = "C${batLevel}"
                            }
                            else {
                                query += ", \"battery\": \"D${batLevel}\""
                                lastBattery = "D${batLevel}"
                            }
                        } else {
                            query += ", \"battery\": \"${batLevel}\""
                            lastBattery = "$batLevel"
                        }
                    }
                }
                query += "}"
                Log.i("BGService-Ping", "Data collected")
                Log.i("BGService-Ping", query)
                val url = URL("${urlString}:${portString}/api/beat")
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doInput = true
                connection.doOutput = true
                connection.setRequestProperty(
                    "Auth",
                    defaultSharedPreferences.getString("server_authentication_code", "")
                )
                connection.setRequestProperty(
                    "Device",
                    defaultSharedPreferences.getString("device_name", Build.MODEL)
                )
                connection.connect()
                Log.i("BGService-Ping", "Connected")
                val writer: Writer = OutputStreamWriter(connection.outputStream)
                Log.i("BGService-Ping", "Writing")
                //writer.writeBytes(query)
                connection.outputStream.write(query.toByteArray())
                Log.i("BGService-Ping", "Flushing")
                connection.outputStream.flush()
                //writer.flush()
                Log.i("BGService-Ping", "Data written")
                //writer.flush()
                val reader: Reader = InputStreamReader(connection.inputStream)
                val response = reader.readText()
                Log.i("BGService-Response", response)
                connection.disconnect()
                Log.i("BGService-Ping", "Ping SUCCESS!!")
                lastUpdateTimestamp = response.trim().toInt()
                val updateIntent: Intent = Intent(StatusFragment.ACTION_UPDATE)
                val bundle = Bundle()


                var status = "Running"
                var subStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    "As of ${since.toString()}"
                } else {
                    "Instant.now() not supported"
                }
                var statusDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    "Last battery: $lastBattery\n" +
                            "Last location: (lat ${lastLocation?.latitude} lon ${lastLocation?.longitude})\n" +
                            "Last ping: ${Instant.ofEpochSecond(lastUpdateTimestamp.toLong())}"
                }
                else {
                    "Last battery: $lastBattery\n" +
                            "Last location: (lat ${lastLocation?.latitude} lon ${lastLocation?.longitude})\n" +
                            "Last ping: $lastUpdateTimestamp"
                }
                bundle.putString("status", status)
                bundle.putString("substatus", subStatus)
                bundle.putString("statusdetails", statusDetails)
                updateIntent.putExtras(bundle)
                context!!.sendBroadcast(updateIntent)

            } catch (e: Exception) {
                Log.e("BGService-Ping", e.toString())
                Log.e("BGService-Ping", e.stackTraceToString())
            }
        }.start()
    }

}