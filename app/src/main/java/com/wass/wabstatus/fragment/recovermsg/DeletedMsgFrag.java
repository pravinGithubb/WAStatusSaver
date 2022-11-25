package com.wass.wabstatus.fragment.recovermsg;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wass.wabstatus.R;
import com.wass.wabstatus.fragment.recovermsg.adapter.DeletedMsgAdapter;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgDatabaseClient;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgTable;
import com.wass.wabstatus.util.SharedPrefs;
import com.wass.wabstatus.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeletedMsgFrag extends Fragment {

    private static final int REQUESTCODE = 55;
    private RelativeLayout loaderLay, emptyLay;
    private RecyclerView rvDeletedMsg;
    private SwipeRefreshLayout refreshLayout;
    private DeletedMsgAdapter adapter;
    private boolean isMultiModeOn = false;
    private List<String> deleteUserNameList = new ArrayList<>();
    private LinearLayout deleteMultiAction, msgServiceLayout, viewAllLayout;
    private boolean onceLongSelected = false;
    private Boolean[] selectedItemList;
    private boolean isAnySelected = false;
    private List<DeletedMsgAdapter.UserViewHolder> holderList = new ArrayList<>();
    private Dialog deleteConfirmDialog;

    private int transparentColor, selectedColor;

    private SwitchCompat msgSwitch,viewAllSwitch;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE) {
            if (Utils.isNotificationServiceRunning(requireContext())) {
                msgServiceLayout.setVisibility(View.GONE);
                viewAllLayout.setVisibility(View.VISIBLE);
            } else {
                msgSwitch.setChecked(false);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recover_fragment, container, false);

        loaderLay = rootView.findViewById(R.id.loaderLay);
        emptyLay = rootView.findViewById(R.id.emptyLay);

        msgSwitch = rootView.findViewById(R.id.msgSwitch);
        msgServiceLayout = rootView.findViewById(R.id.msgServiceLayout);

        viewAllSwitch = rootView.findViewById(R.id.viewAllSwitch);
        viewAllLayout = rootView.findViewById(R.id.viewAllLayout);



        msgSwitch.setChecked(Utils.isNotificationServiceRunning(requireContext()));
        if (Utils.isNotificationServiceRunning(requireContext())) {
            msgServiceLayout.setVisibility(View.GONE);
            viewAllLayout.setVisibility(View.VISIBLE);
        }
        msgSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isChecked && !Utils.isNotificationServiceRunning(requireContext())) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
                alertDialog.setTitle(R.string.confirm);
                alertDialog.setMessage(R.string.notification_permission_msg);
                alertDialog.setPositiveButton(R.string.notification_permission_retry, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), REQUESTCODE);
                    }
                });
                alertDialog.setNegativeButton(R.string.notification_permission_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        msgSwitch.setChecked(false);
                    }
                });
                alertDialog.show();
            }
        });

        deleteConfirmDialog = new Dialog(requireActivity());
        deleteConfirmDialog.setContentView(R.layout.confirm_dialog_box);

        deleteConfirmDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));

        TextView btn0 = deleteConfirmDialog.findViewById(R.id.btn0);
        TextView btn1 = deleteConfirmDialog.findViewById(R.id.btn1);

        btn0.setOnClickListener(arg0 -> {
            deleteConfirmDialog.dismiss();
        });

        btn1.setOnClickListener(arg0 -> {
            deleteConfirmDialog.dismiss();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DeletedMsgDatabaseClient.getInstance(requireActivity()).getAppDatabase()
                            .daoDeletedMsgAccess().deleteRecordByUserName(deleteUserNameList);

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (DeletedMsgAdapter.UserViewHolder holder : holderList) {
                                holder.itemView.findViewById(R.id.ivUserIconSelected)
                                        .setVisibility(View.INVISIBLE);
                                holder.itemView.findViewById(R.id.msgRelativeLayout)
                                        .setBackgroundColor(transparentColor);
                            }
                            loadDeletedMsgFromLocalDatabase();
                        }
                    });
                }
            }).start();
        });

        transparentColor = ContextCompat.getColor(requireContext(), R.color.transparent);
        if (SharedPrefs.getAppNightDayMode(requireContext()) == AppCompatDelegate.MODE_NIGHT_YES) {
            selectedColor = ContextCompat.getColor(requireContext(), R.color.browser_actions_title_color);
        } else {
            selectedColor = ContextCompat.getColor(requireContext(), R.color.browser_actions_divider_color);
        }

        deleteMultiAction = rootView.findViewById(R.id.deleteIV);

        refreshLayout = rootView.findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(() -> {
            //reload data from local database
            refreshLayout.setRefreshing(false);
            loadDeletedMsgFromLocalDatabase();
        });
        rvDeletedMsg = rootView.findViewById(R.id.rvDeletedMsg);

        loadDeletedMsgFromLocalDatabase();
        deleteMultiAction.setOnClickListener(v -> deleteConfirmDialog.show());

        viewAllSwitch.setChecked(SharedPrefs.getShouldGetAllRecords(requireContext()));
        viewAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefs.setShouldGetAllRecords(requireContext(), isChecked);
            loadDeletedMsgFromLocalDatabase();
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    loadDataAsync async;

    public void loadDeletedMsgFromLocalDatabase() {
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


    class loadDataAsync extends AsyncTask<Void, Void, List<DeletedMsgTable>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderLay.setVisibility(View.VISIBLE);
            emptyLay.setVisibility(View.GONE);
            rvDeletedMsg.setVisibility(View.INVISIBLE);
            deleteMultiAction.setVisibility(View.GONE);
        }

        @Override
        protected List<DeletedMsgTable> doInBackground(Void... voids) {
            List<DeletedMsgTable> records;
            if(SharedPrefs.getShouldGetAllRecords(requireActivity())){
                records = DeletedMsgDatabaseClient.getInstance(requireActivity()).getAppDatabase().daoDeletedMsgAccess().getDeletedRecord();
            }else{
                records = DeletedMsgDatabaseClient.getInstance(requireActivity()).getAppDatabase().daoDeletedMsgAccess().getDeletedRecord(true);
            }
            selectedItemList = new Boolean[records.size()];
            Arrays.fill(selectedItemList, false);
            isMultiModeOn = false;
            onceLongSelected = false;
            isAnySelected = false;
            deleteUserNameList.clear();
            return records;
        }

        @Override
        protected void onPostExecute(List<DeletedMsgTable> records) {
            super.onPostExecute(records);

            new Handler().postDelayed(() -> {
                if (records != null && records.size() != 0) {
                    if (adapter == null) {
                        adapter = new DeletedMsgAdapter();
                    }
                    if (rvDeletedMsg.getAdapter() == null) {
                        rvDeletedMsg.setAdapter(adapter);
                    }
                    if (adapter != null) {
                        adapter.doRefresh(records);
                        adapter.setClickEvent((deletedMsgTable, holder, position) -> {
                            ImageView userSelectedIconIV = holder.itemView.findViewById(R.id.ivUserIconSelected);
                            if (selectedItemList[position]) {
                                userSelectedIconIV.setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.msgRelativeLayout)
                                        .setBackgroundColor(selectedColor);
                            } else {
                                userSelectedIconIV.setVisibility(View.INVISIBLE);
                                holder.itemView.findViewById(R.id.msgRelativeLayout)
                                        .setBackgroundColor(transparentColor);
                            }
                            if (deletedMsgTable != null && !isMultiModeOn) {
                                Intent myapp = new Intent(requireActivity(), WhatsappDeletedMsgByUsernameActivity.class);
                                myapp.putExtra("username", deletedMsgTable.getUsername());
                                startActivity(myapp);
                            } else if (isMultiModeOn) {
                                if (selectedItemList[position]) {
                                    if (deletedMsgTable != null && deleteUserNameList.contains(deletedMsgTable.getUsername())) {
                                        deleteUserNameList.remove(deletedMsgTable.getUsername());
                                    }
                                    holderList.remove(holder);
                                    selectedItemList[position] = false;
                                    userSelectedIconIV.setVisibility(View.INVISIBLE);
                                    holder.itemView.findViewById(R.id.msgRelativeLayout)
                                            .setBackgroundColor(transparentColor);

                                    for (boolean isSelectedOne : selectedItemList) {
                                        if (isSelectedOne) {
                                            isAnySelected = true;
                                            holderList.add(holder);
                                            break;
                                        } else {
                                            isAnySelected = false;
                                        }
                                    }
                                    if (!isAnySelected) {
                                        isMultiModeOn = false;
                                        onceLongSelected = false;
                                        deleteMultiAction.setVisibility(View.GONE);
                                    }
                                } else {
                                    selectedItemList[position] = true;
                                    holderList.add(holder);
                                    userSelectedIconIV.setVisibility(View.VISIBLE);
                                    holder.itemView.findViewById(R.id.msgRelativeLayout)
                                            .setBackgroundColor(selectedColor);

                                    if (deletedMsgTable != null) {
                                        deleteUserNameList.add(deletedMsgTable.getUsername());
                                    }
                                }
                            }
                        }, (deletedMsgTable, holder, position) -> {
                            if (!onceLongSelected) {
                                holderList.add(holder);
                                deleteMultiAction.setVisibility(View.VISIBLE);
                                deleteUserNameList.add(deletedMsgTable.getUsername());
                                ImageView userSelectedIconIV = holder.itemView.findViewById(R.id.ivUserIconSelected);
                                userSelectedIconIV.setVisibility(View.VISIBLE);
                                holder.itemView.findViewById(R.id.msgRelativeLayout)
                                        .setBackgroundColor(selectedColor);
                                selectedItemList[position] = true;
                            }
                            if (!isMultiModeOn) {
                                onceLongSelected = true;
                                isMultiModeOn = true;
                            }
                        });
                        for (DeletedMsgAdapter.UserViewHolder holder : holderList) {
                            holder.itemView.findViewById(R.id.ivUserIconSelected).setVisibility(View.INVISIBLE);
                            holder.itemView.findViewById(R.id.msgRelativeLayout)
                                    .setBackgroundColor(transparentColor);
                        }
                    }
                    rvDeletedMsg.setVisibility(View.VISIBLE);
                } else {
                    rvDeletedMsg.setVisibility(View.GONE);
                }

                loaderLay.setVisibility(View.GONE);

                if (records == null || records.size() == 0) {
                    emptyLay.setVisibility(View.VISIBLE);
                } else {
                    emptyLay.setVisibility(View.GONE);
                }

                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }

            }, 1000);
        }
    }

}
