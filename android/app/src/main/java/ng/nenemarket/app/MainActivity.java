package ng.nenemarket.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BridgeActivity {

    // ── File chooser ───────────────────────────────────────────────────────────
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraImageUri;
    private static final int FILE_CHOOSER_REQUEST = 1001;

    // ── Loading / retry ────────────────────────────────────────────────────────
    private static final long LOAD_TIMEOUT_MS = 15_000;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private LinearLayout retryOverlay;
    private ProgressBar topProgressBar;
    private boolean pageFinished = false;
    private boolean isFirstLoad = true;

    // ── Back button (login / register pages) ───────────────────────────────────
    private ImageButton backButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = this.bridge.getWebView();

        // ── WebViewClient ──────────────────────────────────────────────────────
        webView.setWebViewClient(new BridgeWebViewClient(this.bridge) {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // Redirect old hostinger staging URL back to the real site
                if (url.contains("hostingersite.com") || url.contains("darkgreen-goshawk")) {
                    view.loadUrl("https://nenemarket.ng");
                    return true;
                }

                if (url.contains("nenemarket.ng")) {
                    return super.shouldOverrideUrlLoading(view, request);
                }

                // Open external links in the device browser
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception ignored) {}
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!isFirstLoad) {
                    showProgressBar();
                }
                // Show back button on login / register pages
                updateBackButton(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                isFirstLoad = false;
                pageFinished = true;
                timeoutHandler.removeCallbacksAndMessages(null);
                hideRetryOverlay();
                hideProgressBar();
                updateBackButton(url);

                // ── Inject CSS + JS fixes ──────────────────────────────────────
                String js =
                    "(function() {" +

                    // ── CSS: inject once per page ──────────────────────────────
                    "  if (!document.getElementById('__nene_custom_css__')) {" +
                    "    var style = document.createElement('style');" +
                    "    style.id = '__nene_custom_css__';" +
                    "    style.textContent = '" +
                    // Padding so content never hides behind the bottom nav bar
                    "    body, html { padding-bottom: 100px !important; margin-bottom: 0 !important; } " +
                    "    main, #main, .main { margin-bottom: 100px !important; } " +
                    "  ';" +
                    "    document.head.appendChild(style);" +
                    "  }" +

                    "  function fixAll() {" +

                    // ── 1. Remove <footer> HTML tag — safe, never a button wrapper ─
                    "    document.querySelectorAll('footer').forEach(function(el){ el.remove(); });" +

                    // ── 2. Keyword scan — ONLY on footer/nav/section/aside ────────
                    // Deliberately excludes 'div' so we never accidentally remove
                    // a page wrapper div that contains the Continue button somewhere.
                    // Height cap of 250px stops us removing tall page sections that
                    // merely contain a footer keyword deep inside them.
                    "    var footerKw = ['all rights reserved','privacy policy'," +
                    "                    'terms & conditions','terms and conditions'," +
                    "                    'cookie policy'];" +
                    "    document.querySelectorAll('footer, nav, aside, section').forEach(function(el) {" +
                    "      if (!el.parentNode) return;" +
                    "      if (el.clientHeight > 250) return;" +
                    "      var txt = (el.innerText || '').toLowerCase();" +
                    "      for (var i = 0; i < footerKw.length; i++) {" +
                    "        if (txt.indexOf(footerKw[i]) !== -1) { el.remove(); return; }" +
                    "      }" +
                    "    });" +

                    // ── 3. Fix stale hostingersite links ──────────────────────────
                    "    document.querySelectorAll('a[href]').forEach(function(a) {" +
                    "      var h = a.getAttribute('href') || '';" +
                    "      if (h.indexOf('hostingersite.com') !== -1 || h.indexOf('darkgreen-goshawk') !== -1) {" +
                    "        a.setAttribute('href','https://nenemarket.ng');" +
                    "      }" +
                    "    });" +
                    "  }" +

                    // ── MutationObserver: re-run fixAll whenever the DOM changes ─
                    "  if (!window.__neneObserver) {" +
                    "    window.__neneObserver = new MutationObserver(function(){ fixAll(); });" +
                    "    window.__neneObserver.observe(document.body, { childList: true, subtree: true });" +
                    "  }" +

                    // Run immediately and at staggered intervals to catch late-rendered content
                    "  fixAll();" +
                    "  setTimeout(fixAll, 500);" +
                    "  setTimeout(fixAll, 1500);" +
                    "  setTimeout(fixAll, 3000);" +
                    "  setTimeout(fixAll, 6000);" +

                    "})();";

                view.evaluateJavascript(js, null);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    hideProgressBar();
                    showRetryOverlay();
                }
            }
        });

        // ── WebChromeClient (progress + file chooser) ──────────────────────────
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (topProgressBar != null) {
                    topProgressBar.setProgress(progress);
                    if (progress >= 100) {
                        // small delay so the bar finishes visually before hiding
                        new Handler(Looper.getMainLooper()).postDelayed(
                                () -> hideProgressBar(), 250);
                    }
                }
            }

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePath,
                    FileChooserParams fileChooserParams) {

                // Cancel any pending callback
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                    filePathCallback = null;
                }
                filePathCallback = filePath;
                cameraImageUri = null;

                List<Intent> intentList = new ArrayList<>();

                // ── Camera capture ──────────────────────────────────────────
                try {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = createImageFile();
                        if (photoFile != null) {
                            cameraImageUri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    getPackageName() + ".fileprovider",
                                    photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intentList.add(cameraIntent);
                        }
                    }
                } catch (Exception ignored) {}

                // ── Gallery / file picker ───────────────────────────────────
                // Use ACTION_OPEN_DOCUMENT on API 19+ for persistent access;
                // fallback to ACTION_GET_CONTENT for maximum compatibility
                Intent galleryIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                } else {
                    galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                galleryIntent.setType("image/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }

                // ── Combine into chooser ────────────────────────────────────
                Intent chooser = Intent.createChooser(galleryIntent, "Select Image");
                if (!intentList.isEmpty()) {
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                            intentList.toArray(new Intent[0]));
                }

                try {
                    startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    cameraImageUri = null;
                    return false;
                }

                return true;
            }
        });

        buildProgressBar();
        buildBackButton();
        buildRetryOverlay();

        timeoutHandler.postDelayed(() -> {
            if (!pageFinished) {
                hideProgressBar();
                showRetryOverlay();
            }
        }, LOAD_TIMEOUT_MS);
    }

    // ── Back-button logic ──────────────────────────────────────────────────────

    /** Shows the back arrow on login/register/signup pages, hides it elsewhere. */
    private void updateBackButton(String url) {
        if (backButton == null) return;
        runOnUiThread(() -> {
            boolean show = url != null && (
                    url.contains("login") ||
                    url.contains("register") ||
                    url.contains("signup") ||
                    url.contains("sign-up") ||
                    url.contains("forgot") ||
                    url.contains("reset-password"));
            backButton.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }

    private void buildBackButton() {
        backButton = new ImageButton(this);

        // Circle background
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#CC000000"));
        backButton.setBackground(bg);

        // Arrow icon — use system drawable
        backButton.setImageDrawable(
                getResources().getDrawable(android.R.drawable.ic_media_previous, null));
        backButton.setColorFilter(Color.WHITE);
        backButton.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        backButton.setPadding(16, 16, 16, 16);
        backButton.setVisibility(View.GONE);

        int sizePx = (int) (44 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizePx, sizePx);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.setMargins(marginPx,
                marginPx + getStatusBarHeight(),
                marginPx, marginPx);
        backButton.setLayoutParams(lp);

        backButton.setOnClickListener(v -> {
            WebView wv = this.bridge.getWebView();
            if (wv != null && wv.canGoBack()) {
                wv.goBack();
            }
        });

        ViewGroup root = (ViewGroup) this.bridge.getWebView().getParent();
        root.addView(backButton);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) result = getResources().getDimensionPixelSize(resId);
        return result;
    }

    // ── Progress bar ───────────────────────────────────────────────────────────

    private void buildProgressBar() {
        topProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        topProgressBar.setMax(100);
        topProgressBar.setProgress(0);
        topProgressBar.setProgressDrawable(
                getResources().getDrawable(android.R.drawable.progress_horizontal, null));
        // Tint blue to match Nenemarket brand
        topProgressBar.getProgressDrawable()
                .setColorFilter(Color.parseColor("#007BFF"),
                        android.graphics.PorterDuff.Mode.SRC_IN);
        topProgressBar.setVisibility(View.GONE);

        int heightPx = (int) (3 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
        lp.gravity = Gravity.TOP;
        topProgressBar.setLayoutParams(lp);

        ViewGroup root = (ViewGroup) this.bridge.getWebView().getParent();
        root.addView(topProgressBar);
    }

    private void showProgressBar() {
        runOnUiThread(() -> {
            if (topProgressBar != null) {
                topProgressBar.setProgress(0);
                topProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideProgressBar() {
        runOnUiThread(() -> {
            if (topProgressBar != null) {
                topProgressBar.setVisibility(View.GONE);
            }
        });
    }

    // ── Retry overlay ──────────────────────────────────────────────────────────

    private void buildRetryOverlay() {
        retryOverlay = new LinearLayout(this);
        retryOverlay.setOrientation(LinearLayout.VERTICAL);
        retryOverlay.setGravity(Gravity.CENTER);
        retryOverlay.setBackgroundColor(Color.parseColor("#007BFF"));
        retryOverlay.setPadding(64, 64, 64, 64);
        retryOverlay.setVisibility(View.GONE);

        TextView message = new TextView(this);
        message.setText("Nenemarket is taking longer than usual.\nCheck your connection and try again.");
        message.setTextColor(Color.WHITE);
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, 32);

        Button retryButton = new Button(this);
        retryButton.setText("Retry");
        retryButton.setOnClickListener(v -> {
            pageFinished = false;
            hideRetryOverlay();
            this.bridge.getWebView().reload();
            timeoutHandler.removeCallbacksAndMessages(null);
            timeoutHandler.postDelayed(() -> {
                if (!pageFinished) {
                    hideProgressBar();
                    showRetryOverlay();
                }
            }, LOAD_TIMEOUT_MS);
        });

        retryOverlay.addView(message);
        retryOverlay.addView(retryButton);

        ViewGroup root = (ViewGroup) this.bridge.getWebView().getParent();
        root.addView(retryOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showRetryOverlay() {
        runOnUiThread(() -> {
            if (retryOverlay != null) retryOverlay.setVisibility(View.VISIBLE);
        });
    }

    private void hideRetryOverlay() {
        runOnUiThread(() -> {
            if (retryOverlay != null) retryOverlay.setVisibility(View.GONE);
        });
    }

    // ── Back press ────────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        WebView webView = this.bridge.getWebView();
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ── File chooser result ────────────────────────────────────────────────────

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;

            Uri[] results = null;

            if (resultCode == RESULT_OK) {
                if (data == null || (data.getData() == null && data.getClipData() == null)) {
                    // Camera capture — use the pre-created URI
                    if (cameraImageUri != null) {
                        results = new Uri[]{ cameraImageUri };
                    }
                } else if (data.getClipData() != null) {
                    // Multiple images from gallery
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        grantUriReadPermission(uri);
                        results[i] = uri;
                    }
                } else if (data.getData() != null) {
                    // Single image from gallery
                    Uri uri = data.getData();
                    grantUriReadPermission(uri);
                    results = new Uri[]{ uri };
                }
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
            cameraImageUri = null;
        }
    }

    /** Grants persistent read access to a content URI (needed for ACTION_OPEN_DOCUMENT). */
    private void grantUriReadPermission(Uri uri) {
        if (uri == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {}
        }
    }

    /** Creates a uniquely-named temp file for camera captures. */
    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) storageDir = getCacheDir();
        return File.createTempFile("IMG_" + timestamp + "_", ".jpg", storageDir);
    }

    @Override
    public void onDestroy() {
        timeoutHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
