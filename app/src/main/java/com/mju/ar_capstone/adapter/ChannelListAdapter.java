package com.mju.ar_capstone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mju.ar_capstone.InventoryActivity;
import com.mju.ar_capstone.MainActivity;
import com.mju.ar_capstone.R;

import java.util.ArrayList;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder> {

    private ArrayList<String> channelIdList;
    private Context context;
    private int userType;

    public ChannelListAdapter(){
        channelIdList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_channel_list, parent, false );
        ViewHolder viewHolder = new ViewHolder(context, view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.tvChannelID.setText(channelIdList.get(position));
        holder.tvChannelID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InventoryActivity.class);
                intent.putExtra("channel", channelIdList.get(holder.getAdapterPosition()));
                intent.putExtra("userType", userType);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return channelIdList.size();
    }

    public void setArrayData(String channelId){

        //중복 방지
        if(!channelIdList.contains(channelId)){
            channelIdList.add(channelId);
        }
    }
    public void setUserType(int userType){
        this.userType = userType;
    }

    //이너 클래스 뷰홀더
    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvChannelID;

        public ViewHolder(Context context, View itemView) {
            super(itemView);

            tvChannelID = itemView.findViewById(R.id.tvChannelID);
        }
    }
}
