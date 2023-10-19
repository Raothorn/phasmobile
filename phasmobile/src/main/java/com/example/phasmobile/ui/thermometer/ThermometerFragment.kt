package com.example.phasmobile.ui.thermometer

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.phasmobile.R
import com.example.phasmobile.databinding.FragmentThermometerBinding
import com.example.phasmobile.util.MainViewModel
import kotlinx.coroutines.launch

class ThermometerFragment : Fragment() {

    companion object {
        fun newInstance() = ThermometerFragment()
    }

    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val viewModel: ThermometerViewModel = ThermometerViewModel()
    private var _binding: FragmentThermometerBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            mainViewModel.uiState.collect { uiState ->
                viewModel.setTemp(uiState.currentTemp())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThermometerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewModel = viewModel

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}