package akapps.photoediting.photowrap

import akapps.photoediting.extension.save
import akapps.photoediting.extension.share
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.github.dhaval2404.imagepicker.ImagePicker
import innovate.photo2Edit.R
import kotlinx.android.synthetic.main.activity_photo_wrap.*
import java.util.*
import kotlin.math.sqrt


class PhotoWrapActivity : AppCompatActivity() {


    private var meshView: SampleView? = null
    private var bitmapList: ArrayList<Bitmap>? = null
    private var bitmap: Bitmap? = null
    private var imageStackCount: Int = 0
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var brushMode = "pinIn"
    private var matrixSize = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_wrap)
        init()
    }

    private fun init() {

        if (brushMode == "wrap") {
            setWrapBackgroundSelected()
        } else if (brushMode == "pinOut") {
            setPinOutBackgroundSelected()

        } else {
            setPinInBackgroundSelected()
        }

        val rootView = window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        bitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_pic_holder)
                        meshView = SampleView(this@PhotoWrapActivity, bitmap, matrixSize, matrixSize)
                        container.addView(meshView)
                    }
                })

        observeOnBackClickListener()

    }

    private fun setWrapBackgroundSelected() {
        ibPinIn.setImageResource(R.drawable.ic_pin_in)
        ibPinOut.setImageResource(R.drawable.ic_pin_out)
        ibWrap.setImageResource(R.drawable.ic_wrap_pressed)
    }

    private fun setPinInBackgroundSelected() {
        ibPinIn.setImageResource(R.drawable.ic_pin_in_pressed)
        ibPinOut.setImageResource(R.drawable.ic_pin_out)
        ibWrap.setImageResource(R.drawable.ic_wrap)
    }

    private fun setPinOutBackgroundSelected() {
        ibPinIn.setImageResource(R.drawable.ic_pin_in)
        ibPinOut.setImageResource(R.drawable.ic_pin_out_pressed)
        ibWrap.setImageResource(R.drawable.ic_wrap)
    }


    fun saveImage(v: View) {

        save(container.drawToBitmap())

    }

    fun shareImage(v: View) {
        share(container.drawToBitmap())
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        try {
            if (resultCode == Activity.RESULT_OK) {

                val path = ImagePicker.getFilePath(intent)
                val bOptions = BitmapFactory.Options()
                bitmap = BitmapFactory.decodeFile(path, bOptions)
                container.removeView(meshView)
                meshView = SampleView(this, bitmap, matrixSize, matrixSize)
                container.addView(meshView)
                bitmapList?.clear()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onActivityResult(requestCode, resultCode, intent)
    }


    fun openGallery(v: View) {
        ImagePicker.with(this)
                .cropSquare()
                .galleryOnly()
                .start()
    }

    fun back(v: View) {

        if (bitmapList == null)
            return

        if (bitmapList!!.size > 1) {

            container.removeView(meshView)
            imageStackCount = bitmapList!!.size - 1


            val bitmap = if (bitmapList!!.size > 0) {
                bitmapList!![imageStackCount]
            } else {
                null
            }

            meshView = SampleView(this,
                    bitmap, matrixSize, matrixSize)
            bitmapList?.removeAt(imageStackCount)
            container.addView(meshView)
        } else {

            container.removeView(meshView)
            meshView = SampleView(this, bitmap, matrixSize, matrixSize)
            container.addView(meshView)

        }
    }

    fun wrap(v: View) {
        setWrapBackgroundSelected()

        brushMode = "wrap"

        try {
            if (bitmapList!!.size < 1)
                meshView = SampleView(this, bitmap, matrixSize,
                        matrixSize)
            container.addView(meshView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun pinIn(v: View) {

        setPinInBackgroundSelected()

        brushMode = "pinIn"
        matrixSize = 4
        try {
            if (bitmapList!!.size < 1)
                meshView = SampleView(this, bitmap, matrixSize,
                        matrixSize)


            container.addView(meshView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun observeOnBackClickListener() {
        iBClose.setOnClickListener {
            finish()
        }
    }

    fun pinOut(v: View) {
        setPinOutBackgroundSelected()
        brushMode = "pinOut"
        matrixSize = 8

        try {
            if (bitmapList!!.size < 1)
                meshView = SampleView(this, bitmap, matrixSize,
                        matrixSize)
            container.addView(meshView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun restart(v: View) {
        try {
            container.removeView(meshView)
            meshView = SampleView(this, bitmap, matrixSize, matrixSize)
            container.addView(meshView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        viewHeight = container.measuredHeight
        viewWidth = container.measuredWidth
    }


    inner class SampleView(context: Context, imageBitmap: Bitmap?, matrixWidth: Int, matrixHeight: Int) : View(context) {
        private var count: Int = 0
        private val mBitmap: Bitmap
        private var matrixWidth: Int = 0
        private var matrixHeight: Int = 0
        private val mVerts: FloatArray
        private var dst: FloatArray

        private val mMatrix = Matrix()
        private val mInverse = Matrix()

        private var mLastWarpX = -9999 // don't match a touch coordinate
        private var mLastWarpY = -9999

        private fun setXY(array: FloatArray, index: Int, x: Float, y: Float) {
            array[index * 2 + 0] = x
            array[index * 2 + 1] = y
        }

        init {

            this.matrixWidth = matrixWidth
            this.matrixHeight = matrixHeight

            isFocusable = true
            count = (matrixWidth + 1) * (matrixHeight + 1)
            mVerts = FloatArray(count * 2)

            mBitmap = Bitmap.createScaledBitmap(imageBitmap!!, container.width, container.height, true)

            val w = mBitmap.width.toFloat()
            val h = mBitmap.height.toFloat()

            var index = 0
            for (y in 0..matrixWidth) {
                val fy = h * y / matrixWidth
                for (x in 0..matrixHeight) {
                    val fx = w * x / matrixHeight
                    setXY(mVerts, index, fx, fy)
                    index += 1
                }
            }

            mMatrix.setTranslate(0f, 0f)

            dst = mVerts
        }


        override fun onDraw(canvas: Canvas) {
            canvas.concat(mMatrix)
            canvas.drawBitmapMesh(mBitmap, matrixWidth, matrixHeight, dst, 0, null, 0, null)
        }

        private fun warp(cx: Float, cy: Float) {

            val K = 2000f

            if (brushMode == "wrap") {

                var i = 0
                while (i < count * 2) {
                    val x = dst[i + 0]
                    val y = dst[i + 1]
                    val dx = cx - x
                    val dy = cy - y
                    val dd = dx * dx + dy * dy
                    val d = sqrt(dd.toDouble()).toFloat()
                    var pull = K / (dd + 0.000001f)

                    pull /= d + 0.000001f

                    if (pull >= 1) {
                        dst[i + 0] = cx
                        dst[i + 1] = cy
                    } else {

                        dst[i + 0] = x - dx * pull
                        dst[i + 1] = y - dy * pull
                    }
                    i += 2

                }

            } else if (brushMode == "pinOut") {

                var i = 0
                while (i < count * 2) {
                    val x = dst[i + 0]
                    val y = dst[i + 1]
                    val dx = cx - x
                    val dy = cy - y
                    val dd = dx * dx + dy * dy
                    val d = sqrt(dd.toDouble()).toFloat()
                    var pull = K / (dd + 0.000001f)

                    pull /= d + 0.000001f

                    if (pull >= 1) {
                        dst[i + 0] = cx
                        dst[i + 1] = cy
                    } else {

                        dst[i + 0] = x + dx * pull
                        dst[i + 1] = y + dy * pull
                    }
                    i += 2

                }

            } else {

                var i = 0
                while (i < count * 2) {
                    val x = dst[i + 0]
                    val y = dst[i + 1]
                    val dx = cx - x
                    val dy = cy - y
                    val dd = dx * dx + dy * dy
                    val d = sqrt(dd.toDouble()).toFloat()
                    var pull = K / (dd + 0.000001f)

                    pull /= d + 0.000001f

                    if (pull >= 1) {
                        dst[i + 0] = cx
                        dst[i + 1] = cy
                    } else {
                        dst[i + 0] = x + dx * pull
                        dst[i + 1] = y + dy * pull
                    }
                    i += 2

                }

            }

        }

        override fun onTouchEvent(event: MotionEvent): Boolean {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    val pt = floatArrayOf(event.x, event.y)
                    mInverse.mapPoints(pt)

                    val x = pt[0].toInt()
                    val y = pt[1].toInt()

                    if (mLastWarpX != x || mLastWarpY != y) {
                        mLastWarpX = x
                        mLastWarpY = y
                        warp(pt[0], pt[1])

                        invalidate()
                    }

                    return true
                }
                MotionEvent.ACTION_UP -> {

                    val bitmap = container.drawToBitmap()
                    if (bitmapList == null)
                        bitmapList = ArrayList()
                    bitmapList!!.add(bitmap)

                    return true
                }
                MotionEvent.ACTION_MOVE -> {

                    val pt1 = floatArrayOf(event.x, event.y)
                    mInverse.mapPoints(pt1)

                    val x1 = pt1[0].toInt()
                    val y1 = pt1[1].toInt()

                    if (mLastWarpX != x1 || mLastWarpY != y1) {
                        mLastWarpX = x1
                        mLastWarpY = y1
                        warp(pt1[0], pt1[1])
                        invalidate()
                    }
                    return true
                }
            }

            return true
        }
    }
}
