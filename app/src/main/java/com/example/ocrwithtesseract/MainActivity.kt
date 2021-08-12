package com.example.ocrwithtesseract

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ocrwithtesseract.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

private const val REQUEST_CODE = 42
private const val FILE_NAME = "photo.jpg"

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var photoFile: File
    private lateinit var takenImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkForPermissions()
    }

    fun checkForPermissions() {

        //Camere Permission
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CODE
            )

        // Write Permission To Storage
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                );
            }
        }

    }

    fun openCamera(view: View) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(FILE_NAME)

        val fileProvider =
            FileProvider.getUriForFile(
                this,
                "com.example.ocrwithtesseract",
                photoFile
            ) // authority Your packet name
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Camera Failed to Open..", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storegeDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storegeDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

            ocrDynamic(takenImage)

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun statickImage(view: View) {
        ocrStatick()
    }

    fun ocrDynamic(image: Bitmap) {
        val baseAPI = TessBaseAPI()
        val datapath: String = Environment.getExternalStorageDirectory().getPath()

        //Auto only
        baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY)
        val lang = "eng"
        val initResult = baseAPI.init(datapath, lang)

        if (initResult) {
            try {
                baseAPI.setDebug(true)
                baseAPI.setImage(image)
                binding.imageView.setImageBitmap(image)
                val recognizedText = baseAPI.utF8Text.trim { it <= ' ' }
                println("result: " + recognizedText)
                binding.textView.setText(recognizedText)
                baseAPI.end()

            } catch (nfe: FileNotFoundException) {
                nfe.printStackTrace()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        } else {
            println("Unable to init Base API")
        }
    }

    fun ocrStatick() {
        val baseAPI = TessBaseAPI()
        val datapath: String = Environment.getExternalStorageDirectory().getPath()

        //Auto only
        baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY)
        val lang = "eng"
        val initResult = baseAPI.init(datapath, lang)

        if (initResult) {
            var `is`: InputStream? = null
            try {
                `is` = assets.open("test.jpg")
                val drw = Drawable.createFromStream(`is`, null)
                val bmp = (drw as BitmapDrawable).bitmap

                baseAPI.setDebug(true)
                baseAPI.setImage(bmp)
                binding.imageView.setImageBitmap(bmp)
                val recognizedText = baseAPI.utF8Text.trim { it <= ' ' }
                println("result: " + recognizedText)
                binding.textView.setText(recognizedText)
                baseAPI.end()


            } catch (nfe: FileNotFoundException) {
                nfe.printStackTrace()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        } else {
            println("Unable to init Base API")
        }
    }
}