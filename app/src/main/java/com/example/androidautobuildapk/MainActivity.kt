package com.example.fototopdf // O'z paketingiz nomini yozing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnCapture: Button
    private lateinit var btnSavePdf: Button
    private lateinit var tvStatus: TextView

    private var imageUri: Uri? = null
    private var capturedBitmap: Bitmap? = null

    // Kameradan rasm qaytganida ishlaydi
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            contentResolver.openInputStream(imageUri!!)?.use { inputStream ->
                capturedBitmap = BitmapFactory.decodeStream(inputStream)
                ivPreview.setImageBitmap(capturedBitmap)
                btnSavePdf.isEnabled = true
                tvStatus.text = "Rasm olindi! Endi PDF qilishingiz mumkin."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivPreview = findViewById(R.id.ivPreview)
        btnCapture = findViewById(R.id.btnCapture)
        btnSavePdf = findViewById(R.id.btnSavePdf)
        tvStatus = findViewById(R.id.tvStatus)

        btnCapture.setOnClickListener {
            openCamera()
        }

        btnSavePdf.setOnClickListener {
            capturedBitmap?.let { bitmap ->
                createPdf(bitmap)
            }
        }
    }

    private fun openCamera() {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile = File.createTempFile("photo_${System.currentTimeMillis()}", ".jpg", storageDir)
        
        imageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", photoFile)
        takePictureLauncher.launch(imageUri)
    }

    private fun createPdf(bitmap: Bitmap) {
        val pdfDocument = PdfDocument()

        // Standart A4 nisbati yoki rasmning o'z o'lchami bo'yicha sahifa ochamiz
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Rasmni PDF sahifasiga chizamiz
        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        // Faylni saqlaymiz (Downloads papkasiga)
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFileName = "Doc_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(downloadDir, pdfFileName)

        try {
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            tvStatus.text = "PDF saqlandi:\nYuklamalar (Downloads)/$pdfFileName"
            Toast.makeText(this, "Fayl Downloads papkasiga saqlandi!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
