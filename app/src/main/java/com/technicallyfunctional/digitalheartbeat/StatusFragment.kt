package com.technicallyfunctional.digitalheartbeat

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.technicallyfunctional.digitalheartbeat.databinding.FragmentStatusBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StatusFragment : Fragment() {
    companion object {
        val ACTION_REQUEST_UPDATE: String = "ACTION_REQUEST_UPDATE"
        val ACTION_UPDATE: String = "ACTION_UPDATE"

        var Status: String = "Unknown"
        var SubStatus: String = "Unknown"
        var StatusDetails: String = "Unknown"

        var updateReceiver: UpdateReceiver? = null
        val intentFilter: IntentFilter = IntentFilter(ACTION_UPDATE)

        private var fragmentStatusBinding: FragmentStatusBinding? = null
        private val binding get() = fragmentStatusBinding!!
    }

    private var serviceBinding: IBinder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentStatusBinding = FragmentStatusBinding.inflate(inflater, container, false)

        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonStartStop.setOnClickListener{
            val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (defaultSharedPreferences.getBoolean("sync_location", false)) {
                if (!isLocationPermissionGranted())
                    Toast.makeText(requireContext(), "Permission not granted", Toast.LENGTH_SHORT).show()
            }
            val service = ForegroundService()
            serviceBinding = service.Binder()
            val intent = Intent(requireContext(), service::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                requireContext().applicationContext.startForegroundService(intent)
            else
                requireContext().applicationContext.startService(intent)
            binding.textViewStatus.text = "Starting"
        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(updateReceiver)
    }

    override fun onResume() {
        super.onResume()
        if (updateReceiver == null) updateReceiver = UpdateReceiver()

        requireContext().registerReceiver(updateReceiver, intentFilter)
    }

    override fun onDestroy() {
        try {
            requireContext().unregisterReceiver(updateReceiver)
        } catch (e: Exception){
            Log.i("StatusFragment.kt:89", "Receiver not registered, no need to unregister.")
        }
        updateReceiver = null
        super.onDestroy()
    }

    class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.action == ACTION_UPDATE) {
                    Status = intent.extras?.getString("status").toString()
                    SubStatus = intent.extras?.getString("substatus").toString()
                    StatusDetails = intent.extras?.getString("statusdetails").toString()
                    binding.textViewStatus.text = Status
                    binding.textViewSubStatus.text = SubStatus
                    binding.textViewStatusDetails.text = StatusDetails
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        when (Status) {
                            "Running" ->
                                binding.textViewStatus.setTextColor(context!!.getColor(R.color.running_green))


                            else -> binding.textViewStatus.setTextColor(context!!.getColor(R.color.not_running_red))
                        }
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentStatusBinding = null
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this.requireActivity(),
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this.requireActivity(),
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1
                )
            }
            false
        } else {
            true
        }
    }
}