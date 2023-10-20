package com.example.phasmobile.util

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.time.Duration
import java.time.Instant
import java.util.Calendar
import java.util.Date

private const val TAG = "Bluetooth"
class Tracker(
    private val viewModel: MainViewModel, private val ctx: Context
) {
    private var beacons: BeaconList = BeaconList()

    fun run() {
        Log.i(TAG, "Tracking")
        val bluetoothManager = ctx.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        val bluetoothScanner = bluetoothAdapter.bluetoothLeScanner

        scanDevices(bluetoothScanner)
    }

    private fun scanDevices(scanner: BluetoothLeScanner) {
        if (ctx.checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        Log.i(TAG, "Starting scan...")

        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(leScanCallback)
            scanDevices(scanner)
        }, 20000)
        scanner.startScan(leScanCallback)
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)


            fun ByteArray.toHex(): String =
                joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

            val serviceBytes = result.scanRecord?.bytes ?: byteArrayOf()
            val rssi = result.rssi

            val code = byteArrayOf(0x03.toByte(), 0x55.toByte(), 0xFF.toByte())
            var codeIndex = serviceBytes.toHex().indexOf(code.toHex())

            if (codeIndex != -1) {
                Log.d(TAG, "Found device")
                // Hex Char to byte index
                val idIndex = (codeIndex / 2) + code.size
                val roomId: Int = serviceBytes[idIndex].toInt();

                val newClosest = beacons.updateBeacon(roomId, rssi)
                Log.d(TAG, "RoomiD: $roomId Closest: $newClosest")
                if (newClosest != null) {
                    viewModel.updateClosestBeacon(newClosest);
                }
            }
        }
    }

}

class BeaconList {
    class Beacon() {
        var lastKnownSignal: Int? = null
        var lastUpdate: Instant = Instant.now()
    }

    private var beacons: Array<Beacon> = Array(20) { Beacon() }

    private var closestRoom: Int? = null

    public fun updateBeacon(roomId: Int, signalStrength: Int): Int? {
        val beacon = beacons[roomId]
        beacon.lastKnownSignal = signalStrength
        beacon.lastUpdate = Instant.now()

        // Recalculate closest beacon
        var closestSignal = Int.MIN_VALUE
        var closest: Int? = null

        for ((ix, beacon) in beacons.withIndex()) {
            // TODO check last update
            val time = Instant.now()
            val elapsed = Duration.between(beacon.lastUpdate, time);
            if (elapsed.seconds < 2)  {
                Log.d(TAG, "Checking beacon ${beacon.lastKnownSignal}")
                val signal = beacon.lastKnownSignal ?: Int.MIN_VALUE
                if (signal > closestSignal) {
                    closestSignal = signal
                    closest = ix
                }
            }
        }

        return closest
    }

}
