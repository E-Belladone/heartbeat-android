package com.technicallyfunctional.digitalheartbeat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.technicallyfunctional.digitalheartbeat.databinding.ActivityHeartbeatStatusBinding

class HeartbeatStatus : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHeartbeatStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHeartbeatStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_heartbeat_status)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.floatingActionButton.setOnClickListener { view ->
            val controller = findNavController(R.id.nav_host_fragment_content_heartbeat_status);
            when (controller.currentDestination?.id) {
                R.id.FirstFragment -> controller.navigate(R.id.action_FirstFragment_to_SecondFragment)
                R.id.SecondFragment -> controller.navigate(R.id.action_SecondFragment_to_FirstFragment)
                else -> {
                    throw Error("Invalid navigation controller state")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_heartbeat_status)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}