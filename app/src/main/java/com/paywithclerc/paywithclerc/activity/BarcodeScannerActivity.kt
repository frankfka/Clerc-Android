package com.paywithclerc.paywithclerc.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.content.Intent
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
import android.app.Activity
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants


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

    /**
     * Initialization for the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        // Either ask for permissions or start running the camera
        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            getRuntimePermissions()
        }
    }

    /**
     * Ends the current activity with the scanned barcode if one was scanned
     * If the returned barcode is nil, then an error has occurred
     */
    private fun returnToPreviousActivity(barcode: String?) {
        val returnIntent = Intent()
        returnIntent.putExtra(ActivityConstants.BARCODE_EXTRA_KEY, barcode)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    /**
     * Initializes the camera & preview
     *
     * This is the main function that deals with the logic of passing back
     * the scanned barcodes to the previous activity
     */
    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, barcodeCameraOverlay)
        }
        // Camera source should exist at this point
        try {
            Log.i(TAG, "Using Barcode Detector Processor")
            val barcodeProcessor = BarcodeScanningProcessor { barcodes ->
                // Barcodes is never empty, we just retrieve the first barcode
                // and pass it back to the calling activity
                val barcode = barcodes[0].rawValue!! // TODO will this ever be null?
                returnToPreviousActivity(barcode)
            }
            cameraSource?.setMachineLearningFrameProcessor(barcodeProcessor)
        } catch (e: FirebaseMLException) {
            Log.e(TAG, "Cannot create camera source")
            returnToPreviousActivity(barcode = null)
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
                returnToPreviousActivity(barcode = null)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resuming barcode scanner activity")
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
        private const val TAG = "PAYWITHCLERCAPP: BarcodeScannerActivity"
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