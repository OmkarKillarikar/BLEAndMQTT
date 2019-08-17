package com.okandroid.bleandmqtt.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.okandroid.bleandmqtt.R
import com.okandroid.bleandmqtt.models.BleDevice

class BleDevicesAdapter(private val context: Context) : RecyclerView.Adapter<BleDevicesAdapter.BleDeviceViewHolder>() {

    var bleDevices: ArrayList<BleDevice> = ArrayList()

    fun isDeviceAlreadyFound(deviceToCheck: BleDevice): Boolean {
        for (bleDevice in bleDevices) {
            if (bleDevice.address == deviceToCheck.address) {
                return true
            }
        }
        return false
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): BleDeviceViewHolder {
        return BleDeviceViewHolder(LayoutInflater.from(context).inflate(R.layout.row_ble_device, viewGroup, false))
    }

    override fun getItemCount(): Int {
        if (bleDevices == null) {
            return 0
        }
        return bleDevices.size
    }

    override fun onBindViewHolder(holder: BleDeviceViewHolder, position: Int) {
        if (TextUtils.isEmpty(bleDevices[position].name)) {
            holder.tvName.text = context.getString(R.string.ble_name_not_available)
        } else {
            holder.tvName.text = bleDevices[position].name
        }

        holder.tvAddress.text = bleDevices[position].address
    }

    class BleDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById<TextView>(R.id.tvBleDeviceName)!!
        val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)!!
    }
}