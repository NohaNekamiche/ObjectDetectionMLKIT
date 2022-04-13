package com.example.objectdetectionapp

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore

import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var txtResult:TextView
    lateinit var img:ImageView
    lateinit var  btn: Button
    private val CAMERA_PERMISSION_CODE=123
    private val READ_STORAGE_PERMISSION_CODE=113
    private val WRITE_STORAGE_PERMISSION_CODE=113
    private val TAG="MyTag"
    private lateinit var cameraActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityLauncher:ActivityResultLauncher<Intent>
    private lateinit var imageInput: InputImage
    private lateinit var imageLabeler: ImageLabeler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtResult=findViewById(R.id.txtResult)
        img=findViewById(R.id.imgObj)
        btn=findViewById(R.id.btn)
        imageLabeler=ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        cameraActivityLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data
                    try {
                        val photo =data?.extras?.get("data") as Bitmap
                        img.setImageBitmap(photo)
                        imageInput= InputImage.fromBitmap(photo,0)
                        processImage()

                    }
                    catch (e:Exception){
                        Log.d(TAG,"onActivityResult ${e.message}")
                    }
                }
            }
        )
        galleryActivityLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data
                    try {
                        val  photo = data?.data?.let {
                            InputImage.fromFilePath(this@MainActivity,
                                it
                            )
                        }
                        img.setImageURI(data?.data)
                        processImage()

                    }
                    catch (e:Exception){

                    }
                }
            }
        )
        btn.setOnClickListener {
            val opts= arrayOf("Camera","Gallery")
            val builder=AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Choose option")
            builder.setItems(opts,
            DialogInterface.OnClickListener{
                dialog,which ->
                if(which==0){
                    val cameraIntent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraActivityLauncher?.launch(cameraIntent)


                }else{
                    val storageIntent=Intent()
                    storageIntent.setType("image/*")
                    storageIntent.action=Intent.ACTION_GET_CONTENT
                    galleryActivityLauncher?.launch(storageIntent)
                }
            })
            builder.show()
        }

    }

    private fun processImage() {
        imageLabeler.process(imageInput)
            .addOnSuccessListener { labels ->
                var res=""
                for (label in labels) {
                    res=res+"\n"+label.text
                }
                txtResult.text=res

            }
            .addOnFailureListener { e ->
               Log.d(TAG,"Image labeling ${e.message}")
            }
    }

    private fun checkPermission(permission:String,requestCode:Int){
        if(ContextCompat.checkSelfPermission(this@MainActivity ,permission)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this@MainActivity , arrayOf(permission),requestCode)
        }
        else{
            Toast.makeText(this@MainActivity ,"permission already is garented ",Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        checkPermission(android.Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== CAMERA_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                READ_STORAGE_PERMISSION_CODE)
                Toast.makeText(this@MainActivity ,"permission already is garented ", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this@MainActivity ,"permission already is denied ", Toast.LENGTH_LONG).show()
            }
        }
        else if(requestCode==READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    WRITE_STORAGE_PERMISSION_CODE)
                Toast.makeText(this@MainActivity ,"storage already is garented ", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this@MainActivity ,"storage already is denied ", Toast.LENGTH_LONG).show()
            }
        }

        else if(requestCode==WRITE_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity ,"storage already is garented ", Toast.LENGTH_LONG).show()
            }

        }
    }
}