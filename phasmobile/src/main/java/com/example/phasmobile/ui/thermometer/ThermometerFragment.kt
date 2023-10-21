package com.example.phasmobile.ui.thermometer

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.phasmobile.R
import com.example.phasmobile.databinding.FragmentThermometerBinding
import com.example.phasmobile.util.MainViewModel
import kotlinx.coroutines.launch

class ThermometerFragment : Fragment() {


    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val viewModel: ThermometerViewModel = ThermometerViewModel.getInstance()
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
        val layout = binding.clTherm

        setBg(layout);

        val unitBtn = binding.btnUnit
        unitBtn.setOnClickListener {
            viewModel.toggleUnit()
            setBg(layout);

        }

        return root
    }

    private fun setBg(layout: ConstraintLayout) {
        val bg = when(viewModel.unit) {
            Unit.F -> R.drawable.thermometer_f
            Unit.C -> R.drawable.thermometer_c
        }
        layout.background = ResourcesCompat.getDrawable(resources, bg, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}