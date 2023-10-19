package com.example.phasmobile.ui.home

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phasmobile.BR
import com.example.phasmobile.util.MainViewModel

class HomeViewModel(private val mainViewModel: MainViewModel) : BaseObservable() {
    var _name: String = "Gaston"

    @Bindable
    fun getName(): String {
        return _name
    }
    fun setName(value: String) {
        _name = value
        mainViewModel.updateName(value)
        notifyPropertyChanged(BR.name)
    }

    @get:Bindable
    var connected: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.connected)
        }
}