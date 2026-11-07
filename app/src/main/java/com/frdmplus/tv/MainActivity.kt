package com.frdmplus.tv

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var cursor: CursorView
    private lateinit var loading: ProgressBar
    private lateinit var errorView: View
    private lateinit var fullscreen: FrameLayout

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    /** Pointer mode (arrow keys move an on-screen pointer) vs. plain arrow-key mode. */
    private var cursorMode = true
    private var touchDownTime = 0L

    /** Launch gate: keep the page hidden until we know whether to show sign-in or home. */
    private enum class Gate { CHECKING, GOING_HOME, DONE }
    private var gate = Gate.DONE
    private val openGate = Runnable { revealPage() }

    private val handler = Handler(Looper.getMainLooper())
    private val hideCursor = Runnable { cursor.visibility = View.INVISIBLE }
    private val density by lazy { resources.displayMetrics.density }

    // ---------------------------------------------------------------- setup

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        cursor = findViewById(R.id.cursor)
        loading = findViewById(R.id.loading)
        errorView = findViewById(R.id.error)
        fullscreen = findViewById(R.id.fullscreen)

        webView.setBackgroundColor(Color.BLACK)
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            textZoom = 100
            setSupportZoom(false)
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            // Some sites refuse or downgrade "embedded WebView" browsers; drop the marker.
            userAgentString = WebSettings.getDefaultUserAgent(this@MainActivity)
                .replace("; wv", "")
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true) // sign-in and payment flows
        }

        webView.webViewClient = Client()
        webView.webChromeClient = ChromeClient()
        webView.addJavascriptInterface(AuthBridge(), "FrdmTv")

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            // Open on the sign-in page, hidden, and switch to home if already signed in.
            gate = Gate.CHECKING
            webView.visibility = View.INVISIBLE
            handler.postDelayed(openGate, 10_000) // never leave the screen blank
            webView.loadUrl(BuildConfig.LOGIN_URL)
        }
        webView.requestFocus()
        pokeCursor()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onPause() {
        // Pauses video when the user presses Home.
        webView.onPause()
        webView.pauseTimers()
        CookieManager.getInstance().flush() // keep the user signed in
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    // ---------------------------------------------------------------- remote keys

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val down = event.action == KeyEvent.ACTION_DOWN
        val up = event.action == KeyEvent.ACTION_UP

        when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (up && !event.isCanceled) onBack()
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (up) toggleCursorMode()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                if (down && event.repeatCount == 0) mediaKey(event.keyCode)
                return true
            }
        }

        if (gate != Gate.DONE && event.keyCode != KeyEvent.KEYCODE_BACK) return true

        if (errorView.visibility == View.VISIBLE) {
            if (up && isOk(event.keyCode)) retry()
            return true
        }

        // In fullscreen video, or with the pointer off, the page gets the keys itself.
        if (customView != null || !cursorMode) return super.dispatchKeyEvent(event)

        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> if (down) move(0, -1, event.repeatCount)
            KeyEvent.KEYCODE_DPAD_DOWN -> if (down) move(0, 1, event.repeatCount)
            KeyEvent.KEYCODE_DPAD_LEFT -> if (down) move(-1, 0, event.repeatCount)
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (down) move(1, 0, event.repeatCount)
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (down && event.repeatCount == 0) tap(MotionEvent.ACTION_DOWN)
                if (up) tap(MotionEvent.ACTION_UP)
            }
            else -> return super.dispatchKeyEvent(event)
        }
        return true
    }

    private fun isOk(code: Int) = code == KeyEvent.KEYCODE_DPAD_CENTER ||
        code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_NUMPAD_ENTER

    private fun onBack() {
        when {
            customView != null -> customViewCallback?.onCustomViewHidden()
            errorView.visibility != View.VISIBLE && canGoBackPastLogin() -> webView.goBack()
            // Android TV guidelines: Back on the app's first screen returns to the TV home screen.
            else -> finish()
        }
    }

    /** True when there is a previous page that isn't the sign-in screen we already left. */
    private fun canGoBackPastLogin(): Boolean {
        if (!webView.canGoBack()) return false
        val list = webView.copyBackForwardList()
        val prev = list.getItemAtIndex(list.currentIndex - 1)?.url
        return !(isLoginUrl(prev) && !isLoginUrl(webView.url))
    }

    private fun toggleCursorMode() {
        cursorMode = !cursorMode
        if (cursorMode) pokeCursor() else {
            handler.removeCallbacks(hideCursor)
            cursor.visibility = View.INVISIBLE
        }
        Toast.makeText(
            this, if (cursorMode) R.string.cursor_on else R.string.cursor_off, Toast.LENGTH_SHORT,
        ).show()
    }

    /** Shows the pointer and hides it again after a few idle seconds. */
    private fun pokeCursor() {
        if (!cursorMode || customView != null) return
        cursor.visibility = View.VISIBLE
        handler.removeCallbacks(hideCursor)
        handler.postDelayed(hideCursor, 5000)
    }

    // ---------------------------------------------------------------- pointer

    private fun move(dirX: Int, dirY: Int, repeat: Int) {
        val wasHidden = cursor.visibility != View.VISIBLE
        pokeCursor()
        if (wasHidden && repeat == 0) return // first press just reveals the pointer

        // Accelerate while the key is held.
        val step = (8 + minOf(repeat, 20) * 2.2f) * density
        val (overX, overY) = cursor.moveBy(dirX * step, dirY * step)

        sendMouse(MotionEvent.ACTION_HOVER_MOVE) // lets hover menus open
        if (overX != 0f || overY != 0f) {
            // Pointer is at the edge: scroll whatever is under it, like a mouse wheel.
            sendMouse(MotionEvent.ACTION_SCROLL, hScroll = sign(overX), vScroll = -sign(overY))
        }
    }

    private fun sign(v: Float) = when {
        v > 0f -> 1f
        v < 0f -> -1f
        else -> 0f
    }

    private fun tap(action: Int) {
        cursor.clicking = action == MotionEvent.ACTION_DOWN
        pokeCursor()
        val now = SystemClock.uptimeMillis()
        if (action == MotionEvent.ACTION_DOWN) {
            touchDownTime = now
            webView.requestFocus()
        }
        val ev = MotionEvent.obtain(touchDownTime, now, action, cursor.cursorX, cursor.cursorY, 0)
        ev.source = InputDevice.SOURCE_TOUCHSCREEN
        webView.dispatchTouchEvent(ev)
        ev.recycle()
    }

    private fun sendMouse(action: Int, hScroll: Float = 0f, vScroll: Float = 0f) {
        val props = arrayOf(MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_MOUSE
        })
        val coords = arrayOf(MotionEvent.PointerCoords().apply {
            x = cursor.cursorX
            y = cursor.cursorY
            setAxisValue(MotionEvent.AXIS_HSCROLL, hScroll)
            setAxisValue(MotionEvent.AXIS_VSCROLL, vScroll)
        })
        val now = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(
            now, now, action, 1, props, coords, 0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_MOUSE, 0,
        )
        webView.dispatchGenericMotionEvent(ev)
        ev.recycle()
    }

    // ---------------------------------------------------------------- media keys

    private fun mediaKey(code: Int) {
        val op = when (code) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> "v.play()"
            KeyEvent.KEYCODE_MEDIA_PAUSE -> "v.pause()"
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> "v.currentTime=Math.min(v.duration||1e9,v.currentTime+10)"
            KeyEvent.KEYCODE_MEDIA_REWIND -> "v.currentTime=Math.max(0,v.currentTime-10)"
            else -> "(v.paused?v.play():v.pause())"
        }
        val js = "(function(){var vs=[].slice.call(document.querySelectorAll('video'));" +
            "var v=vs.filter(function(x){return !x.paused;})[0]||vs[0];if(v){$op;}})();"
        webView.evaluateJavascript(js, null)
    }

    // ---------------------------------------------------------------- errors

    private fun retry() {
        errorView.visibility = View.GONE
        loading.visibility = View.VISIBLE
        val url = webView.url
        if (url.isNullOrEmpty() || url.startsWith("about:")) webView.loadUrl(BuildConfig.START_URL)
        else webView.reload()
    }

    // ---------------------------------------------------------------- launch gate

    private val authCheckJs = """
        (function(){
          fetch('${BuildConfig.PROFILE_API}', {credentials: 'include'})
            .then(function(r){ return r.json(); })
            .then(function(j){
              var t = j && j.authentication && j.authentication.type;
              FrdmTv.authChecked(!!t && t !== 'anonymous');
            })
            .catch(function(){ FrdmTv.authChecked(false); });
        })();
    """.trimIndent()

    private inner class AuthBridge {
        @JavascriptInterface
        fun authChecked(signedIn: Boolean) {
            runOnUiThread {
                if (gate != Gate.CHECKING) return@runOnUiThread
                if (signedIn) {
                    gate = Gate.GOING_HOME
                    webView.loadUrl(BuildConfig.START_URL)
                } else {
                    revealPage() // not signed in: the sign-in page is the first screen
                }
            }
        }
    }

    private fun revealPage() {
        handler.removeCallbacks(openGate)
        gate = Gate.DONE
        webView.visibility = View.VISIBLE
        loading.visibility = View.GONE
        webView.requestFocus()
    }

    private fun isLoginUrl(url: String?): Boolean =
        url != null && Uri.parse(url).path?.trimEnd('/') == Uri.parse(BuildConfig.LOGIN_URL).path?.trimEnd('/')

    // ---------------------------------------------------------------- sign-in / sign-up

    private val signupPaths = BuildConfig.SIGNUP_PATHS.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    private var signupDialog: AlertDialog? = null

    private fun isSignupPage(uri: Uri?): Boolean {
        if (BuildConfig.ALLOW_SIGNUP || uri == null) return false
        val path = uri.path ?: return false
        return signupPaths.any { path == it || path.startsWith("$it/") }
    }

    private fun showSignupNotice() {
        if (signupDialog?.isShowing == true) return
        val site = Uri.parse(BuildConfig.START_URL).host?.removePrefix("www.") ?: BuildConfig.START_URL
        signupDialog = AlertDialog.Builder(this)
            .setTitle(R.string.signup_title)
            .setMessage(getString(R.string.signup_message, site))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /**
     * TV sign-in convenience: tick the site's "Remember me" box when the sign-in
     * form appears, so people don't have to type their password on a TV again.
     */
    private val rememberMeJs = """
        (function(){
          if (window.__tvRemember) return; window.__tvRemember = true;
          function tick(){
            document.querySelectorAll('input[type=checkbox][name=rememberMe]').forEach(function(b){
              if (!b.dataset.tvDone) { b.dataset.tvDone = '1'; if (!b.checked) b.click(); }
            });
          }
          new MutationObserver(tick).observe(document.documentElement, {childList:true, subtree:true});
          tick();
        })();
    """.trimIndent()

    private inner class Client : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            if (request.isForMainFrame && isSignupPage(uri)) {
                showSignupNotice()
                return true
            }
            if (uri.scheme == "http" || uri.scheme == "https") return false
            // intent:, market:, mailto: etc. — hand to another app if one exists.
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: ActivityNotFoundException) {
            } catch (e: SecurityException) {
            }
            return true
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            loading.visibility = View.VISIBLE
        }

        override fun onPageCommitVisible(view: WebView, url: String?) {
            if (gate == Gate.DONE) loading.visibility = View.GONE
        }

        override fun onPageFinished(view: WebView, url: String?) {
            view.evaluateJavascript(rememberMeJs, null)
            when (gate) {
                Gate.CHECKING -> if (isLoginUrl(url)) view.evaluateJavascript(authCheckJs, null)
                Gate.GOING_HOME -> {
                    view.clearHistory() // Back from home shouldn't land on sign-in
                    revealPage()
                }
                Gate.DONE -> loading.visibility = View.GONE
            }
        }

        // The site is a single-page app: most page changes happen without a real
        // page load, and only show up here.
        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
            if (isSignupPage(url?.let(Uri::parse))) {
                if (view.canGoBack()) view.goBack() else view.loadUrl(BuildConfig.START_URL)
                showSignupNotice()
            }
        }

        override fun onReceivedError(
            view: WebView, request: WebResourceRequest, error: WebResourceError,
        ) {
            if (request.isForMainFrame) {
                if (gate != Gate.DONE) revealPage()
                loading.visibility = View.GONE
                errorView.visibility = View.VISIBLE
            }
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            // The page's renderer crashed or was killed for memory: start fresh
            // instead of letting the whole app crash.
            recreate()
            return true
        }
    }

    // ---------------------------------------------------------------- video & DRM

    private inner class ChromeClient : WebChromeClient() {
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            if (customView != null) {
                callback.onCustomViewHidden()
                return
            }
            customView = view
            customViewCallback = callback
            fullscreen.addView(
                view, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
            fullscreen.visibility = View.VISIBLE
            cursor.visibility = View.INVISIBLE
            handler.removeCallbacks(hideCursor)
            view.requestFocus()
        }

        override fun onHideCustomView() {
            fullscreen.removeAllViews()
            fullscreen.visibility = View.GONE
            customView = null
            customViewCallback = null
            webView.requestFocus()
            pokeCursor()
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            // Allow DRM-protected video (Widevine); refuse camera/microphone.
            val drm = PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID
            runOnUiThread {
                if (request.resources.contains(drm)) request.grant(arrayOf(drm)) else request.deny()
            }
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress >= 100 && gate == Gate.DONE) loading.visibility = View.GONE
        }

        // Without this, video elements show a grey play-button placeholder before loading.
        override fun getDefaultVideoPoster(): Bitmap =
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLACK) }
    }
}
