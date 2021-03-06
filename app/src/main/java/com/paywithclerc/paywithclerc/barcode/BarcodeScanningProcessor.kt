package com.paywithclerc.paywithclerc.barcode

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.io.IOException

/**
 * Barcode scanner class initialized with a callback function that is called when barcodes are detected
 */
class BarcodeScanningProcessor(var barcodeDetected: (List<FirebaseVisionBarcode>) -> Unit) : VisionProcessorBase<List<FirebaseVisionBarcode>>() {

    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one, e.g.
    // FirebaseVisionBarcodeDetectorOptions.Builder()
    //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
    //     .build()
    private val detector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance().visionBarcodeDetector
    }

    override fun stop() {
        try {
            detector.close()
            Log.i(TAG, "Detector stopped")
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        barcodes: List<FirebaseVisionBarcode>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {

        // If any barcodes were found - pass them to the callback function
        if (barcodes.count() > 0) {
            barcodeDetected(barcodes)
        }

        graphicOverlay.clear()

        originalCameraImage?.let {
            val imageGraphic = CameraImageGraphic(graphicOverlay, it)
            graphicOverlay.add(imageGraphic)
        }

        // Don't show barcode overlay
//        barcodes.forEach {
//            val barcodeGraphic = BarcodeGraphic(graphicOverlay, it)
//            graphicOverlay.add(barcodeGraphic)
//        }

        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed $e")
    }

    companion object {
        private const val TAG = "PAYWITHCLERCAPP: BarcodeScanProc"
    }
}