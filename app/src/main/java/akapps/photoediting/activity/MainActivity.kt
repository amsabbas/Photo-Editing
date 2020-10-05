package akapps.photoediting.activity


import akapps.photoediting.extension.rate
import akapps.photoediting.photocollage.activity.PhotoCollageActivity
import akapps.photoediting.photolab.activity.PhotoLabActivity
import akapps.photoediting.photowrap.PhotoWrapActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import innovate.photo2Edit.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
        observeOnPhotoCollageClickListener()
        observeOnPhotoWrapClickListener()
        observeOnPhotoEffectsClickListener()
        observeOnRateClickListener()
    }

    @SuppressLint("CheckResult")
    private fun requestPermissions() {

        val rxPermissions = RxPermissions(this)
        rxPermissions.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).subscribe { granted ->
            if (!granted) {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        llPhotoWrap.isEnabled = true
        llPhotoCollage.isEnabled = true
        llPhotoEffects.isEnabled = true
    }

    private fun observeOnPhotoWrapClickListener() {
        llPhotoWrap.setOnClickListener {
            llPhotoWrap.isEnabled = false
            startActivity(Intent(this@MainActivity, PhotoWrapActivity::class.java))
        }
    }


    private fun observeOnPhotoCollageClickListener() {

        llPhotoCollage.setOnClickListener {
            llPhotoCollage.isEnabled = false
            startActivity(Intent(this@MainActivity, PhotoCollageActivity::class.java))
        }
    }

    private fun observeOnPhotoEffectsClickListener() {
        llPhotoEffects.setOnClickListener {
            llPhotoEffects.isEnabled = false
            startActivity(Intent(this@MainActivity, PhotoLabActivity::class.java))
        }
    }

    private fun observeOnRateClickListener() {
        llRate.setOnClickListener {
            rate()
        }
    }


}
