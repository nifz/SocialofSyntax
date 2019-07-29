package id.syntx.socialofsyntax;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.webkit.WebView;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.onesignal.OneSignal.NotificationOpenedHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements OneSignal.NotificationOpenedHandler {

    private WebView webVIew;
    private float m_downX;
    private String url;


    SwipeRefreshLayout swipe;

    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR = 1;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webVIew.loadUrl(url);
            }
        });



// ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        handleIntent(getIntent());

        if(isActivityVisible() == 0){
            url = "https://social.syntx.id/";
        }else{
            url = getIntent().getStringExtra("url");
        }

        webVIew = (WebView) findViewById(R.id.webView);

        initWebView();
        webVIew.loadUrl(url);


        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler((NotificationOpenedHandler) this)
                .init();


        WebSettings mWebSettings = webVIew.getSettings();
        webVIew.setWebViewClient(new WebViewClient());
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setSupportZoom(false);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            String recipeId = appLinkData.getLastPathSegment();
            Uri appData = Uri.parse("content://id.syntx.socialofsyntax/").buildUpon()
                    .appendPath(recipeId).build();
            showRecipe(appData);
        }
    }

    private void showRecipe(Uri appData) {
        return;
    }

    private void initWebView() {
        webVIew.setWebChromeClient(new MyWebChromeClient(this));
        webVIew.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
                invalidateOptionsMenu();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                webVIew.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url){
                swipe.setRefreshing(false);
                super.onPageFinished(view, url);
                invalidateOptionsMenu();

            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                super.onReceivedError(view, request, error);
                invalidateOptionsMenu();
            }
        });
        webVIew.getSettings().setJavaScriptEnabled(true);
        webVIew.setHorizontalScrollBarEnabled(false);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        this.openFileChooser(uploadMsg, "*/*");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return;
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);


    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webVIew.canGoBack()) {
                        webVIew.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
//    public boolean onKeyDown(int keyCode, KeyEvent event){
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && webVIew.canGoBack()){
//            webVIew.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        if (result.notification.payload.launchURL != null){
            if (isActivityVisible() == 1){
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("url", result.notification.payload.launchURL);
                startActivity(i);
            }else{
                initWebView();
                webVIew.loadUrl(result.notification.payload.launchURL);
            }
        }
    }



    public static int isActivityVisible(){
        return activityVisible;
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;


        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (mUMA != null) {
                mUMA.onReceiveValue(null);
            }
            mUMA = filePathCallback;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCM);
                } catch (IOException ex) {
                    Log.e("Webview", "Image file creation failed", ex);
                }
                if (photoFile != null) {
                    mCM = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");
            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, FCR);
            return true;
        }




        public MyWebChromeClient(MainActivity context) {
            super();
            this.context = context;
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress){
            super.onProgressChanged(view, newProgress);
            super.onProgressChanged(view, newProgress);
            if(webVIew.getProgress()== 100){
                swipe.setRefreshing(false);
            } else {
                swipe.setRefreshing(true);
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.activityStarted();
    }

    @Override
    protected void onPause(){
        super.onPause();
        MainActivity.activityPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        MainActivity.activityResumed();
    }

    @Override
    public void onBackPressed() {

        if (webVIew.canGoBack()) {
            webVIew.goBack();
        } else {
            finish();
        }
    }

    private static void activityResumed() {
        activityVisible = 2;
    }

    private static void activityPause() {
        activityVisible = 1;
    }

    private static void activityStarted() {
        activityVisible = 0;
    }

    private static int activityVisible;
}
