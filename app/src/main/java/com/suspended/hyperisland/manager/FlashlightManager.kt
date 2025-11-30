package com.suspended.hyperisland.manager

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.suspended.hyperisland.model.FlashlightState
import com.suspended.hyperisland.model.IslandEvent

class FlashlightManager(private val context: Context) {
    
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn: Boolean = false
    
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
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                val hasFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
                
                if (hasFlash == true && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    return@forEach
                }
            }
            
            cameraManager?.registerTorchCallback(torchCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    fun toggle() {
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, !isFlashlightOn)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    fun turnOn() {
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, true)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    fun turnOff() {
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, false)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
    
    fun isOn(): Boolean = isFlashlightOn
    
    private fun updateState() {
        val state = FlashlightState(isOn = isFlashlightOn)
        IslandStateManager.processEvent(IslandEvent.FlashlightUpdate(state))
    }
    
    fun release() {
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
