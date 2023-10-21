package com.example.phasmobile.ui.journal

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.phasmobile.databinding.FragmentJournalBinding

private const val TAG = "Interface"
class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModel = JournalViewModel.getInstance()
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        val root = binding.root

        val tvs = hashMapOf(
            "Spirit" to binding.tvSpirit,
            "Poltergeist" to binding.tvPoltergeist,
            "Banshee" to binding.tvBanshee,
            "Jinn" to binding.tvJinn,
            "Mare" to binding.tvMare,
            "Revenant" to binding.tvRevenant,
            "Shade" to binding.tvShade,
            "Demon" to binding.tvDemon,
            "Yurei" to binding.tvYurei,
            "Oni" to binding.tvOni,
            "Wraith" to binding.tvWraith,
            "Phantom" to binding.tvPhantom
        )

        viewModel.ghosts.observe(viewLifecycleOwner) { newGhosts ->
            Log.i(TAG, "Ghosts changed: $newGhosts")
            for ((name, tvId) in tvs) {
                val alpha = if (newGhosts.contains(name)) 255 else 64
                tvId.setTextColor(Color.argb(alpha, 0, 0, 0))
            }
        }

        return root
    }
}
