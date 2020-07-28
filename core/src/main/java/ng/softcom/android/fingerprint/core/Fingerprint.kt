package ng.softcom.android.fingerprint.core

import android.graphics.Bitmap

/**
 * Data representation of a fingerprint.
 */
data class Fingerprint(
    /**
     * Fingerprint image.
     */
    val bitmap: Bitmap,
    /**
     * NIST fingerprint quality score.
     */
    val nfiqScore: Int
)
