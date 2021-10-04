package com.technicallyfunctional.digitalheartbeat

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.technicallyfunctional.digitalheartbeat.R
import com.technicallyfunctional.digitalheartbeat.databinding.FragmentStatusBinding
import java.lang.Exception

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StatusFragment : Fragment() {

    enum class BG_SERVICE_STATUS {
        NOT_RUNNING,
        RUNNING,
        UNKNOWN
    }

    private var _binding: FragmentStatusBinding? = null

    private val bgService = BackgroundService()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentStatusBinding.inflate(inflater, container, false)

        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



//        binding.buttonSettings.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        //binding.textViewStatus.text = getString(R.string.status_running)
        //binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.running_green));
        //binding.textViewStatusDetails.text = "${getString(R.string.status_last_push_datetime)}: 12 Seconds ago\n${getString(R.string.status_last_push_info)}: Device in active use\n${getString(R.string.status_last_push_location)}: Null Island"
        binding.buttonStartStop.setOnClickListener{
            when (getBackgroundServiceStatus())
            {
                BG_SERVICE_STATUS.UNKNOWN -> {
                    binding.textViewStatus.text = getString(R.string.status_unknown)
                    binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.unknown_grey))
                    binding.textViewStatusDetails.text = getString(R.string.status_unknown)
                    binding.buttonStartStop.text = getString(R.string.start_hb)
                    try{
                        requireContext().unregisterReceiver(bgService)
                        Log.i("BGService", "UnRegistered")
                    } catch (e: Exception) {
                        Log.i("UnregReceiver", "Failed: ${e.toString()}")
                    }
                    requireContext().registerReceiver(bgService, IntentFilter(Intent.ACTION_TIME_TICK))
                    Log.i("BGService", "Registered")
                    temp = BG_SERVICE_STATUS.RUNNING
                }
                BG_SERVICE_STATUS.NOT_RUNNING -> {
                    // TODO: Start BG service
                    binding.textViewStatus.text = getString(R.string.status_running)
                    binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.running_green))
                    binding.textViewStatusDetails.text = "${getString(R.string.status_last_push_datetime)}: ${getLastPushTime()}\n${getString(R.string.status_last_push_location)}: ${getLastPushLocation()}"
                    binding.buttonStartStop.text = getString(R.string.stop_hb)
                    requireContext().registerReceiver(bgService, IntentFilter(Intent.ACTION_TIME_TICK))
                    Log.i("BGService", "Registered")
                    temp = BG_SERVICE_STATUS.RUNNING
                }
                BG_SERVICE_STATUS.RUNNING -> {
                    // TODO: Stop BG service
                    binding.textViewStatus.text = getString(R.string.status_not_running)
                    binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_running_red))
                    binding.textViewStatusDetails.text = getString(R.string.status_not_running)
                    binding.buttonStartStop.text = getString(R.string.start_hb)
                    requireContext().unregisterReceiver(bgService)
                    Log.i("BGService", "UnRegistered")
                    temp = BG_SERVICE_STATUS.NOT_RUNNING
                }
            }
        }
        when (getBackgroundServiceStatus())
        {
            BG_SERVICE_STATUS.UNKNOWN -> {
                binding.textViewStatus.text = getString(R.string.status_unknown)
                binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.unknown_grey))
                binding.textViewStatusDetails.text = getString(R.string.status_unknown)
            }
            BG_SERVICE_STATUS.NOT_RUNNING -> {
                binding.textViewStatus.text = getString(R.string.status_not_running)
                binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.not_running_red))
                binding.textViewStatusDetails.text = getString(R.string.status_not_running)
            }
            BG_SERVICE_STATUS.RUNNING -> {
                binding.textViewStatus.text = getString(R.string.status_running)
                binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.running_green))
                binding.textViewStatusDetails.text = "${getString(R.string.status_last_push_datetime)}: ${getLastPushTime()}\n${getString(R.string.status_last_push_location)}: ${getLastPushLocation()}"
            }
        }
    }

    private var temp: BG_SERVICE_STATUS = BG_SERVICE_STATUS.UNKNOWN;

    private fun getBackgroundServiceStatus(): BG_SERVICE_STATUS
    {
        return temp
    }

    private fun getLastPushTime(): String
    {
        return "UNKNOWN"
    }

    private fun getLastPushLocation(): String
    {
        return "UNKNOWN"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}