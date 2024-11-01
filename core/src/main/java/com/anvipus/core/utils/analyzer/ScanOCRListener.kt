package com.anvipus.core.utils.analyzer

import android.graphics.Bitmap
import com.anvipus.core.utils.model.KTPModel

interface ScanKtpListener {
    fun onStatusChanged(status: Status)
    fun onProgress(progress: Int)
    fun onScanComplete(ktpModel: KTPModel)
    fun onScanFailed(exception: Exception)
}

interface CaptureKtpListener {
    fun onStatusChanged(status: Status, croppedBitmap : Bitmap? = null)
    fun onCaptureFailed(exception: Exception)
}

enum class Status {
    NOT_READY, NOT_FOUND, SCANNING, COMPLETE
}