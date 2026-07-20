import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'ng.nenemarket.app',
  appName: 'Nenemarket',
  webDir: 'dist/public',

  // ─── The native WebView loads nenemarket.ng exactly as-is ─────────────
  server: {
    url: 'https://nenemarket.ng',
    cleartext: false,
    androidScheme: 'https',
    allowNavigation: [
      'nenemarket.ng',
      '*.nenemarket.ng',
    ],
  },

  plugins: {
    SplashScreen: {
      launchShowDuration: 2500,
      launchAutoHide: true,        // auto-hides after launchShowDuration; MainActivity also shows a retry screen if nenemarket.ng is still loading
      backgroundColor: '#007BFF',   // matches the site's primary blue header
      androidSplashResourceName: 'splash',
      androidScaleType: 'CENTER_CROP',
      showSpinner: false,
      splashFullScreen: true,
      splashImmersive: true,
    },

    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert'],
    },

    Keyboard: {
      resize: 'body',
      style: 'dark',
      resizeOnFullScreen: true,
    },

    StatusBar: {
      // White status bar with dark icons — matches nenemarket.ng header
      style: 'DARK',
      backgroundColor: '#007BFF',
      overlaysWebView: false,
    },
  },

  android: {
    allowMixedContent: false,
    captureInput: true,
    webContentsDebuggingEnabled: false,
  },

  ios: {
    contentInset: 'automatic',
    scrollEnabled: true,
    limitsNavigationsToAppBoundDomains: true,
  },
};

export default config;
