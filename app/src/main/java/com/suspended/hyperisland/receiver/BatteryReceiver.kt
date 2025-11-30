package com.suspended.hyperisland.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.suspended.hyperisland.manager.IslandStateManager
import com.suspended.hyperisland.model.ChargingState
import com.suspended.hyperisland.model.IslandEvent

class BatteryReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                handleBatteryChanged(context, intent)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                handlePowerConnected(context)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                handlePowerDisconnected()
            }
        }
    }
    
    private fun handleBatteryChanged(context: Context, intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (scale > 0) level * 100 / scale else 0
        
        val state = ChargingState(
            isCharging = isCharging,
            batteryLevel = batteryPct,
            chargingWattage = calculateWattage(context, intent),
            estimatedTimeToFull = 0
        )
        
        IslandStateManager.processEvent(IslandEvent.ChargingUpdate(state))
    }
    
    private fun handlePowerConnected(context: Context) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        val state = ChargingState(
            isCharging = true,
            batteryLevel = batteryLevel,
            chargingWattage = 18f,
            estimatedTimeToFull = 0
        )
        
        IslandStateManager.processEvent(IslandEvent.ChargingUpdate(state))
    }
    
    private fun handlePowerDisconnected() {
        val state = ChargingState(
            isCharging = false,
            batteryLevel = 0,
            chargingWattage = 0f,
            estimatedTimeToFull = 0
        )
        
        IslandStateManager.processEvent(IslandEvent.ChargingUpdate(state))
    }
    
    private fun calculateWattage(context: Context, intent: Intent): Float {
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentAmps = Math.abs(currentNow) / 1000000f
        
        val wattage = voltage * currentAmps
        return if (wattage > 1) wattage else 18f
    }
}
