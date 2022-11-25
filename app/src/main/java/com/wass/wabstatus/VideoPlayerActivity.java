package com.wass.wabstatus;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.wass.wabstatus.util.Utils;


public class VideoPlayerActivity extends AppCompatActivity {

    VideoView displayVV;
    ImageView backIV;
    String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backIV = findViewById(R.id.backIV);
        backIV.setOnClickListener(view -> onBackPressed());
        if(Utils.mPath!=null){
            videoPath = Utils.mPath;
        }
        displayVV = findViewById(R.id.displayVV);

        displayVV.setVideoPath(videoPath);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(displayVV);

        displayVV.setMediaController(mediaController);

        displayVV.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        displayVV.setVideoPath(videoPath);
        displayVV.start();
    }


}
