package com.okandroid.bleandmqtt.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
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
        holder.tvAddress.text = bleDevices[position].address
        val spanString = SpannableString(bleDevices[position].rssi + "dBm")
        spanString.setSpan(RelativeSizeSpan(1.5f), 0, spanString.length - 3, 0)
        holder.tvRssi.text = spanString
    }

    class BleDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRssi = itemView.findViewById<TextView>(R.id.tvRssi)!!
        val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)!!
    }
}