package akapps.photoediting.photocollage.activity


import akapps.photoediting.photocollage.adapter.GridImageAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import innovate.photo2Edit.R
import kotlinx.android.synthetic.main.activity_photo_collage.*

class PhotoCollageActivity : AppCompatActivity() {

    private lateinit var frames: Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_collage)
        init()
    }

    private fun init() {
        loadFrames()
        observeOnBackClickListener()
        val adapter = GridImageAdapter(this, frames)
        gridView.adapter = adapter
        gridView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val i = Intent(this@PhotoCollageActivity,
                    PhotoCollageEditActivity::class.java)
            i.putExtra(PhotoCollageEditActivity.IMAGE_ID, frames[position])
            startActivity(i)
        }
    }

    private fun observeOnBackClickListener() {
        iBClose.setOnClickListener {
            finish()
        }
    }

    private fun loadFrames() {
        frames = arrayOf(R.drawable.bg_photo_collage_frame3, R.drawable.bg_photo_collage_frame24,
                R.drawable.bg_photo_collage_frame15, R.drawable.bg_photo_collage_frame16, R.drawable.bg_photo_collage_frame17,
                R.drawable.bg_photo_collage_frame18, R.drawable.bg_photo_collage_frame19, R.drawable.bg_photo_collage_frame20, R.drawable.bg_photo_collage_frame22,
                R.drawable.bg_photo_collage_frame23,
                R.drawable.bg_photo_collage_frame21, R.drawable.bg_photo_collage_frame26,
                R.drawable.bg_photo_collage_frame11, R.drawable.bg_photo_collage_frame12,
                R.drawable.bg_photo_collage_frame1, R.drawable.bg_photo_collage_frame9, R.drawable.bg_photo_collage_frame10, R.drawable.bg_photo_collage_frame13,
                R.drawable.bg_photo_collage_frame14, R.drawable.bg_photo_collage_frame25, R.drawable.bg_photo_collage_frame27, R.drawable.bg_photo_collage_frame2,
                R.drawable.bg_photo_collage_frame4, R.drawable.bg_photo_collage_frame5, R.drawable.bg_photo_collage_frame6, R.drawable.bg_photo_collage_frame7,
                R.drawable.bg_photo_collage_frame8)
    }

}
