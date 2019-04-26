package com.paywithclerc.paywithclerc.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.paywithclerc.paywithclerc.R

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.annotation.KeepName
import com.google.firebase.ml.common.FirebaseMLException
import com.paywithclerc.paywithclerc.barcode.BarcodeScanningProcessor
import com.paywithclerc.paywithclerc.barcode.CameraSource
import kotlinx.android.synthetic.main.activity_barcode_scanner.*
import java.io.IOException

@KeepName
class BarcodeScannerActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var cameraSource: CameraSource? = null

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        // Check that the required views are set in XML
        if (barcodeCameraPreview == null) {
            Log.d(TAG, "Preview is null")
        }
        if (barcodeCameraOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        // Either ask for permissions or start running the camera
        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            getRuntimePermissions()
        }
    }

    /**
     * Initializes the camera & preview
     */
    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, barcodeCameraOverlay)
        }

        try {
            Log.i(TAG, "Using Barcode Detector Processor")
            val barcodeProcessor = BarcodeScanningProcessor { barcodes ->
                barcodes.forEach {
                    Log.e(TAG, it.rawValue.toString())
                }
            }
            cameraSource?.setMachineLearningFrameProcessor(barcodeProcessor)
        } catch (e: FirebaseMLException) {
            Log.e(TAG, "Cannot create camera source")
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        cameraSource?.let {
            try {
                if (barcodeCameraPreview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (barcodeCameraOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                barcodeCameraPreview?.start(cameraSource, barcodeCameraOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        barcodeCameraPreview?.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
    }

    /**
     * Permissions Methods
     */
    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    // Called when user answers a permissions request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        // Create the camera source if permissions are granted
        if (allPermissionsGranted()) {
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Static variables
    companion object {
        private const val TAG = "BarcodeScannerActivity"
        private const val PERMISSION_REQUESTS = 1

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}