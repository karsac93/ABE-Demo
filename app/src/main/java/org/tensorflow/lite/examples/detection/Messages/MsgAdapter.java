package org.tensorflow.lite.examples.detection.Messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.tensorflow.lite.examples.detection.Homescreen;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.SQLHandler.Msg;

import java.io.File;
import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {
    List<Msg> msgList;
    Context context;
    public static final String MSG = "msg";

    public MsgAdapter(List<Msg> msgList, Context context) {
        this.msgList = msgList;
        this.context = context;
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.msg_single_row, null);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        final Msg msg = msgList.get(position);
        holder.fileName.setText("Filename : " + msg.getFileName());

        if (msg.getType().equals(Homescreen.INTERMEDIATE)) {
            Glide.with(context).load(R.drawable.sample_image).into(holder.thumbnail);
        }
        else{
            Glide.with(context).load(new File(msg.getPath())).into(holder.thumbnail);
        }

        if(msg.getCipher() == null || msg.getCipher().length() == 0){
            holder.status.setText("Status : Unencrypted format");
        }
        else {
            holder.status.setText("Status: Encrypted");
        }

        holder.relativeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MSG, msg);
            intent.putExtras(bundle);
            context.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName, status, policy_tv;
        public ImageView thumbnail;
        public RelativeLayout relativeLayout;

        public MsgViewHolder(View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.status);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            fileName = itemView.findViewById(R.id.filename);
            relativeLayout = itemView.findViewById(R.id.rel_touch);
            policy_tv = itemView.findViewById(R.id.policy_tv);
        }
    }

    public void removeItem(int position) {
        msgList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Msg item, int position) {
        msgList.add(position, item);
        // notify item added by position
        notifyItemChanged(position, item);
    }
}
