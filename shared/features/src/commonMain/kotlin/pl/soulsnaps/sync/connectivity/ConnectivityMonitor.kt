package pl.soulsnaps.sync.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Connectivity monitor interface
 */
interface ConnectivityMonitor {
    val connected: StateFlow<Boolean>
    fun start()
    fun stop()
}

/**
 * Simple connectivity monitor implementation
 */
class SimpleConnectivityMonitor : ConnectivityMonitor {
    
    private val _connected = MutableStateFlow(true) // Assume connected by default
    override val connected: StateFlow<Boolean> = _connected.asStateFlow()
    
    private var isStarted = false
    
    override fun start() {
        if (isStarted) return
        isStarted = true
        println("DEBUG: SimpleConnectivityMonitor.start() - started monitoring")
        
        // This is a fallback implementation
        // Platform-specific implementations should be used instead:
        // - Android: AndroidConnectivityMonitor
        // - iOS: IOSConnectivityMonitor
        _connected.value = true // Assume connected by default
    }
    
    override fun stop() {
        if (!isStarted) return
        isStarted = false
        println("DEBUG: SimpleConnectivityMonitor.stop() - stopped monitoring")
    }
    
    /**
     * Set connectivity status (called from platform-specific code)
     */
    fun setConnected(connected: Boolean) {
        val previousStatus = _connected.value
        _connected.value = connected
        
        if (previousStatus != connected) {
            println("DEBUG: SimpleConnectivityMonitor.setConnected() - connectivity changed: $connected")
        }
    }
}

/**
 * Mock implementation for testing
 */
class MockConnectivityMonitor(
    initialConnected: Boolean = true
) : ConnectivityMonitor {
    
    private val _connected = MutableStateFlow(initialConnected)
    override val connected: StateFlow<Boolean> = _connected.asStateFlow()
    
    override fun start() {
        println("DEBUG: MockConnectivityMonitor.start() - mock monitoring started")
    }
    
    override fun stop() {
        println("DEBUG: MockConnectivityMonitor.stop() - mock monitoring stopped")
    }
    
    fun setConnected(connected: Boolean) {
        _connected.value = connected
        println("DEBUG: MockConnectivityMonitor.setConnected() - mock connectivity: $connected")
    }
}
