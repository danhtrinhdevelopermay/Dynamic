package com.suspended.hyperisland.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.suspended.hyperisland.model.ChargingState
import com.suspended.hyperisland.model.IslandEvent

class ChargingManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ChargingManager"
    }
    
    private var batteryReceiver: BroadcastReceiver? = null
    private var isRegistered = false
    
    fun initialize() {
        if (isRegistered) return
        
        try {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    try {
                        updateChargingState(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating charging state", e)
                    }
                }
            }
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            
            val stickyIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(batteryReceiver, filter)
            }
            
            isRegistered = true
            stickyIntent?.let { updateChargingState(it) }
            
            Log.d(TAG, "ChargingManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ChargingManager", e)
        }
    }
    
    private fun updateChargingState(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (scale > 0) level * 100f / scale else 0f
        
        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val wattage = getChargingWattage(intent, chargePlug)
        
        val state = ChargingState(
            isCharging = isCharging,
            batteryLevel = batteryPct.toInt(),
            chargingWattage = wattage,
            estimatedTimeToFull = 0
        )
        
        IslandStateManager.processEvent(IslandEvent.ChargingUpdate(state))
    }
    
    private fun getChargingWattage(intent: Intent, chargePlug: Int): Float {
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            
            if (batteryManager != null) {
                val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                
                if (currentNow != Int.MIN_VALUE && currentNow != 0) {
                    val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
                    if (voltage > 0) {
                        val currentAmps = Math.abs(currentNow) / 1000000f
                        val wattage = voltage * currentAmps
                        
                        if (wattage >= 1f && wattage <= 200f) {
                            return wattage
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not calculate wattage", e)
        }
        
        return 0f
    }
    
    fun getCurrentState(): ChargingState {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val isCharging = batteryManager?.isCharging ?: false
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
            
            ChargingState(
                isCharging = isCharging,
                batteryLevel = if (batteryLevel != Int.MIN_VALUE) batteryLevel else 0,
                chargingWattage = 0f,
                estimatedTimeToFull = 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current state", e)
            ChargingState()
        }
    }
    
    fun release() {
        if (isRegistered) {
            batteryReceiver?.let {
                try {
                    context.unregisterReceiver(it)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to unregister receiver", e)
                }
            }
            isRegistered = false
        }
    }
}
