package com.wass.wabstatus.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.wass.wabstatus.R;

public class AdController {
    static boolean isShowAds = false;
    public static int adCounter = 1;
    public static int adDisplayCounter = -201;

    public static boolean isLoadIronSourceAd = false;

    public static void initAd(Context context) {
        if (isShowAds) {
            MobileAds.initialize(context, initializationStatus -> {

            });
        }
    }

    static AdView gadView;
    public static void loadBannerAd(Context context, LinearLayout adContainer) {
        if (isShowAds) {
            gadView = new AdView(context);
            gadView.setAdUnitId(context.getString(R.string.admob_banner_id));
            adContainer.addView(gadView);
            loadBanner(context);
        }
    }

    static void loadBanner(Context context) {
        if (isShowAds) {
            AdRequest adRequest =
                    new AdRequest.Builder().build();

            AdSize adSize = getAdSize((Activity) context);
            gadView.setAdSize(adSize);
            gadView.loadAd(adRequest);
        }
    }

    static AdSize getAdSize(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }



    static InterstitialAd mInterstitialAd;

    public static void loadInterAd(Context context) {
        if (isShowAds) {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(context, context.getString(R.string.admob_interstitial), adRequest, new InterstitialAdLoadCallback() {
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
        }
    }

    public static void showInterAd(final Activity context, final Intent intent, final int requstCode) {
        if (isShowAds) {
            if (adCounter == adDisplayCounter && mInterstitialAd != null) {
                adCounter = 1;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
//                    Log.d("TAG", "The ad was dismissed.");
                        loadInterAd(context);
                        startActivity(context, intent, requstCode);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
//                     Called when fullscreen content failed to show.
//                    Log.d("TAG", "The ad failed to show.");
                    }


                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
//                    Log.d("TAG", "The ad was shown.");
                    }
                });
                mInterstitialAd.show((Activity) context);
            } else {
                if (adCounter == adDisplayCounter) {
                    adCounter = 1;
                }
                startActivity(context, intent, requstCode);
            }
        }else {
            startActivity(context, intent, requstCode);
        }
    }

    static void startActivity(Activity context, Intent intent, int requestCode) {
        if (intent != null) {
            context.startActivityForResult(intent, requestCode);
        }
    }

    public static void showInterAd(final Fragment context, final Intent intent, final int requstCode) {
        if (isShowAds) {
            if (adCounter == adDisplayCounter && mInterstitialAd != null) {
                adCounter = 1;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
//                    Log.d("TAG", "The ad was dismissed.");
                        loadInterAd(context.getActivity());
                        startActivity(context, intent, requstCode);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
//                     Called when fullscreen content failed to show.
//                    Log.d("TAG", "The ad failed to show.");
                    }


                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
//                    Log.d("TAG", "The ad was shown.");
                    }
                });
                mInterstitialAd.show(context.getActivity());
            } else {
                if (adCounter == adDisplayCounter) {
                    adCounter = 1;
                }
                startActivity(context, intent, requstCode);
            }
        }else {
            startActivity(context, intent, requstCode);
        }
    }

    static void startActivity(Fragment context, Intent intent, int requestCode) {
        if (intent != null) {
            context.startActivityForResult(intent, requestCode);
        }
    }


    static Intent fbIntent;
    static int fbRequstCode;

    public static void inItIron(Activity activity){
        if (isShowAds) {
            String advertisingId = IronSource.getAdvertiserId(activity);

            IronSource.setUserId(advertisingId);
            // init the IronSource SDK
            IronSource.init(activity, activity.getResources().getString(R.string.iron_ad_id));
        }
    }

    static IronSourceBannerLayout banner = null;
    public static void ironBanner(Activity activity, LinearLayout bannerContainer) {

        if (isShowAds) {
            bannerContainer.removeAllViews();
            banner = IronSource.createBanner(activity, ISBannerSize.BANNER);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            bannerContainer.addView(banner, 0, layoutParams);

            if (banner != null) {
                banner.setBannerListener(new BannerListener() {
                    @Override
                    public void onBannerAdLoaded() {
                        // Called after a banner ad has been successfully loaded
                    }

                    @Override
                    public void onBannerAdLoadFailed(IronSourceError error) {
                        // Called after a banner has attempted to load an ad but failed.
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bannerContainer.removeAllViews();
                            }
                        });
                    }

                    @Override
                    public void onBannerAdClicked() {
                        // Called after a banner has been clicked.
                    }

                    @Override
                    public void onBannerAdScreenPresented() {
                        // Called when a banner is about to present a full screen content.
                    }

                    @Override
                    public void onBannerAdScreenDismissed() {
                        // Called after a full screen content has been dismissed
                    }

                    @Override
                    public void onBannerAdLeftApplication() {
                        // Called when a user would be taken out of the application context.
                    }
                });

                IronSource.loadBanner(banner);
            }
        }
    }


    public static void destroyIron(){
        if (banner != null) {
            IronSource.destroyBanner(banner);
        }

    }

    public static void ironInterstitial(final Activity context) {
        if (isShowAds) {
            IronSource.setInterstitialListener(new InterstitialListener() {
                /**
                 * Invoked when Interstitial Ad is ready to be shown after load function was called.
                 */
                @Override
                public void onInterstitialAdReady() {
                    Log.e("ironSouce", "ready.");
                }

                /**
                 * invoked when there is no Interstitial Ad available after calling load function.
                 */
                @Override
                public void onInterstitialAdLoadFailed(IronSourceError error) {
                    Log.e("ironSouce load fail", error.toString());
                }

                /**
                 * Invoked when the Interstitial Ad Unit is opened
                 */
                @Override
                public void onInterstitialAdOpened() {
                    Log.e("ironSouce", "open.");
                }

                /*
                 * Invoked when the ad is closed and the user is about to return to the application.
                 */
                @Override
                public void onInterstitialAdClosed() {
                    Log.e("ironSouce", "close.");
                    startActivity(context, fbIntent, fbRequstCode);
                }

                /**
                 * Invoked when Interstitial ad failed to show.
                 *
                 * @param error - An object which represents the reason of showInterstitial failure.
                 */
                @Override
                public void onInterstitialAdShowFailed(IronSourceError error) {
                    Log.e("ironSouce show fail", error.toString());
                }

                /*
                 * Invoked when the end user clicked on the interstitial ad, for supported networks only.
                 */
                @Override
                public void onInterstitialAdClicked() {
                    Log.e("ironSouce", "click.");
                }

                /**
                 * Invoked right before the Interstitial screen is about to open.
                 * NOTE - This event is available only for some of the networks.
                 * You should NOT treat this event as an interstitial impression, but rather use InterstitialAdOpenedEvent
                 */
                @Override
                public void onInterstitialAdShowSucceeded() {
                    Log.e("ironSouce", "succeeded.");
                }
            });

            if (!IronSource.isInterstitialReady()) {
                IronSource.loadInterstitial();
            }
        }
    }

    public static void ironShowInterstitial(final Activity context, final Intent intent, final int requestCode) {
        if (isShowAds) {
            fbIntent = intent;
            fbRequstCode = requestCode;
            if (adCounter == adDisplayCounter && IronSource.isInterstitialReady()) {
                adCounter = 1;
                //show the interstitial
                IronSource.showInterstitial();
            } else {
                if (adCounter == adDisplayCounter) {
                    adCounter = 1;
                }
                ironInterstitial(context);
                startActivity(context, intent, requestCode);
            }
        }else {
            startActivity(context, intent, requestCode);
        }
    }

    public static void ironShowInterstitial(final Fragment context, final Intent intent, final int requestCode) {
        if (isShowAds) {
            fbIntent = intent;
            fbRequstCode = requestCode;
            if (adCounter == adDisplayCounter && IronSource.isInterstitialReady()) {
                adCounter = 1;
                //show the interstitial
                IronSource.showInterstitial();
            } else {
                if (adCounter == adDisplayCounter) {
                    adCounter = 1;
                }
                ironInterstitial(context.getActivity());
                startActivity(context, intent, requestCode);
            }
        }else {
            startActivity(context, intent, requestCode);
        }
    }

}
