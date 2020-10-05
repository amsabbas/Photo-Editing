package akapps.photoediting.photocollage.activity

import akapps.photoediting.base.model.ResourceState
import akapps.photoediting.extension.getBitmapFromVectorDrawable
import akapps.photoediting.extension.save
import akapps.photoediting.extension.share
import akapps.photoediting.photocollage.model.Coordinate
import akapps.photoediting.photocollage.model.Pixel
import akapps.photoediting.photocollage.viewmodel.PhotoCollageViewModel
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.github.dhaval2404.imagepicker.ImagePicker
import innovate.photo2Edit.R
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.activity_photo_collage_edit.*
import java.io.ByteArrayOutputStream
import java.util.*

class PhotoCollageEditActivity : AppCompatActivity() {

    private var bitmapImg: Bitmap? = null
    private var segments: ArrayList<Queue<Pixel>>? = null
    private var xySegments: ArrayList<Coordinate>? = null
    private var allImgViews: ArrayList<ImageView> = ArrayList()
    private var currIvPos = 0
    private var progress: Progress? = null
    private lateinit var viewModel: PhotoCollageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_collage_edit)
        init()
    }

    private fun observeOnBackClickListener() {
        iBClose.setOnClickListener {
            finish()
        }
    }

    private fun init() {

        val imgId = intent.extras?.getInt(IMAGE_ID)

        this@PhotoCollageEditActivity.progress = Progress(this@PhotoCollageEditActivity)
        progress?.setMessage("Please Wait...")

        val rootView = window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        imgId?.let {
                            val resBitmap = getBitmapFromVectorDrawable(imgId).copy(Bitmap.Config.ARGB_8888, true)
                            val resized = Bitmap.createScaledBitmap(resBitmap, rlParent.width, rlParent.width, true)
                            imageView.setImageBitmap(resized)
                            divideImage(resized)
                        }
                    }
                })

        btSave.setOnClickListener { saveImage() }
        btShare.setOnClickListener { shareImage() }
        observeOnBackClickListener()
    }

    private fun divideImage(bitmap: Bitmap) {
        viewModel = PhotoCollageViewModel()
        viewModel.divideLiveData.observe(this@PhotoCollageEditActivity, androidx.lifecycle.Observer {
            if (it.state == ResourceState.LOADING) {
                progress?.show()
            } else if (it.state == ResourceState.SUCCESS) {
                segments = viewModel.segments
                xySegments = viewModel.xySegments
                addImageViews()
                progress?.dismiss()
            } else if (it.state == ResourceState.ERROR) {
                progress?.dismiss()
            }
        })
        viewModel.divideImage(bitmap)
    }

    private fun addImageViews() {

        for (i in segments!!.indices) {
            val imageView = ImageView(this)
            imageView.id = i
            imageView.scaleType = ImageView.ScaleType.FIT_XY

            val params = RelativeLayout.LayoutParams(
                    xySegments!![i].maxX - xySegments!![i].minX,
                    xySegments!![i].maxY - xySegments!![i].minY)

            params.setMargins(xySegments!![i].minX, xySegments!![i].minY, 0, 0)
            imageView.layoutParams = params


            imageView.setOnClickListener {
                currIvPos = i
                selectImage()
            }

            allImgViews.add(imageView)
            rlParent.addView(imageView)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val path = ImagePicker.getFilePath(data)
            val bOptions = BitmapFactory.Options()
            bitmapImg = BitmapFactory.decodeFile(path, bOptions)
            val stream = ByteArrayOutputStream()
            bitmapImg!!.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            allImgViews[currIvPos].setImageBitmap(bitmapImg)
        }
    }

    private fun selectImage() {
        ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .start()
    }


    private fun saveImage() {
        rlParent.setBackgroundResource(R.drawable.bg_photo_collage_white)
        val bitmap = rlParent.drawToBitmap()
        rlParent.setBackgroundResource(0)
        save(bitmap)
    }

    private fun shareImage() {
        rlParent.setBackgroundResource(R.drawable.bg_photo_collage_white)
        val bitmap = rlParent.drawToBitmap()
        rlParent.setBackgroundResource(0)
        share(bitmap)
    }

    companion object {
        const val IMAGE_ID = "img_id"
    }


}
