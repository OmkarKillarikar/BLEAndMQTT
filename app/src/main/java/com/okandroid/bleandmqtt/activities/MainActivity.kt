package com.okandroid.bleandmqtt.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.okandroid.bleandmqtt.R
import com.okandroid.bleandmqtt.adapters.BleDevicesAdapter
import com.okandroid.bleandmqtt.models.BleDevice
import com.okandroid.bleandmqtt.services.BleScanService
import com.okandroid.bleandmqtt.utils.Constants
import com.okandroid.bleandmqtt.utils.MQTTUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
    private var bleDeviceAdapter: BleDevicesAdapter? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var mqttUtil: MQTTUtils? = null

    private val bleBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when {
                    intent.hasExtra(Constants.IntentExtras.BLE_DEVICE) -> {
                        val bleDevice = intent.getSerializableExtra(Constants.IntentExtras.BLE_DEVICE) as BleDevice
                        if (bleDevice != null) {
                            if (bleDeviceAdapter?.isDeviceAlreadyFound(bleDevice) == false) {
                                bleDeviceAdapter?.bleDevices?.add(bleDevice)
                            }
                            bleDeviceAdapter?.notifyDataSetChanged()
                        }
                    }
                    intent.hasExtra(Constants.IntentExtras.SCAN_FINISHED) -> {
                        if (refreshBle.isRefreshing) refreshBle.isRefreshing = false

                        if (bleDeviceAdapter?.bleDevices?.size == 0) {
                            llNoData.visibility = View.VISIBLE
                            llData.visibility = View.GONE
                        } else {
                            llNoData.visibility = View.GONE
                            llData.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        mqttUtil = MQTTUtils.getInstance(this@MainActivity)
        Thread.sleep(1000)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.PermissionRequestCode.LOCATION
            )
        } else {
            initBLEScan()
        }
    }

    private fun initViews() {
        bleDeviceAdapter = BleDevicesAdapter(this)
        rvBleDevices.adapter = bleDeviceAdapter
        val gson = Gson()
        btnSendData.setOnClickListener {
            for (bleDevice in bleDeviceAdapter!!.bleDevices) {
                mqttUtil?.publishMessage(gson.toJson(bleDevice))
            }
        }
        refreshBle.setOnRefreshListener {
            initBLEScan()
        }
    }

    private fun initBLEScan() {
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show()
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show()
            finish()
        }

        if (bluetoothAdapter?.isEnabled == true) {
            refreshBle.isRefreshing = true
            bleDeviceAdapter?.bleDevices?.clear()
            startBleService()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, Constants.ActivityResultRequestCode.ENABLE_BT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.ActivityResultRequestCode.ENABLE_BT -> {
                if (bluetoothAdapter?.isEnabled == true) {
                    startBleService()
                } else {
                    Toast.makeText(this, R.string.ble_required, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.PermissionRequestCode.LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initBLEScan()
                } else {
                    Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
    }

    private fun startBleService() {
        try {
            val startScan = Intent(this, BleScanService::class.java)
            startService(startScan)
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(bleBroadcastReceiver, IntentFilter(Constants.BLE_INTENT_FILTER))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bleBroadcastReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        mqttUtil?.disconnect()
        stopService(Intent(this, BleScanService::class.java))
        super.onDestroy()
    }
}