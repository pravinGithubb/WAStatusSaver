package com.wass.wabstatus.adapter;

import android.annotation.SuppressLint;
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
import com.wass.wabstatus.PreviewActivity;
import com.wass.wabstatus.R;
import com.wass.wabstatus.model.StatusModel;
import com.wass.wabstatus.util.AdController;
import com.wass.wabstatus.util.Utils;
import java.util.ArrayList;
import java.util.List;


public class RecentAdapter extends BaseAdapter {

    Fragment context;
    List<StatusModel> arrayList;
    int width;
    LayoutInflater inflater;
    public OnCheckboxListener onCheckboxListener;

    public RecentAdapter(Fragment context, List<StatusModel> arrayList, OnCheckboxListener onCheckboxListener) {
        this.context = context;
        this.arrayList = arrayList;

        inflater = (LayoutInflater) context.requireActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = context.getResources()
                .getDisplayMetrics();
        width = displayMetrics.widthPixels; // width of the device

        this.onCheckboxListener = onCheckboxListener;
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
        @SuppressLint("ViewHolder") View grid = inflater.inflate(R.layout.row_recent, null);

        ImageView play = grid.findViewById(R.id.play);

        if (!Utils.getBack(arrayList.get(arg0).getFilePath(), "((\\.mp4|\\.webm|\\.ogg|\\.mpK|\\.avi|\\.mkv|\\.flv|\\.mpg|\\.wmv|\\.vob|\\.ogv|\\.mov|\\.qt|\\.rm|\\.rmvb\\.|\\.asf|\\.m4p|\\.m4v|\\.mp2|\\.mpeg|\\.mpe|\\.mpv|\\.m2v|\\.3gp|\\.f4p|\\.f4a|\\.f4b|\\.f4v)$)").isEmpty()) {
            play.setVisibility(View.VISIBLE);
        } else {
            play.setVisibility(View.GONE);
        }

        grid.setLayoutParams(new GridView.LayoutParams((width * 320 / 1080),
                (width * 320 / 1080)));
        ImageView imageView = grid
                .findViewById(R.id.gridImage);

        Glide.with(context.requireActivity()).load(arrayList.get(arg0).getFilePath()).into(imageView);

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
            Intent intent = new Intent(context.getActivity(), PreviewActivity.class);
            intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) arrayList);
            intent.putExtra("position", arg0);
            intent.putExtra("statusdownload", "");
//                context.startActivityForResult(intent, 10);

            AdController.adCounter++;
            if (AdController.isLoadIronSourceAd) {
                AdController.ironShowInterstitial(context, intent, 10);
            } else {
                AdController.showInterAd(context, intent, 10);
            }

        });
        return grid;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult");
    }

    public interface OnCheckboxListener {
        void onCheckboxListener(View view, List<StatusModel> list);
    }

}
