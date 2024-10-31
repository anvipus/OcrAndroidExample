package com.anvipus.core.utils.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import com.anvipus.core.common.Result
import com.anvipus.core.common.ResultErrorType
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.io.File

@Parcelize
data class OCRResultModel(
    override val isSuccess: Boolean,
    override val errorMessage: String?,
    override val errorType: ResultErrorType?,
    val imagePath: String? = null,
    val ktp: KTPModel
) : Result(), Parcelable {
    fun getBitmapImage(): Bitmap? {
        if (imagePath == null)
            return null
        val imgFile = File(imagePath)
        return BitmapFactory.decodeFile(imgFile.absolutePath)
    }

    fun toJson(): String {
        val mutableMap = mutableMapOf<String, Any?>()
        mutableMap["isSuccess"] = isSuccess
        mutableMap["errorMessage"] = errorMessage
        mutableMap["errorType"] = errorType.toString()
        mutableMap["imagePath"] = imagePath
        mutableMap["ktp"] = JSONObject(ktp.toJson())

        val json = JSONObject(mutableMap)

        return json.toString()
    }
}