package com.mju.ar_capstone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mju.ar_capstone.R;

import java.util.ArrayList;

public class WinnerListAdapter extends RecyclerView.Adapter<WinnerListAdapter.ViewHolder> {

    private ArrayList<String> userIdList;
    private ArrayList<String> clearTimeList;

    public WinnerListAdapter(){
        userIdList = new ArrayList<>();
        clearTimeList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_winner_list, parent, false );
        ViewHolder viewHolder = new ViewHolder(context, view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String user = userIdList.get(position);
        String time = clearTimeList.get(position);

        holder.tvWinnerID.setText(user);
        holder.tvClearTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return userIdList.size();
    }

    public void setArrayData(String user, String time){
        clearTimeList.add(time);
        userIdList.add(user);
    }




    //이너 클래스 뷰홀더
    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvWinnerID, tvClearTime;

        public ViewHolder(Context context, View itemView) {
            super(itemView);

            tvWinnerID = itemView.findViewById(R.id.tvWinnerID);
            tvClearTime = itemView.findViewById(R.id.tvClearTime);
        }
    }

}
