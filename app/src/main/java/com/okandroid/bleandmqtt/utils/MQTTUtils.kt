package com.okandroid.bleandmqtt.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MQTTUtils private constructor(private val context: Context) {

    private var mqttAndroidClient: MqttAndroidClient? = null

    object MQQTTConstants {
        const val mqttServerUri: String = "tcp://soldier.cloudmqtt.com:17187"
        const val publishTopic: String = "bleData"
        const val clientId: String = "testAndroidClient"
    }

    init {

        mqttAndroidClient = MqttAndroidClient(context, MQQTTConstants.mqttServerUri, MQQTTConstants.clientId)
        mqttAndroidClient?.setCallback(object : MqttCallbackExtended {

            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                Log.d("----MQTT", "connected!")
                if (reconnect) {
                    showToast("reconnected")
                } else {
                    showToast("connected")
                }
            }

            override fun connectionLost(cause: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {

            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d("----MQTT", "data sent!")
                showToast("data sent")
            }
        })


        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = true
        mqttConnectOptions.userName = "tgcufhsp"
        mqttConnectOptions.password = "x8rt6qN8YlIS".toCharArray()

        mqttAndroidClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                mqttAndroidClient?.setBufferOpts(disconnectedBufferOptions)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                showToast("connection failure")
                Log.e("----MQTT", "connect failed")
            }
        })
    }

    companion object {

        //TODO: change this module to a service

        private var instance: MQTTUtils? = null

        @Synchronized
        fun getInstance(context: Context): MQTTUtils {
            if (instance == null) {
                instance = MQTTUtils(context)
            }
            return instance as MQTTUtils
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun disconnect() {
        mqttAndroidClient?.disconnect()
    }

    fun publishMessage(message: String) {
        try {
            val mqttMessage = MqttMessage()
            mqttMessage.payload = message.toByteArray()
            mqttAndroidClient?.publish(MQQTTConstants.publishTopic, mqttMessage)
            if (mqttAndroidClient?.isConnected == true) {

            } else {
                showToast("mqtt not established")

            }
        } catch (e: Exception) {
            Log.e("----MQTT", "not estd.")
            e.printStackTrace()
        }

    }

}