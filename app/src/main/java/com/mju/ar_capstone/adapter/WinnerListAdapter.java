package com.mju.ar_capstone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.type.Color;
import com.google.type.DateTime;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.managefragments.WinnerListFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        int i = position + 1;
        String user = userIdList.get(position);
        String time = clearTimeList.get(position);

        holder.tvRank.setText(String.valueOf(i));
        if(i<=3){
            switch (i){
                case 1:
                    holder.tvRank.setTextColor(ContextCompat.getColor(WinnerListFragment.context, R.color.gold));
                    holder.tvRank.setTextSize(50);
                    break;
                case 2:
                    holder.tvRank.setTextColor(ContextCompat.getColor(WinnerListFragment.context, R.color.silver));
                    holder.tvRank.setTextSize(50);
                    break;
                case 3:
                    holder.tvRank.setTextColor(ContextCompat.getColor(WinnerListFragment.context, R.color.bronze));
                    holder.tvRank.setTextSize(50);
                    break;
            }

        }
        holder.tvWinnerID.setText(user);
        holder.tvClearTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return userIdList.size();
    }

    public void setArrayData(String user, String time){

        //기존에 없던 우승자 정보만 어댑터에 추가
        if(!userIdList.contains(user)){
            clearTimeList.add(time);
            userIdList.add(user);
        }
    }

    //버블정렬로 함, 시간 오름차순
    public void sortListInAdapter(){
        //yyyy-MM-dd HH:mm
        for(int i=0; i<clearTimeList.size()-1; i++){
            for(int j=0; j<clearTimeList.size()-1-i; j++){
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(clearTimeList.get(j));
                    Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(clearTimeList.get(j+1));
                    if(date.compareTo(date2) == 1){ // date가 더 크면
                        String tmpTime = clearTimeList.remove(j);
                        String tmpUser = userIdList.remove(j);

                        clearTimeList.add(j+1, tmpTime);
                        userIdList.add(j+1, tmpUser);
                    }
                }catch (ParseException e){

                }
            }
        }
    }


    //이너 클래스 뷰홀더
    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvWinnerID, tvClearTime, tvRank;

        public ViewHolder(Context context, View itemView) {
            super(itemView);

            tvRank = itemView.findViewById(R.id.tvRank);
            tvWinnerID = itemView.findViewById(R.id.tvWinnerID);
            tvClearTime = itemView.findViewById(R.id.tvClearTime);
        }
    }

}
