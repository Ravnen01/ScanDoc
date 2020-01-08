package com.lecor.scandoc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 123
    val MY_PERMISSIONS_REQUEST_CAMERA = 456
    val tess = TessBaseAPI()
    val lang = "fra"
    lateinit var LANGUAGE_PATH: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LANGUAGE_PATH = applicationContext.filesDir.absolutePath + "/lang/tessdata/"
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val path = applicationContext.filesDir.absolutePath + "/lang/"
        val name = "image.png"
        val file = File(path, name)
        val outputFileUri = FileProvider.getUriForFile(
            this@MainActivity,
            "com.lecor.scandoc.provider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.take_picture->{
                initTesseract()
                dispatchTakePictureIntent()
                true
            }

            else-> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                val path = applicationContext.filesDir.absolutePath + "/lang/"
                val name = "image.png"
                tess.setImage(File(path, name))
                val text = tess.utF8Text
                textView.text = text
            }
        }
    }

    fun initTesseract() {
        loadLangAsset()
        val path = applicationContext.filesDir.absolutePath + "/lang/"
        tess.init(path, lang)
    }

    fun loadLangAsset() {
        val asset = lang + ".traineddata"
        val languageFile = File(LANGUAGE_PATH, asset)
        if (!languageFile.exists()) {
            if (!languageFile.parentFile.exists()) languageFile.parentFile.mkdirs()
            val assetManager = assets
            val input = assetManager.open(asset)
            val output = FileOutputStream(languageFile)
            val buf = ByteArray(1024)
            var len: Int
            len = input.read(buf)
            while (len > 0) {
                output.write(buf, 0, len)
                len = input.read(buf)
            }
            input.close()
            output.close()
        }
    }

}
