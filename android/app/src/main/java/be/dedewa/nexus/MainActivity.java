package be.dedewa.nexus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * NEXUS CONTROL pour Android : une fenêtre plein écran qui ouvre ton NEXUS en ligne.
 * Au premier lancement, setup.html demande l'adresse et la mémorise.
 */
public class MainActivity extends Activity {
    private static final String SETUP = "file:///android_asset/setup.html";
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#060a10"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUserAgentString(s.getUserAgentString() + " NexusApp/1.0");

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("file".equals(scheme)) return false;
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    Uri current = Uri.parse(view.getUrl() == null ? "" : view.getUrl());
                    // même site que NEXUS → dans l'appli ; liens externes (GitHub, sites clients…) → navigateur
                    if (current.getHost() == null || "file".equals(current.getScheme()) || uri.getHost().equals(current.getHost())) return false;
                }
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) view.loadUrl(SETUP + "?error=offline");
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() == 404) view.loadUrl(SETUP + "?error=404");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
            }
        });

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(SETUP);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        // retour dans NEXUS ; on ne revient jamais sur l'écran d'adresse (sinon il relance NEXUS en boucle)
        android.webkit.WebBackForwardList list = web.copyBackForwardList();
        int i = list.getCurrentIndex();
        String prev = i > 0 ? list.getItemAtIndex(i - 1).getUrl() : null;
        if (web.canGoBack() && prev != null && !prev.startsWith("file:")) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}
