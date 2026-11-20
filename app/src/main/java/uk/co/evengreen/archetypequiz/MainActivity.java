package uk.co.evengreen.archetypequiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {
    private WebView web;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0b0a12"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        if (b != null) web.restoreState(b); else web.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onSaveInstanceState(Bundle o) { super.onSaveInstanceState(o); web.saveState(o); }

    @Override public void onBackPressed() {
        web.evaluateJavascript("(function(){var b=document.getElementById('back');var q=document.getElementById('s-q');if(q.classList.contains('on')&&!b.disabled){b.click();return 'y'}return 'n'})()",
            v -> { if (!"\"y\"".equals(v)) MainActivity.super.onBackPressed(); });
    }

    class Bridge {
        @JavascriptInterface public void share(String text) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(i, "Share your archetype"));
        }
    }
}
