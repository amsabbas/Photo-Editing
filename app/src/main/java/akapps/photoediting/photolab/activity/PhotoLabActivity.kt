package akapps.photoediting.photolab.activity

import akapps.photoediting.extension.getBitmapFromResource
import akapps.photoediting.extension.save
import akapps.photoediting.extension.share
import akapps.photoediting.photolab.adapter.PhotoFiltersAdapter
import akapps.photoediting.photolab.adapter.PhotoFramesAdapter
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import innovate.photo2Edit.R
import kotlinx.android.synthetic.main.activity_photo_lab.*
import kotlinx.android.synthetic.main.item_photo_lab_adjust.*
import kotlinx.coroutines.*


class PhotoLabActivity : AppCompatActivity(), CoroutineScope by MainScope(), SeekBar.OnSeekBarChangeListener {

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapWithFilter: Bitmap

    private var job : Job? = null


    private enum class PhotoType {
        FRAMES, FILTERS, ADJUST
    }

    private var selectedButton: PhotoType = PhotoType.FRAMES


    private lateinit var photoFiltersAdapter: PhotoFiltersAdapter


    init {
        System.loadLibrary("NativeImageProcessor")
    }

    private val frames: IntArray = intArrayOf(R.drawable.bg_pic_holder, R.drawable.bg_custom_shape1, R.drawable.bg_custom_shape2, R.drawable.bg_custom_shape3,
            R.drawable.bg_custom_shape4, R.drawable.bg_custom_shape5, R.drawable.bg_custom_shape6, R.drawable.bg_custom_shape7, R.drawable.bg_custom_shape8,
            R.drawable.bg_custom_shape9, R.drawable.bg_custom_shape10, R.drawable.bg_custom_shape11, R.drawable.bg_custom_shape12, R.drawable.bg_custom_shape13,
            R.drawable.bg_custom_shape14, R.drawable.bg_custom_shape15, R.drawable.bg_custom_shape16, R.drawable.bg_custom_shape17, R.drawable.bg_custom_shape18,
            R.drawable.bg_custom_shape19, R.drawable.bg_custom_shape20, R.drawable.bg_custom_shape21, R.drawable.bg_custom_shape22, R.drawable.bg_custom_shape23,
            R.drawable.bg_custom_shape24, R.drawable.bg_custom_shape25, R.drawable.bg_custom_shape26, R.drawable.bg_custom_shape27, R.drawable.bg_custom_shape28,
            R.drawable.bg_custom_shape29, R.drawable.bg_custom_shape30, R.drawable.bg_custom_shape31, R.drawable.bg_custom_shape32, R.drawable.bg_custom_shape32,
            R.drawable.bg_custom_shape33, R.drawable.bg_custom_shape34, R.drawable.bg_custom_shape35, R.drawable.bg_custom_shape36, R.drawable.bg_custom_shape37,
            R.drawable.bg_custom_shape38, R.drawable.bg_custom_shape39, R.drawable.bg_custom_shape40, R.drawable.bg_custom_shape41, R.drawable.bg_custom_shape42,
            R.drawable.bg_custom_shape42, R.drawable.bg_custom_shape43, R.drawable.bg_custom_shape44, R.drawable.bg_custom_shape45, R.drawable.bg_custom_shape46,
            R.drawable.bg_custom_shape47, R.drawable.bg_custom_shape48, R.drawable.bg_custom_shape49, R.drawable.bg_custom_shape50, R.drawable.bg_custom_shape51,
            R.drawable.bg_custom_shape52, R.drawable.bg_custom_shape53, R.drawable.bg_custom_shape54, R.drawable.bg_custom_shape55, R.drawable.bg_custom_shape56,
            R.drawable.bg_custom_shape57, R.drawable.bg_custom_shape57, R.drawable.bg_custom_shape58, R.drawable.bg_custom_shape59, R.drawable.bg_custom_shape60,
            R.drawable.bg_custom_shape61, R.drawable.bg_custom_shape62, R.drawable.bg_custom_shape63, R.drawable.bg_custom_shape64, R.drawable.bg_custom_shape65,
            R.drawable.bg_custom_shape66, R.drawable.bg_custom_shape67, R.drawable.bg_custom_shape68, R.drawable.bg_custom_shape69, R.drawable.bg_custom_shape70,
            R.drawable.bg_custom_shape71, R.drawable.bg_custom_shape72, R.drawable.bg_custom_shape73, R.drawable.bg_custom_shape74, R.drawable.bg_custom_shape75,
            R.drawable.bg_custom_shape76, R.drawable.bg_custom_shape77, R.drawable.bg_custom_shape78, R.drawable.bg_custom_shape79, R.drawable.bg_custom_shape80,
            R.drawable.bg_custom_shape81, R.drawable.bg_custom_shape82, R.drawable.bg_custom_shape83, R.drawable.bg_custom_shape84, R.drawable.bg_custom_shape85,
            R.drawable.bg_custom_shape86, R.drawable.bg_custom_shape87, R.drawable.bg_custom_shape88, R.drawable.bg_custom_shape89, R.drawable.bg_custom_shape90,
            R.drawable.bg_custom_shape91, R.drawable.bg_custom_shape92, R.drawable.bg_custom_shape93, R.drawable.bg_custom_shape94, R.drawable.bg_custom_shape95,
            R.drawable.bg_custom_shape96, R.drawable.bg_custom_shape97, R.drawable.bg_custom_shape98, R.drawable.bg_custom_shape99)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_lab)
        init()
    }

    private fun init() {
        rvFilters.visibility = View.INVISIBLE
        rvFrames.visibility = View.INVISIBLE

        sbBrightness.max = 200
        sbBrightness.progress = 100

        // keeping contrast value b/w 1.0 - 3.0
        sbContrast.max = 20
        sbContrast.progress = 0

        // keeping saturation value b/w 0.0 - 3.0
        sbSaturation.max = 30
        sbSaturation.progress = 10

        sbBrightness.setOnSeekBarChangeListener(this)
        sbContrast.setOnSeekBarChangeListener(this)
        sbSaturation.setOnSeekBarChangeListener(this)

        bitmap = getBitmapFromResource(R.drawable.bg_pic_holder)
        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, false)
        bitmapWithFilter = Bitmap.createBitmap(bitmap)
        imgLab.setImageBitmap(bitmap)
        observeOnGalleryClickListener()
        observeOnBackClickListener()
        observeOnSaveClickListener()
        observeOnShareClickListener()
        observeOnFramesClickListener()
        observeOnAdjustClickListener()
        observeOnFilterClickListener()
        toggle()
    }

    private fun toggle() {
        if (selectedButton == PhotoType.FRAMES) {
            initFrames()
        } else if (selectedButton == PhotoType.FILTERS) {
            initFilters()
        } else if (selectedButton == PhotoType.ADJUST) {
            initAdjust()
        }
    }

    private fun initAdjust() {

        job?.cancel()

        btAdjust.setTextColor(ContextCompat.getColor(this, R.color.green))
        btAdjust.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_adjust_pressed),
                null, null, null)
        btAdjust.setTypeface(null, Typeface.BOLD)

        btFrames.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        btFrames.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_frame),
                null, null, null)
        btFrames.setTypeface(null, Typeface.NORMAL)

        btFilter.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        btFilter.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_filter),
                null, null, null)
        btFilter.setTypeface(null, Typeface.NORMAL)

        if (rvFilters.visibility != View.INVISIBLE)
            rvFilters.visibility = View.GONE
        rvFrames.visibility = View.GONE
        llAdjust.visibility = View.VISIBLE

    }


    private fun initFrames() {

        job?.cancel()

        btFrames.setTextColor(ContextCompat.getColor(this, R.color.green))
        btFrames.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_frame_pressed),
                null, null, null)
        btFrames.setTypeface(null, Typeface.BOLD)

        btFilter.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        btFilter.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_filter),
                null, null, null)
        btFilter.setTypeface(null, Typeface.NORMAL)

        btAdjust.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        btAdjust.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_adjust),
                null, null, null)
        btAdjust.setTypeface(null, Typeface.NORMAL)


        if (rvFrames.visibility == View.INVISIBLE) {
            val adapter = PhotoFramesAdapter(frames, object : PhotoFramesAdapter.Callback {
                override fun onItemClicked(position: Int) {
                    if (position > 0)
                        imgLabBackground.setImageResource(frames[position])
                    else {
                        imgLabBackground.setImageResource(0)
                    }
                }
            })

            rvFrames.adapter = adapter
            rvFrames.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvFrames.visibility = View.VISIBLE
            rvFilters.visibility = View.INVISIBLE
        } else {
            rvFilters.visibility = View.GONE
            rvFrames.visibility = View.VISIBLE
        }
        llAdjust.visibility = View.GONE
    }

    private fun initFilters() {

        job?.cancel()

        btFilter.setTextColor(ContextCompat.getColor(this, R.color.green))
        btFilter.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_filter_pressed),
                null, null, null)
        btFilter.setTypeface(null, Typeface.BOLD)

        btFrames.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        btFrames.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_frame),
                null, null, null)
        btFrames.setTypeface(null, Typeface.NORMAL)

        btAdjust.setTextColor(ContextCompat.getColor(this, R.color.gray_light))
        btAdjust.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_adjust),
                null, null, null)
        btAdjust.setTypeface(null, Typeface.NORMAL)

        if (rvFilters.visibility == View.INVISIBLE) {

            job = launch {
                val thumbList = withContext(Dispatchers.Default) {
                    initThumb()
                }

                photoFiltersAdapter = PhotoFiltersAdapter(thumbList, object : PhotoFiltersAdapter.Callback {
                    override fun onItemClicked(item: ThumbnailItem) {
                        if (item.filter != null) {
                            bitmapWithFilter = Bitmap.createBitmap(bitmap)
                            imgLab.setImageBitmap(item.filter.processFilter(bitmapWithFilter))
                        } else {
                            imgLab.setImageBitmap(bitmap)
                        }
                    }
                })

                rvFilters.adapter = photoFiltersAdapter
                rvFilters.layoutManager = LinearLayoutManager(this@PhotoLabActivity, LinearLayoutManager.HORIZONTAL, false)
                rvFilters.visibility = View.VISIBLE
                rvFrames.visibility = View.GONE
            }
        } else {
            rvFilters.visibility = View.VISIBLE
            rvFrames.visibility = View.GONE
        }
        llAdjust.visibility = View.GONE

    }


    private fun initThumb(): List<ThumbnailItem> {
        val filters = FilterPack.getFilterPack(this)
        ThumbnailsManager.clearThumbs()
        for (filter in filters) {
            val item = ThumbnailItem()
            item.image = getBitmapFromResource(R.drawable.bg_pic_holder)
            item.filter = filter
            item.filterName = filter.name
            ThumbnailsManager.addThumb(item)
        }

        val thumbnails = (ThumbnailsManager.processThumbs(this))

        val item = ThumbnailItem()
        item.image = getBitmapFromResource(R.drawable.bg_pic_holder)
        ThumbnailsManager.addThumb(item)
        thumbnails.add(0, item)
        return thumbnails
    }

    private fun observeOnGalleryClickListener() {
        ibPGallery.setOnClickListener {
            ImagePicker.with(this)
                    .cropSquare()
                    .galleryOnly()
                    .start()
        }
    }

    private fun observeOnBackClickListener() {
        iBClose.setOnClickListener {
            onBackPressed()
        }
    }

    private fun observeOnFilterClickListener() {
        btFilter.setOnClickListener {
            selectedButton = PhotoType.FILTERS
            toggle()
        }
    }

    private fun observeOnFramesClickListener() {
        btFrames.setOnClickListener {
            selectedButton = PhotoType.FRAMES
            toggle()
        }
    }

    private fun observeOnAdjustClickListener() {
        btAdjust.setOnClickListener {
            selectedButton = PhotoType.ADJUST
            toggle()
        }
    }


    private fun observeOnSaveClickListener() {
        ibSave.setOnClickListener {
            saveImage()
        }
    }

    private fun observeOnShareClickListener() {
        ibShare.setOnClickListener {
            shareImage()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                imgLab.setImageURI(intent?.data)
                bitmap = (imgLab.drawable as BitmapDrawable).bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, false)
                bitmapWithFilter = Bitmap.createBitmap(bitmap)
                if (::photoFiltersAdapter.isInitialized) {
                    photoFiltersAdapter.notifyDataSetChanged()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun shareImage() {
        share(rlImage.drawToBitmap())
    }

    private fun saveImage() {
        save(rlImage.drawToBitmap())
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {

        seekBar?.let { bar ->
            if (bar.id == R.id.sbBrightness) {
                // brightness values are b/w -100 to +100
                onBrightnessChanged(progress - 100)
            }

            if (bar.id == R.id.sbContrast) {
                // converting int value to float
                // contrast values are b/w 1.0f - 3.0f
                // progress = progress > 10 ? progress : 10;
                var pg = progress
                pg += 10
                val floatVal = .10f * pg
                onContrastChanged(floatVal)
            }

            if (bar.id == R.id.sbSaturation) {
                // converting int value to float
                // saturation values are b/w 0.0f - 3.0f
                val floatVal = .10f * progress
                onSaturationChanged(floatVal)
            }
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }

    private fun onBrightnessChanged(brightness: Int) {
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightness))
        imgLab.setImageBitmap(myFilter.processFilter(bitmapWithFilter.copy(Bitmap.Config.ARGB_8888, true)))
    }

    private fun onSaturationChanged(saturation: Float) {
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubfilter(saturation))
        imgLab.setImageBitmap(myFilter.processFilter(bitmapWithFilter.copy(Bitmap.Config.ARGB_8888, true)))
    }

    private fun onContrastChanged(contrast: Float) {
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(contrast))
        imgLab.setImageBitmap(myFilter.processFilter(bitmapWithFilter.copy(Bitmap.Config.ARGB_8888, true)))
    }

}