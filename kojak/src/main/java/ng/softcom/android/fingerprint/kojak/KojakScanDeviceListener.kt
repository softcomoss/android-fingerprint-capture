package ng.softcom.android.fingerprint.kojak

import android.graphics.Bitmap
import com.integratedbiometrics.ibscanultimate.IBScanDevice
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener
import com.integratedbiometrics.ibscanultimate.IBScanException
import ng.softcom.android.fingerprint.core.Fingerprint
import ng.softcom.android.fingerprint.core.FingerprintDevice

/**
 * Callback implementation for the kojak scanner device sdk.
 */
class KojakScanDeviceListener(
    private val kojakBitmap: Bitmap,
    private val captureListener: FingerprintDevice.CaptureListener,
    private val onFinishedAction: (IBScanDevice) -> Unit
) : IBScanDeviceListener {

    /**
     * Not supported.
     */
    override fun deviceAcquisitionBegun(
        device: IBScanDevice?,
        imageType: IBScanDevice.ImageType?
    ) {
        /* Do nothing */
    }

    /**
     * Trigger error events.
     */
    override fun deviceCommunicationBroken(device: IBScanDevice) {
        captureListener.onDeviceCaptureFail("Unable to communicate with the device")
        onFinishedAction(device)
    }

    /**
     * Not supported.
     */
    override fun deviceWarningReceived(
        device: IBScanDevice?,
        exception: IBScanException?
    ) {
        /* Do nothing */
    }

    /**
     * Return image preview bitmap.
     */
    override fun deviceImagePreviewAvailable(
        device: IBScanDevice,
        image: IBScanDevice.ImageData
    ) {
        try {
            device.createBmpEx(image.buffer, kojakBitmap)
            captureListener.onImagePreviewCaptured(image.toBitmap())
        } catch (e: IBScanException) {
            captureListener.onDeviceCaptureFail(
                e.localizedMessage ?: "Unable to get image preview"
            )
        } catch (e: OutOfMemoryError) {
            captureListener.onDeviceCaptureFail(
                e.localizedMessage ?: "Device ran out of memory"
            )
        }
    }

    /**
     * Return final segmented fingerprints.
     */
    override fun deviceImageResultExtendedAvailable(
        device: IBScanDevice,
        imageStatus: IBScanException?,
        image: IBScanDevice.ImageData,
        imageType: IBScanDevice.ImageType,
        detectedFingerCount: Int,
        segmentImageArray: Array<IBScanDevice.ImageData>,
        segmentPositionArray: Array<IBScanDevice.SegmentPosition>
    ) {
        try {
            when (imageType) {
                IBScanDevice.ImageType.FLAT_TWO_FINGERS -> if (detectedFingerCount != 2) {
                    captureListener.onDeviceCaptureFail("Please use 2 fingers")
                    return
                }
                IBScanDevice.ImageType.FLAT_FOUR_FINGERS -> if (detectedFingerCount != 4) {
                    captureListener.onDeviceCaptureFail("Please use 4 fingers")
                    return
                }
                else -> {
                    /* Do nothing */
                }
            }
            image.toBitmap()
            device.createBmpEx(image.buffer, kojakBitmap)
            val fingerprints = segmentImageArray.map {
                Fingerprint(it.toBitmap(), device.calculateNfiqScore(it))
            }

            captureListener.onImageResultCaptured(fingerprints)
            onFinishedAction(device)
        } catch (e: IBScanException) {
            captureListener.onDeviceCaptureFail(
                e.localizedMessage ?: "Couldn't get the results"
            )
            onFinishedAction(device)
        } catch (e: OutOfMemoryError) {
            captureListener.onDeviceCaptureFail(
                e.localizedMessage ?: "Device ran out of memory"
            )
        }
    }

    /**
     * Not supported.
     */
    override fun deviceFingerQualityChanged(
        device: IBScanDevice?,
        fingerQualities: Array<out IBScanDevice.FingerQualityState>
    ) {
        // Do nothing.
    }

    /**
     * Not supported.
     */
    override fun deviceImageResultAvailable(
        device: IBScanDevice,
        image: IBScanDevice.ImageData,
        imageType: IBScanDevice.ImageType,
        splitImageArray: Array<out IBScanDevice.ImageData>
    ) {
        // Do nothing.
    }

    /**
     * Not supported.
     */
    override fun deviceFingerCountChanged(
        device: IBScanDevice?,
        fingerState: IBScanDevice.FingerCountState?
    ) {
        /* Do nothing */
    }

    /**
     * Not supported.
     */
    override fun deviceAcquisitionCompleted(
        device: IBScanDevice?,
        imageType: IBScanDevice.ImageType?
    ) {
        /* Do nothing */
    }

    /**
     * Not supported.
     */
    override fun devicePlatenStateChanged(
        device: IBScanDevice?,
        state: IBScanDevice.PlatenState?
    ) {
        /* Do nothing */
    }

    /**
     * Not supported.
     */
    override fun devicePressedKeyButtons(device: IBScanDevice?, key: Int) {
        /* Do nothing */
    }
}
