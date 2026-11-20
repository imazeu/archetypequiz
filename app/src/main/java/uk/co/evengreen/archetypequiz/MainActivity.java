package uk.co.evengreen.archetypequiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView web;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0b0a12"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }
        });
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        if (b != null) web.restoreState(b);
        else web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    @Override protected void onSaveInstanceState(Bundle o) { super.onSaveInstanceState(o); web.saveState(o); }

    @Override public void onBackPressed() {
        web.evaluateJavascript("(function(){var c=document.getElementById('cardbox');if(c&&!c.hidden){c.hidden=true;return 'y'}var b=document.getElementById('back');var q=document.getElementById('s-q');if(q.classList.contains('on')&&!b.disabled){b.click();return 'y'}return 'n'})()",
            v -> { if (!"\"y\"".equals(v)) MainActivity.super.onBackPressed(); });
    }

    class Bridge {
        @JavascriptInterface public void share(String text) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, text);
            runOnUiThread(() -> startActivity(Intent.createChooser(i, "Share your archetype")));
        }

        @JavascriptInterface public void shareImage(String b64, String text) {
            try {
                File dir = new File(getCacheDir(), "share");
                dir.mkdirs();
                File f = new File(dir, "my-archetype.png");
                try (FileOutputStream o = new FileOutputStream(f)) { o.write(Base64.decode(b64, Base64.DEFAULT)); }
                Uri u = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/png");
                i.putExtra(Intent.EXTRA_STREAM, u);
                i.putExtra(Intent.EXTRA_TEXT, text);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(i, "Share your archetype")));
            } catch (Exception e) { share(text); }
        }
    }
}
