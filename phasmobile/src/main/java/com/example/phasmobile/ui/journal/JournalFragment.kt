package com.example.phasmobile.ui.journal

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phasmobile.R
import com.example.phasmobile.databinding.FragmentJournalBinding

private const val TAG = "JournalFragment"
class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    companion object {
        fun newInstance() = JournalFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModel = JournalViewModel()
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        val root = binding.root

        val customAdapter = CustomAdapter()

        val recycleView: RecyclerView = binding.rvGhosts;
        recycleView.layoutManager = LinearLayoutManager(context)
        recycleView.adapter = customAdapter
        Log.d(TAG, "Adapter attached")

        viewModel.ghosts.observe(viewLifecycleOwner
        ) { newGhosts ->
            run {
                customAdapter.submitList(newGhosts)
            }
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}

class CustomAdapter() : ListAdapter<String, CustomAdapter.ViewHolder>(GhostDiffCallback)  {
    class ViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        private val textView: TextView

        init {
            textView = view.findViewById(R.id.textRowView)
        }

        fun bind(ghost: String) {
            textView.text = ghost;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_row_layout, parent, false);
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ghost = getItem(position)
        holder.bind(ghost)
    }

    object GhostDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}