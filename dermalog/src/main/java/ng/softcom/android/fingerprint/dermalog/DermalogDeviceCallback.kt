package ng.softcom.android.fingerprint.dermalog

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import com.dermalog.afis.imagecontainer.DICException
import com.dermalog.afis.imagecontainer.Decoder
import com.dermalog.afis.nistqualitycheck.Functions
import com.dermalog.afis.nistqualitycheck.NistQualityCheckException
import com.dermalog.biometricpassportsdk.Device
import com.dermalog.biometricpassportsdk.DeviceCallback
import com.dermalog.biometricpassportsdk.enums.CallbackEventId
import com.dermalog.biometricpassportsdk.utils.BitmapUtil
import com.dermalog.biometricpassportsdk.wrapped.DeviceCallbackEventArgument
import com.dermalog.biometricpassportsdk.wrapped.arguments.ErrorArgument
import com.dermalog.biometricpassportsdk.wrapped.arguments.FingerprintSegmentationArgument
import com.dermalog.biometricpassportsdk.wrapped.arguments.ImageArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ng.softcom.android.fingerprint.core.Fingerprint
import ng.softcom.android.fingerprint.core.FingerprintDevice
import java.io.IOException

/**
 * Callback implementation for the dermalog device sdk.
 */
class DermalogDeviceCallback(
    private val imageDecoder: Decoder,
    private val captureType: FingerprintDevice.CaptureType,
    private val captureListener: FingerprintDevice.CaptureListener
) : DeviceCallback {

    /**
     * Handle events.
     */
    override fun onCall(
        device: Device,
        deviceCallbackEventArgument: DeviceCallbackEventArgument
    ) {
        when (deviceCallbackEventArgument.eventId) {
            CallbackEventId.ERROR -> {
                deviceCallbackEventArgument.arguments.forEach {
                    if (it is ErrorArgument) {
                        captureListener.onDeviceCaptureFail(
                            it.message ?: "Unable to start capture."
                        )
                    }
                }
            }
            CallbackEventId.FINGER_IMAGE -> deviceCallbackEventArgument.arguments.forEach {
                if (it is ImageArgument) {
                    try {
                        captureListener.onImagePreviewCaptured(BitmapUtil.fromImageArgument(it))
                    } catch (e: IOException) {
                        captureListener.onDeviceCaptureFail("There was an error getting the fingerprint image preview.")
                    }
                }
            }
            CallbackEventId.FINGER_DETECT -> {
                var fingerprintSegmentationArgument: FingerprintSegmentationArgument? = null
                var imageArgument: ImageArgument? = null

                for (ea in deviceCallbackEventArgument.arguments) {
                    if (ea is FingerprintSegmentationArgument) {
                        fingerprintSegmentationArgument = ea
                    }
                    if (ea is ImageArgument) {
                        imageArgument = ea
                    }
                }

                try {
                    val bitmap = BitmapUtil.fromImageArgument(imageArgument)

                    // Create an image for each segment
                    val fingerprints =
                        extractFingerprints(bitmap, fingerprintSegmentationArgument!!)

                    val hasRightNumberOfFingers = when (captureType) {
                        FingerprintDevice.CaptureType.FINGERPRINT_CAPTURE_TWO -> {
                            if (fingerprints.size == 2) {
                                true
                            } else {
                                captureListener.onDeviceCaptureFail("Please use 2 fingers.")
                                false
                            }
                        }
                        FingerprintDevice.CaptureType.FINGERPRINT_CAPTURE_FOUR -> {
                            if (fingerprints.size == 4) {
                                true
                            } else {
                                captureListener.onDeviceCaptureFail("Please use 3 fingers.")
                                false
                            }
                        }
                    }

                    if (hasRightNumberOfFingers) {
                        captureListener.onImageResultCaptured(fingerprints)
                        device.stopCapture()
                    }
                } catch (e: IOException) {
                    captureListener.onDeviceCaptureFail("There was an error getting the fingerprint image result.")
                } catch (e: DICException) {
                    captureListener.onDeviceCaptureFail("Couldn't capture the fingerprint.")
                } catch (e: NistQualityCheckException) {
                    captureListener.onDeviceCaptureFail(e.message.toString())
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

    private fun extractFingerprints(
        sourceBitmap: Bitmap,
        segmentationArgument: FingerprintSegmentationArgument
    ): List<Fingerprint> {
        val segments = ArrayList<Fingerprint>()

        val targetRect = Rect().apply {
            left = 0
            top = 0
        }

        segmentationArgument.fingerprintSegments.forEach {
            val sourceRect = Rect().apply {
                left = it.positionTopLeft.x
                top = it.positionTopLeft.y
                bottom = it.positionBottomRight.y
                right = it.positionBottomRight.x
            }
            targetRect.run {
                bottom = sourceRect.bottom - sourceRect.top
                right = sourceRect.right - sourceRect.left
            }
            val bitmap = Bitmap.createBitmap(
                targetRect.width(),
                targetRect.height(),
                Bitmap.Config.ARGB_8888
            )
            Canvas(bitmap).run { drawBitmap(sourceBitmap, sourceRect, targetRect, null) }

            var nfiq = -1
            GlobalScope.launch(Dispatchers.Main) {
                val rawImage = imageDecoder.Decode(it.bitmapInfoHeaderData.rawData)
                val nfiq2 = Functions.CheckNfiq2(rawImage)
                nfiq = when {
                    nfiq2 >= 61 -> 1
                    nfiq2 >= 56 -> 2
                    nfiq2 >= 27 -> 3
                    nfiq2 >= 1 -> 4
                    else -> 5
                }
            }

            while (nfiq < 0) Thread.sleep(10)

            segments.add(Fingerprint(bitmap, nfiq))
        }

        return segments
    }
}
