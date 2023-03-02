package com.example.myapplication

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat



fun Activity.isPermitionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission) == PackageManager.PERMISSION_GRANTED
}