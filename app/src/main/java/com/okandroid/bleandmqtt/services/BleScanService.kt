package com.okandroid.bleandmqtt.services

import android.R
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.okandroid.bleandmqtt.models.BleDevice
import com.okandroid.bleandmqtt.utils.Constants


class BleScanService : Service() {

    private val handler = Handler()
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        scanLeDevice()
        return Service.START_STICKY
    }

    private fun scanLeDevice() {
        handler.postDelayed({
            bluetoothAdapter?.stopLeScan(scanCallback)
        }, Constants.SCAN_PERIOD)
//        bluetoothAdapter?.startLeScan(arrayOf(UUID.fromString("00001803-0000-1000-8000-00805F9B34FB")), scanCallback)
        bluetoothAdapter?.startLeScan(scanCallback)
    }

    private val scanCallback = BluetoothAdapter.LeScanCallback { bluetoothDevice, rssi, p2 ->
        Log.d("---ble found---", bluetoothDevice.address + rssi)

        val payload = Intent(Constants.BLE_INTENT_FILTER)
        payload.putExtra(
            Constants.IntentExtras.BLE_DEVICE, BleDevice(
                name = bluetoothDevice.name,
                address = bluetoothDevice.address,
                rssi = rssi.toString()
            )
        )

        LocalBroadcastManager.getInstance(this@BleScanService).sendBroadcast(payload)
    }

    private fun startForegroundService() {
        val intent = Intent()
        val channelId = "com.example.simpleapp"
        val channelName = "My Background Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_media_play)
            .setContentTitle("Scanning BLE devices")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(1, notification)
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

}