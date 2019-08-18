package com.okandroid.bleandmqtt.utils

object Constants {
    object ActivityResultRequestCode {
        const val ENABLE_BT = 1
    }

    const val SCAN_PERIOD = 10 * 1000L
    const val BLE_INTENT_FILTER = "BLE_INTENT_FILTER"

    object IntentExtras {
        const val BLE_DEVICE = "BLE_DEVICE"
        const val SCAN_FINISHED = "SCAN_FINISHED"
    }

    object PermissionRequestCode {
        const val LOCATION = 1
    }
}