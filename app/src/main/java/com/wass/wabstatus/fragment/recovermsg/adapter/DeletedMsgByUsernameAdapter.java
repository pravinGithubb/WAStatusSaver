package com.wass.wabstatus.fragment.recovermsg.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wass.wabstatus.R;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgTable;

import java.util.List;

public class DeletedMsgByUsernameAdapter extends RecyclerView.Adapter<DeletedMsgByUsernameAdapter.UserViewHolder> {

    //list data
    private List<DeletedMsgTable> listData;
    private DeletedMsgByUsernameAdapter.OnItemClickListener listener;
    private DeletedMsgByUsernameAdapter.OnItemLongClickListener listener1;

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
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_deleted_msg_by_username, parent, false);
        DeletedMsgByUsernameAdapter.UserViewHolder vh = new DeletedMsgByUsernameAdapter.UserViewHolder(v);
        return vh;
    }

    public void setClickEvent(DeletedMsgByUsernameAdapter.OnItemClickListener listener, DeletedMsgByUsernameAdapter.OnItemLongClickListener listener1){
        this.listener = listener;
        this.listener1 = listener1;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {
        DeletedMsgTable dataObject = getItem(position);

        holder.tvMessage.setText(!TextUtils.isEmpty(dataObject.getMessage()) ? dataObject.getMessage() : "");
        holder.tvMessageTime.setText(!TextUtils.isEmpty(dataObject.getCreated_at()) ? dataObject.getCreated_at() : "");
//        if(dataObject.getDeleted_at().equals("red")){
//            holder.msgBkg.setBackgroundResource(R.drawable.deleted_bkg);
//        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.OnItemClick(dataObject, holder, position);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            listener1.OnItemLongClickCustom(dataObject, holder, position);
            holder.chaRelativeLayout.setVisibility(View.VISIBLE);
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

        LinearLayout msgBkg;
        TextView tvMessage, tvMessageTime;
        RelativeLayout chaRelativeLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            chaRelativeLayout=itemView.findViewById(R.id.chatRelativeLayout);
            msgBkg =itemView.findViewById(R.id.msgbkg);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(DeletedMsgTable deletedMsgTable, DeletedMsgByUsernameAdapter.UserViewHolder holder, int position);
    }

    public interface OnItemLongClickListener {
        void OnItemLongClickCustom(DeletedMsgTable deletedMsgTable, DeletedMsgByUsernameAdapter.UserViewHolder holder, int position);
    }

}
