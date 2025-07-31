import SwiftUI

struct CameraPreviewRepresentable: UIViewRepresentable {
    func makeUIView(context: Context) -> CameraPreviewView {
        return CameraPreviewView()
    }
    func updateUIView(_ uiView: CameraPreviewView, context: Context) {}
} 