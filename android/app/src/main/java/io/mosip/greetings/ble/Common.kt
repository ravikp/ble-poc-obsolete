package io.mosip.greetings.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class Common {
    companion object {
        private const val REQUEST_CODE_REQUIRED_PERMISSIONS = 2
        private val REQUIRED_PERMISSIONS_OVER_API_LEVEL_31 = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        private var REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        init {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                REQUIRED_PERMISSIONS += REQUIRED_PERMISSIONS_OVER_API_LEVEL_31
            }
        }
        private const val REQUEST_ENABLE_BT = 3

        private fun checkPermissions(context: Context?): Boolean {
            for (permission in REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(
                        context!!,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }

        fun requestForRequiredPermissions(activity: Activity, context: Context) {
            if (!checkPermissions(context)) {
                ActivityCompat.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_REQUIRED_PERMISSIONS
                )
                Toast.makeText(context, "Requested permissions", Toast.LENGTH_SHORT).show()
            }
        }

        fun startBluetooth(activity: Activity) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
}