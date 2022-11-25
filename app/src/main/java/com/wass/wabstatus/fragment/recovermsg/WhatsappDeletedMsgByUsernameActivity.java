package com.wass.wabstatus.fragment.recovermsg;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ironsource.mediationsdk.IronSource;
import com.wass.wabstatus.R;
import com.wass.wabstatus.fragment.recovermsg.adapter.DeletedMsgByUsernameAdapter;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgDatabaseClient;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgTable;
import com.wass.wabstatus.util.AdController;
import com.wass.wabstatus.util.SharedPrefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhatsappDeletedMsgByUsernameActivity extends AppCompatActivity {

    private ImageView back;
    private TextView tvActivityTitle;
    private LinearLayout container;
    private RelativeLayout loaderLay, emptyLay;
    private RecyclerView rvDeletedMsgByUsername;
    private SwipeRefreshLayout refreshLayout;
    private DeletedMsgByUsernameAdapter adapter;
    private String username = null;

    private boolean isMultiModeOn = false;
    private List<DeletedMsgTable> deletedMsgTablesList = new ArrayList<>();
    private LinearLayout deleteMultiAction;
    private boolean onceLongSelected = false;
    private Boolean[] selectedItemList;
    private boolean isAnySelected = false;
    private List<DeletedMsgByUsernameAdapter.UserViewHolder> holderList = new ArrayList<>();

    private int transparentColor, selectedColor;
    private Dialog deleteConfirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whatsapp_deleted_msg_by_username);

        back = findViewById(R.id.backIV);
        back.setOnClickListener(v -> onBackPressed());

        deleteConfirmDialog = new Dialog(this);
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
                    DeletedMsgDatabaseClient.getInstance(WhatsappDeletedMsgByUsernameActivity.this).getAppDatabase()
                            .daoDeletedMsgAccess().deleteRecordAll(deletedMsgTablesList);

                    WhatsappDeletedMsgByUsernameActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (DeletedMsgByUsernameAdapter.UserViewHolder holder : holderList) {
                                holder.itemView.findViewById(R.id.chatRelativeLayout)
                                        .setBackgroundColor(transparentColor);
                            }
                            loadDeletedMsgFromLocalDatabase();
                        }
                    });
                }
            }).start();
        });

        transparentColor = ContextCompat.getColor(this, R.color.transparent);
        selectedColor = ContextCompat.getColor(this, R.color.selectedList);

        deleteMultiAction = findViewById(R.id.deleteIV);

        tvActivityTitle = findViewById(R.id.tvActivityTitle);

        username = getIntent().getExtras().getString("username", null);
        if (!TextUtils.isEmpty(username)) {
            tvActivityTitle.setText("WA Deleted Msg From " + username);
        }

        loaderLay = findViewById(R.id.loaderLay);
        emptyLay = findViewById(R.id.emptyLay);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //reload data from local database
                loadDeletedMsgFromLocalDatabase();
            }
        });
        rvDeletedMsgByUsername = findViewById(R.id.rvDeletedMsgByUsername);

        loadDeletedMsgFromLocalDatabase();

        deleteMultiAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirmDialog.show();
            }
        });

        container = findViewById(R.id.banner_container);
        if (!AdController.isLoadIronSourceAd) {
            /*admob*/
            AdController.loadBannerAd(WhatsappDeletedMsgByUsernameActivity.this, container);
            AdController.loadInterAd(WhatsappDeletedMsgByUsernameActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        AdController.adCounter++;
        if (AdController.adCounter == AdController.adDisplayCounter) {
            if (AdController.isLoadIronSourceAd) {
                AdController.ironShowInterstitial(WhatsappDeletedMsgByUsernameActivity.this, null, 0);
            } else {
                AdController.showInterAd(WhatsappDeletedMsgByUsernameActivity.this, null, 0);
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
            AdController.ironBanner(WhatsappDeletedMsgByUsernameActivity.this, container);
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
            rvDeletedMsgByUsername.setVisibility(View.INVISIBLE);
            deleteMultiAction.setVisibility(View.INVISIBLE);
        }

        @Override
        protected List<DeletedMsgTable> doInBackground(Void... voids) {
            List<DeletedMsgTable> records = new ArrayList<DeletedMsgTable>();
            if (!TextUtils.isEmpty(username)) {
                if(SharedPrefs.getShouldGetAllRecords(WhatsappDeletedMsgByUsernameActivity.this))
                {
                    records = DeletedMsgDatabaseClient.
                            getInstance(WhatsappDeletedMsgByUsernameActivity.this).
                            getAppDatabase().daoDeletedMsgAccess().
                            getAllDeletedRecordByUsername(username);

                }else{

                    records = DeletedMsgDatabaseClient.
                            getInstance(WhatsappDeletedMsgByUsernameActivity.this).
                            getAppDatabase().daoDeletedMsgAccess().
                            getAllDeletedRecordByUsername(username, true);
                }
            }
            selectedItemList = new Boolean[records.size()];
            Arrays.fill(selectedItemList, false);
            isMultiModeOn = false;
            onceLongSelected = false;
            isAnySelected = false;
            deletedMsgTablesList.clear();
            return records;
        }

        @Override
        protected void onPostExecute(List<DeletedMsgTable> records) {
            super.onPostExecute(records);

            new Handler().postDelayed(() -> {
                if (records != null && records.size() != 0) {
                    if (adapter == null) {
                        adapter = new DeletedMsgByUsernameAdapter();
                    }
                    if (rvDeletedMsgByUsername.getAdapter() == null) {
                        rvDeletedMsgByUsername.setAdapter(adapter);
                    }
                    if (adapter != null) {
                        adapter.doRefresh(records);
                        adapter.setClickEvent((deletedMsgTable, holder, position) -> {
                            RelativeLayout chatRelativeLayout = holder.itemView.findViewById(R.id.chatRelativeLayout);
                            if (selectedItemList[position]) {
                                chatRelativeLayout.setBackgroundColor(selectedColor);
                            } else {
                                chatRelativeLayout.setBackgroundColor(transparentColor);
                            }
                            if (isMultiModeOn) {
                                if (selectedItemList[position]) {
                                    if (deletedMsgTable != null) {
                                        deletedMsgTablesList.remove(deletedMsgTable);
                                    }
                                    holderList.remove(holder);
                                    selectedItemList[position] = false;
                                    chatRelativeLayout.setBackgroundColor(transparentColor);
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
                                    chatRelativeLayout.setBackgroundColor(selectedColor);
                                    if (deletedMsgTable != null) {
                                        deletedMsgTablesList.add(deletedMsgTable);
                                    }
                                }
                            }
                        }, (deletedMsgTable, holder, position) -> {
                            if (!onceLongSelected) {
                                holderList.add(holder);
                                deleteMultiAction.setVisibility(View.VISIBLE);
                                if (deletedMsgTable != null) {
                                    deletedMsgTablesList.add(deletedMsgTable);
                                }
                                RelativeLayout chatRelativeLayout = holder.itemView.findViewById(R.id.chatRelativeLayout);
                                chatRelativeLayout.setBackgroundColor(selectedColor);
                                selectedItemList[position] = true;
                            }
                            if (!isMultiModeOn) {
                                onceLongSelected = true;
                                isMultiModeOn = true;
                            }
                        });
                        for (DeletedMsgByUsernameAdapter.UserViewHolder holder : holderList) {
                            holder.itemView.findViewById(R.id.chatRelativeLayout).setBackgroundColor(transparentColor);
                        }

                    }
                    rvDeletedMsgByUsername.setVisibility(View.VISIBLE);
                } else {
                    rvDeletedMsgByUsername.setVisibility(View.GONE);
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