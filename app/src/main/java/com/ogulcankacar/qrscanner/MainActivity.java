package com.ogulcankacar.qrscanner;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;
import com.ogulcankacar.qrscanner.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher permissionResultLauncher;
    private ActivityMainBinding activityMainBinding;
    private InterstitialAd mInterstitialAd;
    private CodeScanner codeScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        NetworkCheck();
        registerLauncher();
        codeScanner = new CodeScanner(MainActivity.this, activityMainBinding.scannerView);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setScanMode(ScanMode.SINGLE);
        codeScanner.isAutoFocusEnabled();


        activityMainBinding.webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                activityMainBinding.progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(MainActivity.this, getString(R.string.fix) + error.toString() + "\n" + error.getDescription(), Toast.LENGTH_LONG).show();

            }
        });

        activityMainBinding.qrButton.setOnClickListener(view1 -> {

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                    Snackbar.make(view1, "İzin gerekli", Snackbar.LENGTH_INDEFINITE)
                            .setAction("İzin ver", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //izin al
                                    permissionResultLauncher.launch(Manifest.permission.CAMERA);
                                }
                            }).show();
                } else {
                    //izin al
                    permissionResultLauncher.launch(Manifest.permission.CAMERA);
                }
            } else {
                activityMainBinding.scannerView.setVisibility(View.VISIBLE);
                codeScanner.setDecodeCallback(new DecodeCallback() {
                    @Override
                    public void onDecoded(@NonNull Result result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (result == null) {
                                    Toast.makeText(MainActivity.this, R.string.Cancelled, Toast.LENGTH_LONG).show();
                                    activityMainBinding.progress.setVisibility(View.INVISIBLE);
                                } else {
                                    activityMainBinding.progress.setVisibility(View.INVISIBLE);
                                    activityMainBinding.textView.setVisibility(View.VISIBLE);
                                    activityMainBinding.textView.setText(result.toString());
                                    activityMainBinding.scannerView.setVisibility(View.INVISIBLE);
                                    openWebView(result.toString());
                                }
                            }
                        });

                    }
                });
            }

            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }


        });

        activityMainBinding.scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeScanner.startPreview();
            }
        });

        activityMainBinding.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstallOrNot("com.whatsapp");
                if (installed) {
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");

                    String sentUrl = activityMainBinding.textView.getText().toString();
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, sentUrl);
                    try {
                        startActivity(whatsappIntent);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "hata, gönderim başarısız", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Whatsapp Yüklü Değil", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //ADS
        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        activityMainBinding.adView.loadAd(adRequest);

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

    private void registerLauncher() {

        permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //izin verildi
                    codeScanner.setDecodeCallback(new DecodeCallback() {
                        @Override
                        public void onDecoded(@NonNull Result result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (result == null) {
                                        Toast.makeText(MainActivity.this, R.string.Cancelled, Toast.LENGTH_LONG).show();
                                        activityMainBinding.progress.setVisibility(View.INVISIBLE);
                                    } else {
                                        activityMainBinding.progress.setVisibility(View.INVISIBLE);
                                        activityMainBinding.textView.setVisibility(View.VISIBLE);
                                        activityMainBinding.textView.setText(result.toString());
                                        activityMainBinding.scannerView.setVisibility(View.INVISIBLE);
                                        openWebView(result.toString());
                                    }
                                }
                            });

                        }
                    });
                } else {
                    Snackbar.make(activityMainBinding.view, "İzin gerekli", Snackbar.LENGTH_INDEFINITE)
                            .setAction("İzin ver", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                                    } else {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                        intent.setData(uri);
                                        MainActivity.this.startActivity(intent);
                                    }
                                }
                            }).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        activityMainBinding.adView.resume();
        codeScanner.startPreview();
        super.onResume();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        activityMainBinding.adView.destroy();
        super.onDestroy();
    }

    public void openWebView(String url) {

        activityMainBinding.webview.setVisibility(View.VISIBLE);
        activityMainBinding.webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        activityMainBinding.webview.getSettings().setAllowFileAccess(true);
        activityMainBinding.webview.getSettings().setDomStorageEnabled(true);
        activityMainBinding.webview.getSettings().setJavaScriptEnabled(true);
        activityMainBinding.webview.getSettings().setSupportMultipleWindows(true);
        activityMainBinding.webview.getSettings().setAppCacheEnabled(true);
        activityMainBinding.webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        activityMainBinding.webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        activityMainBinding.webview.loadData(url, "text/html", "UTF-8");

        try {
            activityMainBinding.webview.loadUrl(url);
        } catch (Exception e) {
            Toast.makeText(this, R.string.fix, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {

        if (activityMainBinding.webview.canGoBack()) {
            activityMainBinding.webview.goBack();
        } else {
            super.onBackPressed();
        }

    }

    private void NetworkCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {

        } else {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Hata");
            builder.setMessage("Lütfen internet bağlantınızı kontrol ediniz!");
            builder.setCancelable(false);
            builder.setPositiveButton("Çıkış", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    finish();
                }
            });
            android.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

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