package com.andrayudu.sureshdiaryfoods.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.andrayudu.sureshdiaryfoods.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class PdfViewingActivity : AppCompatActivity() {


    private lateinit var pdfView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var actionBarBack:ImageView

    private val removePdfTopIcon =
        "javascript:(function() {" + "document.querySelector('[role=\"toolbar\"]').remove();})()"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewing)

        pdfView = findViewById(R.id.pdfView);
        progress = findViewById(R.id.progress);
        actionBarBack = findViewById(R.id.actionbar_Back)

        initClickListeners()

        val policyType = intent.getStringExtra("policyType")

        if(policyType!=null){
            getUrlFromFirebase(policyType)
        }
    }

    private fun initClickListeners() {
        actionBarBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getUrlFromFirebase(policyType:String){

            CoroutineScope(Dispatchers.IO).launch {
                val utilitiesDb =  FirebaseDatabase.getInstance().getReference("Utilities")

                if (policyType == "Shipping"){
                    val urlTask =utilitiesDb.child("returnAndRefundPolicyLink").get().await()
                    val policyLink = urlTask.value
                    policyLink?.let {
                        withContext(Dispatchers.Main){
                            showPdfFile(policyLink.toString())
                        }
                    }

                }
                else{
                    val urlTask =utilitiesDb.child("shippingPolicyLink").get().await()
                    val policyLink = urlTask.value
                    withContext(Dispatchers.Main){
                        showPdfFile(policyLink.toString())
                    }
                }
            }


    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showPdfFile(imageString: String) {

        showProgress()
        pdfView.invalidate()
        pdfView.settings.javaScriptEnabled = true
        pdfView.settings.setSupportZoom(true)
        pdfView.settings.builtInZoomControls = true
        pdfView.loadUrl("http://docs.google.com/gview?embedded=true&url=$imageString")
        pdfView.webViewClient = object : WebViewClient() {
            var checkOnPageStartedCalled = false
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                checkOnPageStartedCalled = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (checkOnPageStartedCalled) {
                    pdfView.loadUrl(removePdfTopIcon)
                    hideProgress()
                } else {
                    showPdfFile(imageString)
                }
            }
        }
    }

    private fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress.visibility = View.GONE
    }

}