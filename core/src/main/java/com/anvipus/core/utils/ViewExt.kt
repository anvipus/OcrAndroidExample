package com.anvipus.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.anvipus.core.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.imageview.ShapeableImageView

fun Activity.closeKeyboard() {
    var focus = currentFocus
    if (focus == null) {
        focus = View(this)
    }

    val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
    imm?.hideSoftInputFromWindow(focus.windowToken, 0)
}
fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide(type: Int = 1) {
    this.visibility = if (type == 1) View.GONE else View.INVISIBLE
}
fun View.isShowing(): Boolean = visibility == View.VISIBLE

fun View.showIf(show: Boolean?, type: Int = 1) {
    if (show == true) {
        show()
    } else {
        hide(type)
    }
}

fun ShapeableImageView.load(url: String?, placeholder: Int? = null, success: ((Boolean) -> Unit)? = null) {
    GlideApp.with(context).load(url)
        .placeholder(placeholder ?: R.drawable.ic_placeholder)
        .error(placeholder ?: R.drawable.ic_placeholder)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>, p3: Boolean): Boolean {
                success?.invoke(false)
                return false
            }

            override fun onResourceReady(
                p0: Drawable,
                p1: Any,
                p2: Target<Drawable>?,
                p3: DataSource,
                p4: Boolean
            ): Boolean {
                success?.invoke(true)
                return false
            }
        }).into(this)
}

fun ShapeableImageView.loadV2(url: String?, success: ((Boolean) -> Unit)? = null) {
    GlideApp.with(context).load(url)
        .placeholder(R.drawable.ic_placeholder)
        .error(R.drawable.ic_placeholder)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>, p3: Boolean): Boolean {
                success?.invoke(false)
                return false
            }

            override fun onResourceReady(
                p0: Drawable,
                p1: Any,
                p2: Target<Drawable>?,
                p3: DataSource,
                p4: Boolean
            ): Boolean {
                success?.invoke(true)
                return false
            }
        }).into(this)
}

fun ShapeableImageView.loadImageFromAsset(
    url: Int?,
    placeholder: Int? = null,
    success: ((Boolean) -> Unit)? = null
) {
    GlideApp.with(context).load(url)
        .placeholder(placeholder ?: R.drawable.ic_placeholder)
        .error(placeholder ?: R.drawable.ic_placeholder)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>, p3: Boolean): Boolean {
                success?.invoke(false)
                return false
            }

            override fun onResourceReady(
                p0: Drawable,
                p1: Any,
                p2: Target<Drawable>?,
                p3: DataSource,
                p4: Boolean
            ): Boolean {
                success?.invoke(true)
                return false
            }
        }).into(this)
}

fun Context.showDismissableErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    isShowBtnContinue: Boolean = false,
    onContinue: (() -> Unit)? = null
) {
    try {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_error, null)
        val errorMessageTextView = dialogView.findViewById<TextView>(R.id.errorMessageTextView)
        val dismissButton = dialogView.findViewById<Button>(R.id.dismissButton)
        val continueButton = dialogView.findViewById<Button>(R.id.continueButton)

        errorMessageTextView.text = message

        val errorDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        if(isShowBtnContinue && onContinue != null) {
            continueButton.setOnClickListener {
                onContinue()
                errorDialog.dismiss()
            }
        } else {
            continueButton.visibility = View.GONE
        }

        dismissButton.setOnClickListener {
            onDismiss()
            errorDialog.dismiss()
        }

        errorDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
