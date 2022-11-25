package com.wass.wabstatus.fragment.recovermsg.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wass.wabstatus.R;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgTable;


import java.util.List;

public class DeletedMsgAdapter extends RecyclerView.Adapter<DeletedMsgAdapter.UserViewHolder> {

    //list data
    private List<DeletedMsgTable> listData;
    private OnItemClickListener listener;
    private OnItemLongClickListener listener1;

    public DeletedMsgAdapter() {
    }

    public void setClickEvent(OnItemClickListener listener, DeletedMsgAdapter.OnItemLongClickListener listener1) {
        this.listener = listener;
        this.listener1 = listener1;
    }

    /**
     * refresh and reload adapter data
     *
     * @param listData
     */

    public void doRefresh(List<DeletedMsgTable> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeletedMsgAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_deleted_msg, parent, false);
        DeletedMsgAdapter.UserViewHolder vh = new DeletedMsgAdapter.UserViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final DeletedMsgAdapter.UserViewHolder holder, int position) {
        DeletedMsgTable dataObject = getItem(position);

        holder.tvUserName.setText(!TextUtils.isEmpty(dataObject.getUsername()) ? dataObject.getUsername() : "");
        holder.tvLastMessage.setText(!TextUtils.isEmpty(dataObject.getMessage()) ? dataObject.getMessage() : "");
        holder.tvMessageTime.setText(!TextUtils.isEmpty(dataObject.getCreated_at()) ? dataObject.getCreated_at() : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.OnItemClick(dataObject, holder, position);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            listener1.OnItemLongClickCustom(dataObject, holder, position);
            holder.ivUserSelectedIcon.setVisibility(View.VISIBLE);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    /**
     * get single item data from list by position
     *
     * @param position
     * @return
     */
    private DeletedMsgTable getItem(int position) {
        return listData.get(position);
    }

    /**
     * user view holder
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvLastMessage, tvMessageTime;
        ImageView ivUserIcon, ivUserSelectedIcon;
        RelativeLayout msgLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            msgLayout = itemView.findViewById(R.id.msgRelativeLayout);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            ivUserSelectedIcon = itemView.findViewById(R.id.ivUserIconSelected);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(DeletedMsgTable deletedMsgTable, DeletedMsgAdapter.UserViewHolder holder, int position);
    }

    public interface OnItemLongClickListener {
        void OnItemLongClickCustom(DeletedMsgTable deletedMsgTable, DeletedMsgAdapter.UserViewHolder holder, int position);
    }

}
