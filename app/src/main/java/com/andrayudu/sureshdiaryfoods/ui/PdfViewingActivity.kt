package com.andrayudu.sureshdiaryfoods.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.andrayudu.sureshdiaryfoods.R
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class PdfViewingActivity : AppCompatActivity() {

    private val Tag = "PdfViewingActivity"

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
                val urlTask: Task<DataSnapshot> = when (policyType) {
                    "Return" -> {
                        utilitiesDb.child("returnAndRefundPolicyLink").get()
                    }
                    "Shipping" -> {
                        utilitiesDb.child("shippingPolicyLink").get()
                    }
                    "Terms" -> {
                        utilitiesDb.child("termsAndConditions").get()
                    }

                    //else case contains privacy policy
                    else -> {
                        utilitiesDb.child("privacyPolicyLink").get()
                    }
                }


               val policyLink =  urlTask.await().value
               policyLink?.let {
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