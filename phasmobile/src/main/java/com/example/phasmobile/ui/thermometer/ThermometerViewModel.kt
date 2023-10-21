package com.example.phasmobile.ui.thermometer

import android.graphics.drawable.Drawable
import android.media.Image
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.ViewModel
import com.example.phasmobile.BR

class ThermometerViewModel : BaseObservable() {
    private var _temp = 60
    private var _unit = Unit.F

    val unit: Unit
        get() = _unit

    @Bindable
    fun getTemp(): String {
        return if (_unit == Unit.F) {
            "$_temp.0";
        } else {
            val c = (_temp.toDouble() - 32.0) * (5.0 / 9.0)
            String.format("%.1f", c)
        }
    }

    fun setTemp(value: Int) {
        _temp = value
        notifyPropertyChanged(BR.temp)
    }

    fun toggleUnit() {
        _unit = when(_unit) {
            Unit.F -> Unit.C
            Unit.C -> Unit.F
        }
        notifyPropertyChanged(BR.temp)
    }

    companion object {
        private val instance = ThermometerViewModel()

        public fun getInstance(): ThermometerViewModel {
            return instance
        }
    }
}
enum class Unit {
    F, C
}
