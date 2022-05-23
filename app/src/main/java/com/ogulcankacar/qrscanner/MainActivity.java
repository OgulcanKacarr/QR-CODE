package com.ogulcankacar.qrscanner;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private ImageView scanButton;
    private TextView urlView;
    private WebView webView;
    private ProgressBar progressBar;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scanButton = findViewById(R.id.qr_button);
        urlView = findViewById(R.id.textView);
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress);


        NetworkCheck();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(MainActivity.this, getString(R.string.fix) + error.toString() + "\n" + error.getDescription(), Toast.LENGTH_SHORT).show();

            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
                }

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }


                progressBar.setVisibility(View.VISIBLE);
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
                options.setPrompt(getString(R.string.scanCamera));
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


        urlView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //SendAddressForWp;

                boolean installed = appInstallOrNot("com.whatsapp");
                if (installed) {
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");


                    String sentUrl = urlView.getText().toString();
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, sentUrl);
                    try {
                        startActivity(whatsappIntent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "hata, gönderim başarısız", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Whatsapp Yüklü Değil", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });


        //ADS
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Geçiş
        InterstitialAd.load(MainActivity.this, "ca-app-pub-4310209378038401/6603109166", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error

                        mInterstitialAd = null;
                    }
                });


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

    @Override
    protected void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }

        super.onDestroy();
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(MainActivity.this, R.string.Cancelled, Toast.LENGTH_LONG).show();
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
        //webView.getSettings().setBuiltInZoomControls(true);
        // webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        //webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadData(url, "text/html", "UTF-8");

        try {
            webView.loadUrl(url);

        } catch (Exception e) {
            Toast.makeText(this, R.string.fix, Toast.LENGTH_SHORT).show();

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

    private void NetworkCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting())// iki şart vermemizin sebebi bağlantı olsa
        //bile network bilgisi gelmez ise hataya düşmemek adına işimizi sağlama alıyoruz.
        {

        } else {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Hata");
            builder.setMessage("Lütfen internet bağlantınızı kontrol ediniz!");
            builder.setCancelable(true);
            builder.setPositiveButton("Çıkış", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    finish(); //Uygulamayı sonlandırıyoruz
                }
            });
            android.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    //Whatsapp
    private boolean appInstallOrNot(String url) {
        PackageManager packageManager = getPackageManager();
        boolean app_installed;
        try {
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

}//Main