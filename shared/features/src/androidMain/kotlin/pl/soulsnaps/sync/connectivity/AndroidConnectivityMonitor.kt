package pl.soulsnaps.sync.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android connectivity monitor using ConnectivityManager
 */
class AndroidConnectivityMonitor(
    private val context: Context
) : ConnectivityMonitor {
    
    private val _connected = MutableStateFlow(false)
    override val connected: StateFlow<Boolean> = _connected.asStateFlow()
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connected.value = true
            println("DEBUG: AndroidConnectivityMonitor - network available")
        }
        
        override fun onLost(network: Network) {
            _connected.value = false
            println("DEBUG: AndroidConnectivityMonitor - network lost")
        }
    }
    
    override fun start() {
        println("DEBUG: AndroidConnectivityMonitor.start() - starting network monitoring")
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    override fun stop() {
        println("DEBUG: AndroidConnectivityMonitor.stop() - stopping network monitoring")
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
