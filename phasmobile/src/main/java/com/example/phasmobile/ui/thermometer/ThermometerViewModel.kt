package com.example.phasmobile.ui.thermometer

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.ViewModel
import com.example.phasmobile.BR

class ThermometerViewModel : BaseObservable() {
    private var _temp = 0

    @Bindable
    fun getTemp(): Int {
        return _temp
    }

    fun setTemp(value: Int) {
        _temp = value
        notifyPropertyChanged(BR.temp)
    }
}