package ng.softcom.android.fingerprint.core

import android.graphics.Bitmap

/**
 * Base fingerprint scanner device for capturing fingerprints.
 */
interface FingerprintDevice {

    /**
     * The name of the fingerprint device.
     */
    val deviceName: String

    /**
     * Initialize the scanner device.
     *
     * @param initListener registered listener for getting callbacks for the initialization process.
     */
    fun initializeScanner(initListener: InitListener)

    /**
     * Close the scanner device
     */
    fun closeScanner()

    /**
     * Start capturing fingerprints.
     *
     * @param captureListener registered listener for getting callbacks for the capture process.
     */
    fun startCapture(captureType: CaptureType, captureListener: CaptureListener)

    /**
     * Enumeration of different type of fingerprint capture
     */
    enum class CaptureType {

        /**
         * Capture two fingerprints. Usually thumbs.
         */
        FINGERPRINT_CAPTURE_TWO,

        /**
         * Capture four fingerprints.
         */
        FINGERPRINT_CAPTURE_FOUR
    }

    /**
     * Interface for getting callbacks from the device initialization process externally.
     */
    interface InitListener {

        /**
         * Callback for if the scanner failed to initialize properly.
         */
        fun onDeviceInitFail(reason: String)

        /**
         * Callback for when the device is initialized and opened.
         */
        fun onDeviceOpened()
    }

    /**
     * Interface for getting callbacks from the capture process externally.
     */
    interface CaptureListener {

        /**
         * Callback for if the device failed to capture fingerprints properly.
         */
        fun onDeviceCaptureFail(reason: String)

        /**
         * Callback for when a preview of the image from the scanner is available.
         */
        fun onImagePreviewCaptured(bitmap: Bitmap)

        /**
         * Callback for when a full result of the fingerprint has been captured.
         */
        fun onImageResultCaptured(segmentedFingerprints: List<Fingerprint>)
    }
}
