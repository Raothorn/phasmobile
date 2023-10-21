package com.example.phasmobile.ui.journal

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phasmobile.BR

private const val TAG="ViewModel"
class JournalViewModel : BaseObservable() {
    private var _emf = false
    private var _spiritBox = false
    private var _writing = false
    private var _orbs = false
    private var _uv = false
    private var _freezing = false;

    private val _ghosts = MutableLiveData<List<String>>(emptyList())

    val ghosts:LiveData<List<String>>
        get() = _ghosts
    init {
        updateGhosts()
    }

    init {
        updateGhosts()
    }

    private fun updateGhosts() {
        val allGhosts = arrayOf(
            "Spirit",
            "Poltergeist",
            "Banshee",
            "Jinn",
            "Mare",
            "Revenant",
            "Shade",
            "Demon",
            "Yurei",
            "Oni",
            "Wraith",
            "Phantom",
        )

        _ghosts.value = allGhosts.filter { ghost ->
            when (ghost) {
                "Spirit" -> !(_freezing || _emf || _orbs)
                "Wraith" -> !(_writing || _emf || _orbs)
                "Phantom" -> !(_uv || _writing || _spiritBox)
                "Poltergeist" -> !(_writing || _freezing || _emf)
                "Banshee" -> !(_writing || _spiritBox || _orbs)
                "Jinn" -> !(_uv || _writing || _freezing)
                "Mare" -> !(_uv || _writing || _emf)
                "Revenant" -> !(_spiritBox || _freezing || _orbs)
                "Shade" -> !(_uv || _spiritBox || _freezing)
                "Demon" -> !(_uv || _emf || _orbs)
                "Yurei" -> !(_uv || _spiritBox || _emf)
                "Oni" -> !(_uv || _freezing || _orbs)
                else -> false
            }
        }
    }

    @Bindable
    fun getEmf(): Boolean {
        return _emf
    }

    fun setEmf(value: Boolean) {
        _emf = value
        updateGhosts()
        notifyPropertyChanged(BR.emf)
    }

    @Bindable
    fun getSpiritBox(): Boolean {
        return _spiritBox
    }

    fun setSpiritBox(value: Boolean) {
        _spiritBox = value
        updateGhosts()
        notifyPropertyChanged(BR.spiritBox)
    }

    @Bindable
    fun getUv(): Boolean {
        return _uv
    }

    fun setUv(value: Boolean) {
        _uv = value
        updateGhosts()
        notifyPropertyChanged(BR.uv)
    }

    @Bindable
    fun getWriting(): Boolean {
        return _writing
    }

    fun setWriting(value: Boolean) {
        _writing = value
        updateGhosts()
        notifyPropertyChanged(BR.writing)
    }

    @Bindable
    fun getOrbs(): Boolean {
        return _orbs
    }

    fun setOrbs(value: Boolean) {
        _orbs = value
        updateGhosts()
        notifyPropertyChanged(BR.orbs)
    }

    @Bindable
    fun getFreezing(): Boolean {
        return _freezing
    }

    fun setFreezing(value: Boolean) {
        _freezing = value
        updateGhosts()
        notifyPropertyChanged(BR.freezing)
    }

    companion object {
        private var instance: JournalViewModel? = null
        public fun getInstance(): JournalViewModel {
            if (instance == null) {
                instance = JournalViewModel()
            }
            return instance!!
        }
    }
}