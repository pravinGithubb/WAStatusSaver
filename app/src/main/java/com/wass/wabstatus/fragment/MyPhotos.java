package com.wass.wabstatus.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.wass.wabstatus.R;
import com.wass.wabstatus.adapter.MyStatusAdapter;
import com.wass.wabstatus.model.StatusModel;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyPhotos extends Fragment implements MyStatusAdapter.OnCheckboxListener {

    GridView imageGrid;
    ArrayList<StatusModel> f = new ArrayList<>();
    MyStatusAdapter myAdapter = null;
    int save = 10;
    ArrayList<StatusModel> filesToDelete = new ArrayList<>();
    LinearLayout actionLay, deleteIV;
    CheckBox selectAll;
    SwipeRefreshLayout swipeToRefresh;

    RelativeLayout loaderLay, emptyLay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.my_status_fragment, container, false);

        loaderLay = rootView.findViewById(R.id.loaderLay);
        emptyLay = rootView.findViewById(R.id.emptyLay);

        imageGrid = rootView.findViewById(R.id.videoGrid);

        swipeToRefresh = rootView.findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setOnRefreshListener(() -> {
            for (StatusModel deletedFile : filesToDelete) {
                f.contains(deletedFile.selected = false);
            }
            if (myAdapter != null) {
                myAdapter.notifyDataSetChanged();
            }
            filesToDelete.clear();
            selectAll.setChecked(false);
            actionLay.setVisibility(View.GONE);
            populateGrid();
            swipeToRefresh.setRefreshing(false);
        });

        populateGrid();

        actionLay = rootView.findViewById(R.id.actionLay);
        deleteIV = rootView.findViewById(R.id.deleteIV);
        deleteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filesToDelete.isEmpty()) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(getResources().getString(R.string.delete_alert))
                            .setCancelable(true)
                            .setNegativeButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    int success = -1;
                                    ArrayList<StatusModel> deletedFiles = new ArrayList<>();

                                    for (StatusModel details : filesToDelete) {
                                        File file = new File(details.getFilePath());
                                        if (file.exists()) {
                                            if (file.delete()) {
                                                deletedFiles.add(details);
                                                if (success == 0) {
                                                    return;
                                                }
                                                success = 1;
                                            } else {
                                                success = 0;
                                            }
                                        } else {
                                            success = 0;
                                        }
                                    }

                                    filesToDelete.clear();
                                    for (StatusModel deletedFile : deletedFiles) {
                                        f.remove(deletedFile);
                                    }
                                    if(myAdapter!=null){
                                        myAdapter.notifyDataSetChanged();
                                    }
                                    if (success == 0) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
                                    } else if (success == 1) {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                                    }
                                    actionLay.setVisibility(View.GONE);
                                    selectAll.setChecked(false);
                                }
                            })
                            .setPositiveButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                }
            }
        });

        selectAll = rootView.findViewById(R.id.selectAll);
        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

//                AdController.adCounter++;
//                if (AdController.isLoadIronSourceAd){
//                    AdController.ironShowInterstitial(getActivity(), null, 0);
//                }else {
//                    AdController.showInterAd(getActivity(), null, 0);
//                }

                if (!compoundButton.isPressed()) {
                    return;
                }

                filesToDelete.clear();

                for (int i = 0; i < f.size(); i++) {
                    if (!f.get(i).selected) {
                        b = true;
                        break;
                    }
                }

                if (b) {
                    for (int i = 0; i < f.size(); i++) {
                        f.get(i).selected = true;
                        filesToDelete.add(f.get(i));
                    }
                    selectAll.setChecked(true);
                } else {
                    for (int i = 0; i < f.size(); i++) {
                        f.get(i).selected = false;
                    }
                    actionLay.setVisibility(View.GONE);
                }
                if(myAdapter!=null){
                    myAdapter.notifyDataSetChanged();
                }
            }
        });

        return rootView;
    }

    public void populateGrid() {
        if (getActivity() != null && isAdded()) {
            new loadDataAsync().execute();
        }
    }

    class loadDataAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderLay.setVisibility(View.VISIBLE);
            imageGrid.setVisibility(View.GONE);
            emptyLay.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getFromSdcard();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            new Handler().postDelayed(() -> {
                if (getActivity() != null && isAdded()) {
                    myAdapter = new MyStatusAdapter(MyPhotos.this, f, MyPhotos.this);
                }
                if (myAdapter != null) {
                    imageGrid.setAdapter(myAdapter);
                    loaderLay.setVisibility(View.GONE);

                    if (f == null || f.size() == 0) {
                        emptyLay.setVisibility(View.VISIBLE);
                    } else {
                        emptyLay.setVisibility(View.GONE);
                        imageGrid.setVisibility(View.VISIBLE);
                    }
                }
            }, 1000);
        }
    }

    public void getFromSdcard() {
        File file = new File(
                Environment
                        .getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + getResources().getString(R.string.app_name) + "/Saved");
        f = new ArrayList<>();
        if (file.isDirectory() && file.listFiles() != null) {
            File[] listFile = file.listFiles();
            if (listFile != null) {
                Arrays.sort(listFile, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                for (File value : listFile) {
                    f.add(new StatusModel(value.getAbsolutePath()));
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(myAdapter!=null){
            myAdapter.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == save && resultCode == save) {
            if (myAdapter != null) {
                myAdapter.notifyDataSetChanged();
            }
            getFromSdcard();
            if (getActivity() != null && isAdded()) {
                myAdapter = new MyStatusAdapter(MyPhotos.this, f, MyPhotos.this);
                imageGrid.setAdapter(myAdapter);
                actionLay.setVisibility(View.GONE);
                selectAll.setChecked(false);
            }
        }
    }

    @Override
    public void onCheckboxListener(View view, List<StatusModel> list) {
        filesToDelete.clear();
        for (StatusModel details : list) {
            if (details.isSelected()) {
                filesToDelete.add(details);
            }
        }
        if (filesToDelete.size() == f.size()) {
            selectAll.setChecked(true);
        }
        if (!filesToDelete.isEmpty()) {
            actionLay.setVisibility(View.VISIBLE);
            return;
        }
        selectAll.setChecked(false);
        actionLay.setVisibility(View.GONE);
    }
}
