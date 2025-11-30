package com.suspended.hyperisland.manager

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import com.suspended.hyperisland.model.IslandEvent
import com.suspended.hyperisland.model.TimerState

class TimerManager {
    
    private var countDownTimer: CountDownTimer? = null
    private var remainingTimeMs: Long = 0
    private var totalTimeMs: Long = 0
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun startTimer(durationMs: Long) {
        stopTimer()
        
        totalTimeMs = durationMs
        remainingTimeMs = durationMs
        isRunning = true
        isPaused = false
        
        countDownTimer = object : CountDownTimer(durationMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                updateState()
            }
            
            override fun onFinish() {
                remainingTimeMs = 0
                isRunning = false
                isPaused = false
                updateState()
            }
        }.start()
        
        updateState()
    }
    
    fun pauseTimer() {
        countDownTimer?.cancel()
        isPaused = true
        isRunning = true
        updateState()
    }
    
    fun resumeTimer() {
        if (remainingTimeMs > 0) {
            isPaused = false
            isRunning = true
            
            countDownTimer = object : CountDownTimer(remainingTimeMs, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    remainingTimeMs = millisUntilFinished
                    updateState()
                }
                
                override fun onFinish() {
                    remainingTimeMs = 0
                    isRunning = false
                    isPaused = false
                    updateState()
                }
            }.start()
            
            updateState()
        }
    }
    
    fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        remainingTimeMs = 0
        totalTimeMs = 0
        isRunning = false
        isPaused = false
        
        IslandStateManager.processEvent(IslandEvent.TimerStop)
    }
    
    fun togglePause() {
        if (isPaused) {
            resumeTimer()
        } else {
            pauseTimer()
        }
    }
    
    private fun updateState() {
        val state = TimerState(
            remainingTimeMs = remainingTimeMs,
            totalTimeMs = totalTimeMs,
            isRunning = isRunning,
            isPaused = isPaused
        )
        IslandStateManager.processEvent(IslandEvent.TimerUpdate(state))
    }
    
    fun getFormattedTime(): String {
        val totalSeconds = remainingTimeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun release() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
}
