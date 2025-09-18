import Foundation
import Network
import shared

/**
 * iOS Network Monitor using Network framework
 */
class NetworkMonitor: ObservableObject {
    
    private let networkMonitor = NWPathMonitor()
    private let workerQueue = DispatchQueue(label: "NetworkMonitor")
    
    @Published var isOnline = false
    
    private var syncManager: IOSSyncManager?
    
    init() {
        startMonitoring()
    }
    
    deinit {
        stopMonitoring()
    }
    
    /**
     * Set sync manager for automatic sync triggering
     */
    func setSyncManager(_ syncManager: IOSSyncManager) {
        self.syncManager = syncManager
    }
    
    /**
     * Start monitoring network status
     */
    private func startMonitoring() {
        print("DEBUG: NetworkMonitor.startMonitoring() - starting network monitoring")
        
        networkMonitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                let wasOnline = self?.isOnline ?? false
                self?.isOnline = path.status == .satisfied
                
                if wasOnline != self?.isOnline {
                    print("DEBUG: NetworkMonitor.startMonitoring() - network status changed: \(self?.isOnline ?? false)")
                    
                    // Trigger sync when coming back online
                    if self?.isOnline == true && wasOnline == false {
                        print("DEBUG: NetworkMonitor.startMonitoring() - network back online, triggering sync")
                        self?.syncManager?.enqueuePendingSync()
                    }
                }
            }
        }
        
        networkMonitor.start(queue: workerQueue)
    }
    
    /**
     * Stop monitoring network status
     */
    private func stopMonitoring() {
        print("DEBUG: NetworkMonitor.stopMonitoring() - stopping network monitoring")
        networkMonitor.cancel()
    }
    
    /**
     * Get current network status
     */
    func getNetworkStatus() -> Bool {
        return isOnline
    }
}
