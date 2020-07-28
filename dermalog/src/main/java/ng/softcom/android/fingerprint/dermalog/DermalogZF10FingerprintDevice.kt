package ng.softcom.android.fingerprint.dermalog

import android.content.Context
import com.dermalog.afis.imagecontainer.Decoder
import com.dermalog.biometricpassportsdk.BiometricPassportException
import com.dermalog.biometricpassportsdk.BiometricPassportSdkAndroid
import com.dermalog.biometricpassportsdk.Device
import com.dermalog.biometricpassportsdk.enums.DeviceId
import com.dermalog.biometricpassportsdk.usb.permission.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ng.softcom.android.fingerprint.core.FingerprintDevice

/**
 * Implementation for Dermalog ZF10 Fingerprint Device.
 */
class DermalogZF10FingerprintDevice(context: Context) : FingerprintDevice {

    private val imageDecoder = Decoder()

    override val deviceName: String = DEVICE_NAME

    private var scannerDevice: Device? = null
    private val biometricSdk = BiometricPassportSdkAndroid(context)

    private var dermalogInitListener: FingerprintDevice.InitListener? = null

    override fun initializeScanner(initListener: FingerprintDevice.InitListener) {
        closeScanner()
        dermalogInitListener = initListener

        GlobalScope.launch(Dispatchers.Default) {
            try {
                requestPermission()
            } catch (e: BiometricPassportException) {
                dermalogInitListener?.onDeviceInitFail("Couldn't initialize device correctly.")
                dermalogInitListener = null
            }
        }
    }

    private fun openDevice() {
        try {
            val deviceInfoArr = biometricSdk.enumerateDevices(DeviceId.ALL)
            if (deviceInfoArr.isNotEmpty()) {
                scannerDevice = biometricSdk.createDevice(deviceInfoArr[0])
                dermalogInitListener?.onDeviceOpened()
                dermalogInitListener = null
            } else {
                dermalogInitListener?.onDeviceInitFail("No scanner could be found.")
                dermalogInitListener = null
            }
        } catch (e: BiometricPassportException) {
            dermalogInitListener?.onDeviceInitFail("Couldn't initialize device correctly.")
            dermalogInitListener = null
        }
    }

    private fun requestPermission() {
        biometricSdk.requestUSBPermissionsAsync {
            when (it.result) {
                ResultCode.Success -> {
                    openDevice()
                }
                ResultCode.PartialPermission -> {
                    dermalogInitListener?.onDeviceInitFail("Not all permissions were granted.")
                    dermalogInitListener = null
                }
                ResultCode.UsbNotSupported -> {
                    dermalogInitListener?.onDeviceInitFail("The USB device found isn't supported")
                    dermalogInitListener = null
                }
                ResultCode.NoDevice -> {
                    dermalogInitListener?.onDeviceInitFail("No USB devices were found.")
                    dermalogInitListener = null
                }
                ResultCode.NoPermission -> {
                    dermalogInitListener?.onDeviceInitFail("No permissions were granted.")
                    dermalogInitListener = null
                }
                else -> {
                    dermalogInitListener?.onDeviceInitFail("Couldn't initialize device correctly.")
                    dermalogInitListener = null
                }
            }
        }
    }

    override fun closeScanner() {
        scannerDevice?.run {
            stopCapture()
            dispose()
        }
        biometricSdk.dispose()
    }

    override fun startCapture(
        captureType: FingerprintDevice.CaptureType,
        captureListener: FingerprintDevice.CaptureListener
    ) {
        scannerDevice?.registerCallback(
            DermalogDeviceCallback(
                imageDecoder,
                captureType,
                captureListener
            )
        )

        scannerDevice?.startCapture()
    }

    companion object {
        private const val DEVICE_NAME = "Dermalog ZF10"
    }
}
