package com.suspended.hyperisland.manager

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import com.suspended.hyperisland.model.FlashlightState
import com.suspended.hyperisland.model.IslandEvent

class FlashlightManager(private val context: Context) {
    
    companion object {
        private const val TAG = "FlashlightManager"
    }
    
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn: Boolean = false
    private var isInitialized = false
    private var hasFlashlight = false
    
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(camId: String, enabled: Boolean) {
            if (camId == cameraId) {
                isFlashlightOn = enabled
                updateState()
            }
        }
        
        override fun onTorchModeUnavailable(camId: String) {
            if (camId == cameraId) {
                isFlashlightOn = false
                updateState()
            }
        }
    }
    
    fun initialize() {
        if (isInitialized) return
        
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            
            if (cameraManager == null) {
                Log.w(TAG, "CameraManager not available")
                return
            }
            
            cameraManager?.cameraIdList?.forEach { id ->
                try {
                    val characteristics = cameraManager?.getCameraCharacteristics(id)
                    val hasFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
                    
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id
                        hasFlashlight = true
                        return@forEach
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error checking camera $id", e)
                }
            }
            
            if (hasFlashlight) {
                cameraManager?.registerTorchCallback(torchCallback, null)
                isInitialized = true
                Log.d(TAG, "FlashlightManager initialized successfully")
            } else {
                Log.w(TAG, "No flashlight available on this device")
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Camera access error during initialization", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FlashlightManager", e)
        }
    }
    
    fun toggle() {
        if (!hasFlashlight) {
            Log.w(TAG, "Flashlight not available")
            return
        }
        
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, !isFlashlightOn)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to toggle flashlight", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error toggling flashlight", e)
        }
    }
    
    fun turnOn() {
        if (!hasFlashlight) return
        
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, true)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to turn on flashlight", e)
        }
    }
    
    fun turnOff() {
        if (!hasFlashlight) return
        
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, false)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to turn off flashlight", e)
        }
    }
    
    fun isOn(): Boolean = isFlashlightOn
    
    fun isAvailable(): Boolean = hasFlashlight
    
    private fun updateState() {
        val state = FlashlightState(isOn = isFlashlightOn)
        IslandStateManager.processEvent(IslandEvent.FlashlightUpdate(state))
    }
    
    fun release() {
        if (isInitialized) {
            try {
                cameraManager?.unregisterTorchCallback(torchCallback)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to unregister torch callback", e)
            }
            isInitialized = false
        }
    }
}
