package akapps.photoediting.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import innovate.photo2Edit.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun Activity.share(bmp: Bitmap?) {
    try {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs() // don't forget to make the directory
        val stream = FileOutputStream("$cachePath/image.png")
        bmp?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val newFile = File(cachePath, "image.png")
        val contentUri = FileProvider.getUriForFile(this, "com.akapps.fileprovider", newFile)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        startActivity(Intent.createChooser(sharingIntent,
                "Share image using"))

    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun Activity.getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableId)
    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

fun Activity.getBitmapFromResource(drawableId: Int): Bitmap {
    return BitmapFactory.decodeResource(resources, drawableId)

}

@SuppressLint("InlinedApi")
@Suppress("DEPRECATION")
fun Activity.save(bmp: Bitmap) {
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name))
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {

            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                try {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            values.put(MediaStore.Images.Media.IS_PENDING, false)
            contentResolver.update(uri, values, null, null)

            Toast.makeText(this, "Image Saved...", Toast.LENGTH_LONG).show()
        }
    } else {
        val directory = File(Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.app_name))
        // getExternalStorageDirectory is deprecated in API 29

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = System.currentTimeMillis().toString() + ".png"
        val file = File(directory, fileName)


        val outputStream: OutputStream? = FileOutputStream(file)
        if (outputStream != null) {
            try {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!file.absolutePath.isNullOrEmpty()) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            Toast.makeText(this, "Image Saved...", Toast.LENGTH_LONG).show()
        }
    }
}

fun Activity.rate() {

    val uri = Uri.parse("market://details?id=$packageName")
    val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
        startActivity(myAppLinkToMarket)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}



