/**
 * Nenemarket Native App — Capacitor Bootstrap
 *
 * Architecture: Pure WebView wrapper.
 * capacitor.config.ts sets `server.url = "https://nenemarket.ng"` so the
 * native WebView loads the live website directly. This file handles only the
 * native-layer concerns that sit on top of the website.
 */

import { Capacitor } from '@capacitor/core';
import { App as CapApp } from '@capacitor/app';
import { Network } from '@capacitor/network';
import { StatusBar, Style } from '@capacitor/status-bar';
import { Keyboard } from '@capacitor/keyboard';
import { SplashScreen } from '@capacitor/splash-screen';

/* ─────────────────────────────────────────────────────────────────────────────
   Non-native (browser) fallback
   Shows a branded holding screen with a link to the live site.
   The actual app runs on Android / iOS via Capacitor.
───────────────────────────────────────────────────────────────────────────── */
if (!Capacitor.isNativePlatform()) {
  document.body.innerHTML = `
    <div style="
      display:flex;flex-direction:column;align-items:center;justify-content:center;
      height:100vh;background:#007BFF;color:#fff;font-family:-apple-system,BlinkMacSystemFont,
      'Segoe UI',Roboto,sans-serif;gap:24px;text-align:center;padding:32px;
    ">
      <img
        src="https://nenemarket.ng/assets/logo2.png"
        style="width:200px;height:auto;margin-bottom:8px;"
        alt="Nenemarket"
      />
      <p style="font-size:14px;opacity:0.85;max-width:300px;line-height:1.7;margin:0;">
        This is the native app shell.<br/>
        Open in <strong>Android Studio</strong> or <strong>Xcode</strong> to run
        on a device — the WebView loads <strong>nenemarket.ng</strong> live.
      </p>
      <a
        href="https://nenemarket.ng"
        target="_blank"
        style="
          display:inline-block;padding:12px 32px;
          background:#fff;color:#007BFF;
          border-radius:12px;font-weight:700;
          text-decoration:none;font-size:15px;
          box-shadow:0 4px 16px rgba(0,0,0,0.15);
        "
      >Open nenemarket.ng →</a>
    </div>`;
}

/* ─────────────────────────────────────────────────────────────────────────────
   Native platform — plugin initialisation
───────────────────────────────────────────────────────────────────────────── */
if (Capacitor.isNativePlatform()) {

  /* Status bar — matches nenemarket.ng's blue header */
  StatusBar.setStyle({ style: Style.Dark }).catch(() => {});
  StatusBar.setBackgroundColor({ color: '#007BFF' }).catch(() => {});

  /* Keyboard — scroll body so focused inputs stay visible */
  Keyboard.setAccessoryBarVisible({ isVisible: false }).catch(() => {});

  /* ── Offline / online detection ──────────────────────────────────────────
     Shows a sticky red banner when the device loses internet.
     The banner is injected into the WebView's DOM so it floats above the
     nenemarket.ng page content.
  ───────────────────────────────────────────────────────────────────────── */
  const BANNER_ID = '__nene_offline__';

  const showOfflineBanner = (): void => {
    if (document.getElementById(BANNER_ID)) return;
    const el = document.createElement('div');
    el.id = BANNER_ID;
    el.textContent = '⚠️  No internet connection — some features may be unavailable';
    Object.assign(el.style, {
      position:   'fixed',
      top:        '0',
      left:       '0',
      right:      '0',
      background: '#c62828',
      color:      '#fff',
      textAlign:  'center',
      padding:    '9px 16px',
      fontSize:   '13px',
      fontFamily: '-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif',
      fontWeight: '600',
      zIndex:     '2147483647',
      lineHeight: '1.4',
    });
    document.body.prepend(el);
  };

  const hideOfflineBanner = (): void =>
    document.getElementById(BANNER_ID)?.remove();

  Network.addListener('networkStatusChange', (status) => {
    status.connected ? hideOfflineBanner() : showOfflineBanner();
  });
  Network.getStatus().then((status) => {
    if (!status.connected) showOfflineBanner();
  });

  /* ── Android hardware back button ────────────────────────────────────────
     Navigate WebView history first; exit the app when at the root.
  ───────────────────────────────────────────────────────────────────────── */
  CapApp.addListener('backButton', ({ canGoBack }) => {
    if (canGoBack) {
      window.history.back();
    } else {
      CapApp.exitApp();
    }
  });

  /* ── Splash screen ───────────────────────────────────────────────────────
     Hide the native splash after the page has begun loading.
     The 400 ms fade gives a smooth transition into the website.
  ───────────────────────────────────────────────────────────────────────── */
  SplashScreen.hide({ fadeOutDuration: 400 }).catch(() => {});
}
