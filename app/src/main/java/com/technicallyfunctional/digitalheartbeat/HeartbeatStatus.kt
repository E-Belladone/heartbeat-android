package com.technicallyfunctional.digitalheartbeat

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.technicallyfunctional.digitalheartbeat.databinding.ActivityHeartbeatStatusBinding

class HeartbeatStatus : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHeartbeatStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        when (defaultSharedPreferences.getString("ui_theme", "default"))
        {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "default" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            else -> {
                Log.i("HeartbeatStatus.kt:32", "Invalid theme setting")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        binding = ActivityHeartbeatStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            val navController = findNavController(R.id.nav_host_fragment_content_heartbeat_status)
            when (menuItem.itemId) {
                R.id.menu_about -> {
                    navController.navigate(R.id.action_StatusFragment_to_aboutFragment)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_settings -> {
                    navController.navigate(R.id.action_StatusFragment_to_SettingsFragment)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }

        val navController = findNavController(R.id.nav_host_fragment_content_heartbeat_status)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        binding.floatingActionButton.setOnClickListener {
//            val controller = findNavController(R.id.nav_host_fragment_content_heartbeat_status)
//            when (controller.currentDestination?.id) {
//                R.id.StatusFragment -> controller.navigate(R.id.action_StatusFragment_to_SettingsFragment)
//                R.id.SettingsFragment -> controller.navigate(R.id.action_SecondFragment_to_FirstFragment)
//                else -> {
//                    throw Error("Invalid navigation controller state")
//                }
//            }
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_heartbeat_status)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.topbar, menu)
        return true
    }
}