package ng.softcom.android.fingerprint.kojak

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbManager
import com.integratedbiometrics.ibscanultimate.IBScan
import com.integratedbiometrics.ibscanultimate.IBScanDevice
import com.integratedbiometrics.ibscanultimate.IBScanException
import com.integratedbiometrics.ibscanultimate.IBScanListener
import ng.softcom.android.fingerprint.core.FingerprintDevice

/**
 * Implementation for Kojak Fingerprint Device.
 */
class KojakFingerprintDevice(private val context: Context) : FingerprintDevice {

    private var capturedKojakBitmap: Bitmap? = null

    private var scannerDevice: IBScanDevice? = null

    private val ibScan = IBScan.getInstance(context)

    override var deviceName: String = DEVICE_NAME

    private var kojakInitListener: FingerprintDevice.InitListener? = null

    override fun initializeScanner(initListener: FingerprintDevice.InitListener) {
        closeScanner()
        ibScan.setScanListener(object : IBScanListener {

            override fun scanDeviceAttached(deviceId: Int) {
                if (!ibScan.hasPermission(deviceId)) ibScan.requestPermission(deviceId)
            }

            override fun scanDeviceCountChanged(deviceCount: Int) {
                // Do nothing
            }

            override fun scanDevicePermissionGranted(deviceId: Int, granted: Boolean) {
                refreshScanner()
            }

            override fun scanDeviceInitProgress(deviceIndex: Int, progressValue: Int) {
                // Do nothing
            }

            override fun scanDeviceOpenComplete(
                deviceIndex: Int,
                device: IBScanDevice?,
                exception: IBScanException?
            ) {
                if (device != null) {
                    scannerDevice = device
                    setupDeviceForCapture(device)
                    kojakInitListener?.onDeviceOpened()
                    kojakInitListener = null
                }
            }

            override fun scanDeviceDetached(deviceId: Int) {
                /* Do nothing */
            }
        })
        refreshScanner()
    }

    /**
     * Refresh scanner device and re-initialize scanner.
     */
    fun refreshScanner() {
        if (ibScan.deviceCount > 0) {
            try {
                ibScan.openDeviceAsync(0)
            } catch (e: IBScanException) {
                kojakInitListener?.onDeviceInitFail(e.localizedMessage ?: "Unable to open device.")
                kojakInitListener = null
            }
        } else {
            var foundScannerDevice = false

            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            usbManager.deviceList.forEach {
                val usbDevice = it.value

                if (IBScan.isScanDevice(usbDevice)) {
                    foundScannerDevice = true
                    if (!usbManager.hasPermission(usbDevice)) {
                        ibScan.requestPermission(usbDevice.deviceId)
                    }
                }
            }
            if (!foundScannerDevice) {
                kojakInitListener?.onDeviceInitFail("No Scanner found")
                kojakInitListener = null
            }
        }
    }

    override fun closeScanner() {
        scannerDevice?.close()
        scannerDevice = null
    }

    private fun setupDeviceForCapture(device: IBScanDevice) {
        try {

            val deviceName = device.getProperty(IBScanDevice.PropertyId.PRODUCT_ID)

            if (deviceName == "KOJAK") {
                val imageKojakWidth =
                    device.getProperty(IBScanDevice.PropertyId.ROLLED_IMAGE_WIDTH).toInt()
                val imageKojakHeight =
                    device.getProperty(IBScanDevice.PropertyId.ROLLED_IMAGE_HEIGHT).toInt()
                capturedKojakBitmap =
                    Bitmap.createBitmap(imageKojakWidth, imageKojakHeight, Bitmap.Config.ARGB_8888)
            }
        } catch (e: IBScanException) {
            kojakInitListener?.onDeviceInitFail(e.localizedMessage ?: "Unable to setup device.")
            kojakInitListener = null
        }
    }

    override fun startCapture(
        captureType: FingerprintDevice.CaptureType,
        captureListener: FingerprintDevice.CaptureListener
    ) {
        val imageType = when (captureType) {
            FingerprintDevice.CaptureType.FINGERPRINT_CAPTURE_TWO -> IBScanDevice.ImageType.FLAT_TWO_FINGERS
            FingerprintDevice.CaptureType.FINGERPRINT_CAPTURE_FOUR -> IBScanDevice.ImageType.FLAT_FOUR_FINGERS
        }
        scannerDevice?.setScanDeviceListener(
            KojakScanDeviceListener(
                capturedKojakBitmap!!,
                captureListener,
                ::setupDeviceForCapture
            )
        )

        try {
            scannerDevice?.beginCaptureImage(
                imageType,
                IBScanDevice.ImageResolution.RESOLUTION_500,
                IBScanDevice.OPTION_AUTO_CAPTURE or IBScanDevice.OPTION_AUTO_CONTRAST
            )
        } catch (e: IBScanException) {
            captureListener.onDeviceCaptureFail(
                e.localizedMessage ?: "Unable to start capture"
            )
        }
    }

    companion object {
        private const val DEVICE_NAME = "Kojak"
    }
}
