package com.technicallyfunctional.digitalheartbeat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.technicallyfunctional.digitalheartbeat.R
import com.technicallyfunctional.digitalheartbeat.databinding.FragmentStatusBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StatusFragment : Fragment() {

    private var _binding: FragmentStatusBinding? = null

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

        binding.textViewStatus.text = getString(R.string.status_running)
        binding.textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.running_green));
        binding.textViewStatusDetails.text = "${getString(R.string.status_last_push_datetime)}: 12 Seconds ago\n${getString(R.string.status_last_push_info)}: Device in active use\n${getString(R.string.status_last_push_location)}: Null Island"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}