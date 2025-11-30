package com.suspended.hyperisland.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.suspended.hyperisland.model.ChargingState
import com.suspended.hyperisland.model.IslandEvent

class ChargingManager(private val context: Context) {
    
    private var batteryReceiver: BroadcastReceiver? = null
    
    fun initialize() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateChargingState(intent)
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        
        val stickyIntent = context.registerReceiver(batteryReceiver, filter)
        stickyIntent?.let { updateChargingState(it) }
    }
    
    private fun updateChargingState(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level * 100 / scale.toFloat()
        
        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val wattage = calculateChargingWattage(intent, chargePlug)
        
        val state = ChargingState(
            isCharging = isCharging,
            batteryLevel = batteryPct.toInt(),
            chargingWattage = wattage,
            estimatedTimeToFull = calculateTimeToFull(batteryPct.toInt(), wattage)
        )
        
        IslandStateManager.processEvent(IslandEvent.ChargingUpdate(state))
    }
    
    private fun calculateChargingWattage(intent: Intent, chargePlug: Int): Float {
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
        
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        
        val currentAmps = Math.abs(currentNow) / 1000000f
        val wattage = voltage * currentAmps
        
        return when {
            wattage > 1 -> wattage
            chargePlug == BatteryManager.BATTERY_PLUGGED_AC -> 18f
            chargePlug == BatteryManager.BATTERY_PLUGGED_USB -> 10f
            chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS -> 15f
            else -> 0f
        }
    }
    
    private fun calculateTimeToFull(currentLevel: Int, wattage: Float): Long {
        if (currentLevel >= 100 || wattage <= 0) return 0
        
        val batteryCapacityWh = 4500f * 3.85f / 1000f
        val remainingPercent = 100 - currentLevel
        val remainingWh = batteryCapacityWh * remainingPercent / 100f
        val hoursToFull = remainingWh / wattage
        
        return (hoursToFull * 60 * 60 * 1000).toLong()
    }
    
    fun getCurrentState(): ChargingState {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val isCharging = batteryManager.isCharging
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        return ChargingState(
            isCharging = isCharging,
            batteryLevel = batteryLevel,
            chargingWattage = 0f,
            estimatedTimeToFull = 0
        )
    }
    
    fun release() {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
