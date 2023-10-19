package com.example.phasmobile.ui.journal

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phasmobile.BR

class JournalViewModel : BaseObservable() {
    private var _emf = false
    private var _spiritBox = false
    private var _writing = false
    private var _orbs = false
    private var _uv = false
    private var _freezing = false;

    private val _ghosts = MutableLiveData<ArrayList<String>>(ArrayList())
    val ghosts:LiveData<ArrayList<String>>
        get() = _ghosts

    init {
        updateGhosts()
    }

    fun updateGhosts() {
        val allGhosts = hashMapOf(
            "Spirit" to true,
            "Poltergeist" to true,
            "Banshee" to true,
            "Jinn" to true,
            "Mare" to true,
            "Revenant" to true,
            "Shade" to true,
            "Demon" to true,
            "Yurei" to true,
            "Oni" to true,
        )

        if (_emf) {
            allGhosts["Spirit"] = false
            allGhosts["Wraith"] = false
            allGhosts["Poltergeist"] = false
            allGhosts["Mare"] = false
            allGhosts["Demon"] = false
            allGhosts["Yurei"] = false
        }

        if(_writing) {
            allGhosts["Wraith"] = false
            allGhosts["Phantom"] = false
            allGhosts["Poltergeist"] = false
            allGhosts["Banshee"] = false
            allGhosts["Jinn"] = false
            allGhosts["Mare"] = false
        }

        if(_uv) {
            allGhosts["Phantom"] = false
            allGhosts["Jinn"] = false
            allGhosts["Mare"] = false
            allGhosts["Shade"] = false
            allGhosts["Demon"] = false
            allGhosts["Yurei"] = false
            allGhosts["Oni"] = false
        }

        if (_spiritBox){
            allGhosts["Phantom"] = false
            allGhosts["Banshee"] = false
            allGhosts["Revenant"] = false
            allGhosts["Shade"] = false
            allGhosts["Demon"] = false
            allGhosts["Yurei"] = false
        }

        if (_orbs){
            allGhosts["Spirit"] = false
            allGhosts["Wraith"] = false
            allGhosts["Banshee"] = false
            allGhosts["Revenant"] = false
            allGhosts["Demon"] = false
            allGhosts["Oni"] = false
        }

        if (_freezing){
            allGhosts["Spirit"] = false
            allGhosts["Poltergeist"] = false
            allGhosts["Jinn"] = false
            allGhosts["Revenant"] = false
            allGhosts["Shade"] = false
            allGhosts["Oni"] = false
        }

        _ghosts.value = ArrayList()
        for (ghost in allGhosts.keys) {
            if (allGhosts[ghost] == true) {
                _ghosts.value?.add(ghost)
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
}