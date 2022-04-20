package com.ogulcankacar.qrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private Button scanButton;
    private TextView urlView;
    private WebView webView;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scanButton = findViewById(R.id.qr_button);
        urlView = findViewById(R.id.textView);
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(MainActivity.this, "Hata: " + error.toString() + "\n" + error.getDescription(), Toast.LENGTH_SHORT).show();

            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
                }

                progressBar.setVisibility(View.VISIBLE);
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
                options.setPrompt("Scan a barcode");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(true);
                barcodeLauncher.launch(options);

                /*
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .setPrompt("Scan a QR Code")
                        .setOrientationLocked(true)
                        .setBeepEnabled(true)
                        .initiateScan();

                 */


            }
        });

        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(false);

    }//OnCreate

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 5) {

            IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);


            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    urlView.setText(result.getContents().toString());
                    openWebView(result.getContents().toString());
                }

            }
        }


    }
*/

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    urlView.setVisibility(View.VISIBLE);
                    urlView.setText(result.getContents().toString());
                    openWebView(result.getContents().toString());
                }
            });


    //open browser
    public void openWebView(String url) {

        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadData(url, "text/html", "UTF-8");

        try {
            webView.loadUrl(url);

        } catch (Exception e) {
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public void onBackPressed() {


        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }

    }
}//Main