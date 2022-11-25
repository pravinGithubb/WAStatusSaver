package com.wass.wabstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.ironsource.mediationsdk.IronSource;
import com.wass.wabstatus.util.AdController;

public class HelpActivity extends AppCompatActivity {

    ImageView back;
    ImageView help1, help2, help3, help4;
    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        back = findViewById(R.id.backIV);
        back.setOnClickListener(v -> onBackPressed());

        help1 = findViewById(R.id.help1);
        help2 = findViewById(R.id.help2);
        help3 = findViewById(R.id.help3);
        help4 = findViewById(R.id.help4);

        Glide.with(this)
                .load(R.drawable.step1)
                .into(help1);

        Glide.with(this)
                .load(R.drawable.step2)
                .into(help2);

        Glide.with(this)
                .load(R.drawable.step3)
                .into(help3);

        Glide.with(this)
                .load(R.drawable.step4)
                .into(help4);


        container = findViewById(R.id.banner_container);
        if (!AdController.isLoadIronSourceAd) {
            /*admob*/
            AdController.loadBannerAd(HelpActivity.this, container);
            AdController.loadInterAd(HelpActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        AdController.adCounter++;
        if (AdController.adCounter == AdController.adDisplayCounter) {
            if (AdController.isLoadIronSourceAd){
                AdController.ironShowInterstitial(HelpActivity.this, null, 0);
            }else {
                AdController.showInterAd(HelpActivity.this, null, 0);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AdController.isLoadIronSourceAd) {
            AdController.destroyIron();
            AdController.ironBanner(HelpActivity.this, container);
            // call the IronSource onResume method
            IronSource.onResume(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AdController.isLoadIronSourceAd) {
            // call the IronSource onPause method
            IronSource.onPause(this);
        }
    }
}