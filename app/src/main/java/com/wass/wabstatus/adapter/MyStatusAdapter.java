package com.wass.wabstatus.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.wass.wabstatus.MyApp;
import com.wass.wabstatus.PreviewActivity;
import com.wass.wabstatus.R;
import com.wass.wabstatus.model.StatusModel;
import com.wass.wabstatus.util.AdController;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MyStatusAdapter extends BaseAdapter {

    Fragment fragment;
    List<StatusModel> arrayList;
    int width = 1080;
    LayoutInflater inflater;
    public OnCheckboxListener onCheckboxListener;

    public MyStatusAdapter(Fragment fragment, List<StatusModel> arrayList, OnCheckboxListener onCheckboxListener) {
        try {
            this.fragment = fragment;
            this.arrayList = arrayList;
            inflater = (LayoutInflater) fragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DisplayMetrics displayMetrics = fragment.getResources()
                    .getDisplayMetrics();
            width = displayMetrics.widthPixels; // width of the device
            this.onCheckboxListener = onCheckboxListener;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(
                    new RuntimeException("MyStatusAdapter  error: "+e));
        }
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int arg0, View arg1, ViewGroup arg2) {
        try {
            View grid = inflater.inflate(R.layout.row_my_status, null);
            ImageView play = grid.findViewById(R.id.play);

            if (isVideoFile(arrayList.get(arg0).getFilePath())) {
                play.setVisibility(View.VISIBLE);
            } else {
                play.setVisibility(View.GONE);
            }

            grid.setLayoutParams(new GridView.LayoutParams((width * 320 / 1080),
                    (width * 320 / 1080)));
            ImageView imageView = grid
                    .findViewById(R.id.gridImageVideo);

            if (fragment.getActivity() != null) {
                Glide.with(fragment.getActivity()).load(arrayList.get(arg0).getFilePath()).into(imageView);
            } else {
                Glide.with(MyApp.getAppContext()).load(arrayList.get(arg0).getFilePath()).into(imageView);
            }
            CheckBox checkbox = grid.findViewById(R.id.checkbox);
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                arrayList.get(arg0).setSelected(isChecked);
                if (onCheckboxListener != null) {
                    onCheckboxListener.onCheckboxListener(buttonView, arrayList);
                }
            });
            checkbox.setChecked(arrayList.get(arg0).isSelected());

            grid.setOnClickListener(view -> {
                Log.e("click", "click");
                Intent intent = new Intent(fragment.getActivity(), PreviewActivity.class);
                intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) arrayList);
                intent.putExtra("position", arg0);
                intent.putExtra("statusdownload", "download");
//				context.startActivityForResult(intent, 10);

                AdController.adCounter++;
                if (AdController.isLoadIronSourceAd) {
                    AdController.ironShowInterstitial(fragment, intent, 10);
                } else {
                    AdController.showInterAd(fragment, intent, 10);
                }

            });
            return grid;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(
                    new RuntimeException("MyStatusAdapter  error: "+e));
            return null;
        }
    }


    public interface OnCheckboxListener {
        void onCheckboxListener(View view, List<StatusModel> list);
    }

    public boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }
}
