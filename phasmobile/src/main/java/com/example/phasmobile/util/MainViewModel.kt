package com.example.phasmobile.util

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ViewModel"

data class GameState (
    val ghostLocation: Int,
    val favoriteRoom: Int,
    val orbsVisible: Boolean,
    val ambientTemp: Int,
    val ghostRoomTemp: Int,
    val emfLevel: Int,
) {

}
data class UiState(
    val name: String = "Gaston",
    val closestBeacon: Int? = null,
    val connected: Boolean = false,
    val gamestate: GameState? = null
) {
    fun canSeeOrbs(): Boolean {
        if (!connected) return true;
        val orbsVisible = gamestate?.orbsVisible ?: false
        val favRoom = gamestate?.favoriteRoom ?: -1
        val room = closestBeacon ?: -2

        return orbsVisible && (favRoom == room)
    }

    fun currentTemp(): Int {
        return if (closestBeacon == gamestate?.favoriteRoom) {
            gamestate?.ghostRoomTemp ?: 50
        } else {
            gamestate?.ambientTemp ?: 50
        }
    }
}

class MainViewModel(app: Application, save: SavedStateHandle) : AndroidViewModel(app){
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val bleTracker: Tracker = Tracker(this, app.applicationContext);
    private val wsListener: WebSocketListener = WebSocketListener(this)

    companion object {
        public val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            var instance: MainViewModel? = null
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                if (instance != null) {
                    return instance as T
                }
                Log.d(TAG, "This should only get called once")
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                instance =  MainViewModel(
                    (application as Application),
                    savedStateHandle
                )
                return instance as T
            }
        }
    }

    fun updateName(value: String) {
        Log.i(TAG, "Name changed: $value")
        _uiState.update { currentState ->
            currentState.copy(
                name = value
            )
        }
    }

    fun updateClosestBeacon(value: Int) {
        if (value == _uiState.value.closestBeacon) {
            return
        }
        Log.i(TAG, "Location changed: $value")
        _uiState.update { currentState ->
            currentState.copy(
                closestBeacon = value
            )
        }

        wsListener.sendJsonMessage(
            "LocationUpdate",
            hashMapOf(
                "name" to _uiState.value.name,
                "location" to value
            )
        )
    }

    fun updateGamestate(value: GameState) {
        Log.i(TAG, "Gamestate changed: $value")
        _uiState.update { currentState ->
            currentState.copy(
                gamestate = value
            )
        }
    }

    fun updateConnected(value: Boolean) {
        Log.i(TAG, "Connection state changed: $value")
        _uiState.update { currentState ->
            currentState.copy(
                connected = value
            )
        }
    }


    fun connect() {
        Log.i(TAG, "Connecting...")
        viewModelScope.launch {
            wsListener.run()
        }

        // TODO don't do this until game starts
        viewModelScope.launch {
            bleTracker.run()
        }
    }

    fun canSeeOrbs(): Boolean {
        return uiState.value.canSeeOrbs()
    }

}
