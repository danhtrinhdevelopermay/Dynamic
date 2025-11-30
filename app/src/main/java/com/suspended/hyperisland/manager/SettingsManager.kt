package com.suspended.hyperisland.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "island_settings")

class SettingsManager(private val context: Context) {
    
    companion object {
        private val POSITION_X = intPreferencesKey("position_x")
        private val POSITION_Y = intPreferencesKey("position_y")
        private val SIZE_SCALE = floatPreferencesKey("size_scale")
        private val ALWAYS_ON_TOP = booleanPreferencesKey("always_on_top")
        
        const val DEFAULT_POSITION_X = 0
        const val DEFAULT_POSITION_Y = 0
        const val DEFAULT_SIZE_SCALE = 1.0f
        const val DEFAULT_ALWAYS_ON_TOP = true
        
        const val MIN_SIZE_SCALE = 0.5f
        const val MAX_SIZE_SCALE = 1.5f
        
        const val MIN_POSITION_X = -200
        const val MAX_POSITION_X = 200
        const val MIN_POSITION_Y = -50
        const val MAX_POSITION_Y = 200
    }
    
    val positionX: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[POSITION_X] ?: DEFAULT_POSITION_X
    }
    
    val positionY: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[POSITION_Y] ?: DEFAULT_POSITION_Y
    }
    
    val sizeScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SIZE_SCALE] ?: DEFAULT_SIZE_SCALE
    }
    
    val alwaysOnTop: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ALWAYS_ON_TOP] ?: DEFAULT_ALWAYS_ON_TOP
    }
    
    suspend fun setPositionX(x: Int) {
        context.dataStore.edit { preferences ->
            preferences[POSITION_X] = x.coerceIn(MIN_POSITION_X, MAX_POSITION_X)
        }
    }
    
    suspend fun setPositionY(y: Int) {
        context.dataStore.edit { preferences ->
            preferences[POSITION_Y] = y.coerceIn(MIN_POSITION_Y, MAX_POSITION_Y)
        }
    }
    
    suspend fun setSizeScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[SIZE_SCALE] = scale.coerceIn(MIN_SIZE_SCALE, MAX_SIZE_SCALE)
        }
    }
    
    suspend fun setAlwaysOnTop(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ALWAYS_ON_TOP] = enabled
        }
    }
    
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences[POSITION_X] = DEFAULT_POSITION_X
            preferences[POSITION_Y] = DEFAULT_POSITION_Y
            preferences[SIZE_SCALE] = DEFAULT_SIZE_SCALE
            preferences[ALWAYS_ON_TOP] = DEFAULT_ALWAYS_ON_TOP
        }
    }
}

data class IslandSettings(
    val positionX: Int = SettingsManager.DEFAULT_POSITION_X,
    val positionY: Int = SettingsManager.DEFAULT_POSITION_Y,
    val sizeScale: Float = SettingsManager.DEFAULT_SIZE_SCALE
)
