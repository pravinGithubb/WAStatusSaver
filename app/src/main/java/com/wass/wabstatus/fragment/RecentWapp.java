package com.wass.wabstatus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.wass.wabstatus.HelpActivity;
import com.wass.wabstatus.R;
import com.wass.wabstatus.adapter.RecentAdapter;
import com.wass.wabstatus.model.StatusModel;
import com.wass.wabstatus.util.AdController;
import com.wass.wabstatus.util.SharedPrefs;
import com.wass.wabstatus.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecentWapp extends Fragment implements RecentAdapter.OnCheckboxListener {

    GridView imageGrid;
    ArrayList<StatusModel> f = new ArrayList<>();
    RecentAdapter myAdapter;
    ArrayList<StatusModel> filesToDelete = new ArrayList<>();
    LinearLayout actionLay, downloadIV, deleteIV;
    CheckBox selectAll;
    RelativeLayout loaderLay, emptyLay;
    SwipeRefreshLayout swipeToRefresh;
    LinearLayout sAccessBtn, nHow;
    TextView allowPermission;
    int REQUEST_ACTION_OPEN_DOCUMENT_TREE = 101;

    private boolean android_vCheck = false;
    public static boolean shouldPopulateGrid = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recent_fragment, container, false);

        loaderLay = rootView.findViewById(R.id.loaderLay);
        emptyLay = rootView.findViewById(R.id.emptyLay);

        nHow = rootView.findViewById(R.id.nHow);

        imageGrid = rootView.findViewById(R.id.WorkImageGrid);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!SharedPrefs.getandroidCheck(getActivity())) {
                SharedPrefs.setandroidCheck(getActivity(), true);
            }
            android_vCheck = SharedPrefs.getandroidCheck(getActivity());
            if (!SharedPrefs.getshouldPopulate(getActivity())) {
                if (Utils.appInstalledOrNot(requireActivity(), "com.whatsapp")) {
                    SharedPrefs.setshouldPopulate(getActivity(), true);
                }
            }
            shouldPopulateGrid = SharedPrefs.getshouldPopulate(getActivity());
        }

        nHow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdController.adCounter++;
                Log.e("navigate: ", "" + AdController.adCounter);
                if (AdController.isLoadIronSourceAd) {
                    AdController.ironShowInterstitial(getActivity(), new Intent(getActivity(), HelpActivity.class), 0);
                } else {
                    AdController.showInterAd(getActivity(), new Intent(getActivity(), HelpActivity.class), 0);
                }
            }
        });

        swipeToRefresh = rootView.findViewById(R.id.swipeToRefresh);
        swipeToRefresh.setOnRefreshListener(() -> {

            if (android_vCheck) {
                if (shouldPopulateGrid) {
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
                }
            } else if (!SharedPrefs.getWATree(getActivity()).equals("")) {
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
            }
            swipeToRefresh.setRefreshing(false);
        });

        actionLay = rootView.findViewById(R.id.actionLay);
        deleteIV = rootView.findViewById(R.id.deleteIV);
        deleteIV.setOnClickListener(view -> {

            if (!filesToDelete.isEmpty()) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getResources().getString(R.string.delete_alert))
                        .setCancelable(true)
                        .setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
                            int success = -1;
                            ArrayList<StatusModel> deletedFiles = new ArrayList<>();

                            if (android_vCheck) {
                                for (StatusModel details : filesToDelete) {
                                    File fileDirect = new File(details.getFilePath());
                                    if (fileDirect.exists()) {
                                        if (fileDirect.delete()) {
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
                            } else {
                                for (StatusModel details : filesToDelete) {
                                    DocumentFile fromTreeUri = DocumentFile.fromSingleUri(getActivity(), Uri.parse(details.getFilePath()));
                                    if (fromTreeUri != null) {
                                        if (fromTreeUri.exists()) {
                                            if (fromTreeUri.delete()) {
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
                                }
                            }
                            filesToDelete.clear();
                            for (StatusModel deletedFile : deletedFiles) {
                                f.remove(deletedFile);
                            }
                            myAdapter.notifyDataSetChanged();
                            if (success == 0) {
                                Toast.makeText(getContext(), getResources().getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
                            } else if (success == 1) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                            }
                            actionLay.setVisibility(View.GONE);
                            selectAll.setChecked(false);
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        });

        downloadIV = rootView.findViewById(R.id.downloadIV);
        downloadIV.setOnClickListener(view -> {
//            AdController.adCounter++;
//            if (AdController.isLoadIronSourceAd) {
//                AdController.ironShowInterstitial(getActivity(), null, 0);
//            } else {
//                AdController.showInterAd(getActivity(), null, 0);
//            }
            if (!filesToDelete.isEmpty()) {
                int success = -1;
                ArrayList<StatusModel> deletedFiles = new ArrayList<>();
                if (android_vCheck) {
                    for (StatusModel details : filesToDelete) {
                        File fileDirect = new File(details.getFilePath());
                        if (fileDirect.exists()) {
                            Log.e("again: ", new File(details.getFilePath()).getAbsolutePath());
                            if (Utils.download(getActivity(), details.getFilePath())) {
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
                } else {
                    for (StatusModel details : filesToDelete) {
                        DocumentFile fromTreeUri = DocumentFile.fromSingleUri(getActivity(), Uri.parse(details.getFilePath()));
                        if (fromTreeUri.exists()) {
                            Log.e("again: ", new File(details.getFilePath()).getAbsolutePath());
                            if (Utils.download(getActivity(), details.getFilePath())) {
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
                }

                filesToDelete.clear();
                for (StatusModel deletedFile : deletedFiles) {
                    f.contains(deletedFile.selected = false);
                }
                myAdapter.notifyDataSetChanged();
                if (success == 0) {
                    Toast.makeText(getContext(), getResources().getString(R.string.save_error), Toast.LENGTH_SHORT).show();
                } else if (success == 1) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                }
                actionLay.setVisibility(View.GONE);
                selectAll.setChecked(false);
            }
        });

        selectAll = rootView.findViewById(R.id.selectAll);
        selectAll.setOnCheckedChangeListener((compoundButton, b) -> {
//            AdController.adCounter++;
//            if (AdController.isLoadIronSourceAd) {
//                AdController.ironShowInterstitial(getActivity(), null, 0);
//            } else {
//                AdController.showInterAd(getActivity(), null, 0);
//            }
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
            myAdapter.notifyDataSetChanged();
        });

        sAccessBtn = rootView.findViewById(R.id.sAccessBtn);
        allowPermission = rootView.findViewById(R.id.allowPermission);
        allowPermission.setOnClickListener(v -> {
            if (android_vCheck) {
                if (shouldPopulateGrid)
                    Toast.makeText(getActivity(), "Please Install WhatsApp For Download Status!!!!!", Toast.LENGTH_SHORT).show();
            } else if (Utils.appInstalledOrNot(getActivity(), "com.whatsapp")) {
                StorageManager sm = (StorageManager) getActivity().getSystemService(Context.STORAGE_SERVICE);
                String statusDir = getWhatsupFolder();
                Intent intent = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                    Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");

                    String scheme = uri.toString();

                    scheme = scheme.replace("/root/", "/document/");

                    scheme += "%3A" + statusDir;

                    uri = Uri.parse(scheme);

                    intent.putExtra("android.provider.extra.INITIAL_URI", uri);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/document/primary%3A" + statusDir));
                }

                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                startActivityForResult(intent, REQUEST_ACTION_OPEN_DOCUMENT_TREE);

            } else {
                Toast.makeText(getActivity(), "Please Install WhatsApp For Download Status!!!!!", Toast.LENGTH_SHORT).show();
            }
        });

        if (android_vCheck) {
            if (shouldPopulateGrid) {
                populateGrid();
            }
        } else if (!SharedPrefs.getWATree(getActivity()).equals("")) {
            populateGrid();
        }
        return rootView;
    }

    loadDataAsync async;

    public void populateGrid() {
        async = new loadDataAsync();
        async.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (async != null) {
            async.cancel(true);
        }
    }

    class loadDataAsync extends AsyncTask<Void, Void, Void> {
        DocumentFile[] allFiles;
        File[] allFilesDirect;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderLay.setVisibility(View.VISIBLE);
            imageGrid.setVisibility(View.GONE);
            sAccessBtn.setVisibility(View.GONE);
            emptyLay.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            allFiles = null;
            allFilesDirect = null;
            f = new ArrayList<>();

            if (android_vCheck) {
                allFilesDirect = getFromSdcardDirect();
                if (allFilesDirect != null) {
                    for (int i = 0; i < allFilesDirect.length - 1; i++) {
                        if (!allFilesDirect[i].getName().contains(".nomedia")) {
                            f.add(new StatusModel(allFilesDirect[i].getPath()));
                        }
                    }
                }
            } else {
                allFiles = getFromSdcard();
                if (allFiles != null) {
                    for (int i = 0; i < allFiles.length - 1; i++) {
                        if (!allFiles[i].getUri().toString().contains(".nomedia")) {
                            f.add(new StatusModel(allFiles[i].getUri().toString()));
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            new Handler().postDelayed(() -> {
                if (getActivity() != null) {
                    if (f != null && f.size() != 0) {
                        myAdapter = new RecentAdapter(RecentWapp.this, f, RecentWapp.this);
                        imageGrid.setAdapter(myAdapter);
                        imageGrid.setVisibility(View.VISIBLE);
                    }
                    loaderLay.setVisibility(View.GONE);
                }

                if (f == null || f.size() == 0) {
                    emptyLay.setVisibility(View.VISIBLE);
                } else {
                    emptyLay.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }

    private File[] getFromSdcardDirect() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "WhatsApp" + File.separator + "Media" + File.separator + ".Statuses");
        if (file.isDirectory() && file.canRead()) {
            return file.listFiles();
        }
        return null;
    }

    private DocumentFile[] getFromSdcard() {
        String treeUri = SharedPrefs.getWATree(getActivity());
        DocumentFile fromTreeUri = DocumentFile.fromTreeUri(requireContext().getApplicationContext(), Uri.parse(treeUri));
        if (fromTreeUri != null && fromTreeUri.exists() && fromTreeUri.isDirectory()
                && fromTreeUri.canRead() && fromTreeUri.canWrite()) {
            return fromTreeUri.listFiles();
        } else {
            return null;
        }
    }

    public String getWhatsupFolder() {
        if (new File(Environment.getExternalStorageDirectory() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses").isDirectory()) {
            return "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses";
        } else {
            return "WhatsApp%2FMedia%2F.Statuses";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (myAdapter != null) {
            myAdapter.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == 10 && resultCode == 10) {
//            myAdapter.notifyDataSetChanged();

            DocumentFile[] allFiles = null;
            f = new ArrayList<>();
            allFiles = getFromSdcard();
            for (int i = 0; i < allFiles.length - 1; i++) {
                if (!allFiles[i].getUri().toString().contains(".nomedia")) {
                    f.add(new StatusModel(allFiles[i].getUri().toString()));
                }
            }
            myAdapter = new RecentAdapter(RecentWapp.this, f, RecentWapp.this);
            imageGrid.setAdapter(myAdapter);

            actionLay.setVisibility(View.GONE);
            selectAll.setChecked(false);
        }

        if (requestCode == REQUEST_ACTION_OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Log.e("onActivityResult: ", "" + data.getData());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    requireContext().getContentResolver()
                            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            SharedPrefs.setWATree(getActivity(), uri.toString());
            populateGrid();
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
