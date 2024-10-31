package com.anvipus.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.Keep
import androidx.core.net.toUri
import com.anvipus.core.utils.Constants.Companion.EXTRA_RESULT
import com.anvipus.core.utils.model.OCRResultModel

@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.EXPRESSION
)
@MustBeDocumented
annotation class KeepDocumented

@Keep
object IdentifierOCR {
    var withFlash: Boolean? = null
    var cameraOnly: Boolean? = null
    var lowMemoryThreshold: Int? = null

    /**
     * Start capture
     * @param withFlash boolean value to show button flash or not (true to show or false to hide it). The default value is false
     * @param cameraOnly boolean value to show an activity camera only (without splash and confirmation screen) to get result OCR. The default value is false
     * @param lowMemoryThreshold Int value to set low memory threshold for show warning usage minimum alocation memory. The default value is 50 MB
     */
    @JvmStatic
    @KeepDocumented
    fun config(
        @KeepDocumented withFlash: Boolean? = false,
        @KeepDocumented cameraOnly: Boolean? = false,
        @KeepDocumented lowMemoryThreshold: Int? = 50
    ) {
        IdentifierOCR.withFlash = withFlash
        IdentifierOCR.cameraOnly = cameraOnly
        IdentifierOCR.lowMemoryThreshold = lowMemoryThreshold
    }

    /**
     * Start capture
     * @param intent an Intent data from activity result
     * @return an OCRResultModel
     */
    @JvmStatic
    @KeepDocumented
    fun getOCRResult(@KeepDocumented intent: Intent?): OCRResultModel? {
        return intent?.getParcelableExtra(EXTRA_RESULT) as OCRResultModel?
    }

    /**
     * Function to extract Data OCR from one image path.
     * @param imagePath list of image path
     * @param context an Context
     * @param listener an listener to listen on start and on finish process extract data.
     */
    @JvmStatic
    fun extractData(
        imagePath: String, context: Context,
        listener: ExtractDataOCRListener
    ) {
        val uriList = mutableListOf<Uri>()
        uriList.add(imagePath.toUri())

        extractDataFromUri(uriList, context, listener)
    }

    /**
     * Function to extract Data OCR from list of image path.
     * Use this function to get best result by using many options of input images.
     * @param imagePaths list of image path
     * @param context an Context
     * @param listener an listener to listen on start and on finish process extract data.
     */
    @JvmStatic
    fun extractData(
        imagePaths: List<String>,
        context: Context,
        listener: ExtractDataOCRListener
    ) {
        val uriList = mutableListOf<Uri>()
        imagePaths.forEach {
            uriList.add(it.toUri())
        }
        extractDataFromUri(uriList, context, listener)
    }

    @JvmStatic
    fun extractDataFromUri(
        uri: Uri,
        context: Context,
        listener: ExtractDataOCRListener
    ) {
        val extractDataOCR = ExtractDataOCR(context, listener)
        extractDataOCR.processExtractDataUri(listOf(uri))
    }

    @JvmStatic
    fun extractDataFromUri(
        uriList: List<Uri>,
        context: Context,
        listener: ExtractDataOCRListener
    ) {
        val extractDataOCR = ExtractDataOCR(context, listener)
        extractDataOCR.processExtractDataUri(uriList)
    }

    @JvmStatic
    fun extractDataFromBitmap(
        bitmapList: List<Bitmap?>,
        context: Context,
        listener: ExtractDataOCRListener
    ) {
        val extractDataOCR = ExtractDataOCR(context, listener)
        extractDataOCR.processExtractDataBitmap(bitmapList)
    }
}