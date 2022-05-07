package com.mju.ar_capstone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    public interface AdapterCallback {
        void DoSomeThing(String title);
    }

    private ArrayList<String> mDataTitle = null ;
    private AdapterCallback mAdapterCallback;
    Context mContext;

    public Adapter(ArrayList<String> list0,Adapter.AdapterCallback AdapterCallback, Context context) {
        mDataTitle = list0 ;
        mAdapterCallback = AdapterCallback;
        mContext=context;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_title,tv_condition,tv_trade;
        ImageView imageView;

        ConstraintLayout ll_con;

        ViewHolder(View itemView) {
            super(itemView) ;
            imageView =  itemView.findViewById(R.id.iv_image) ;
            tv_title = itemView.findViewById(R.id.tv_title) ;
            tv_condition = itemView.findViewById(R.id.tv_condition);
            tv_trade = itemView.findViewById(R.id.tv_trade) ;

            ll_con = itemView.findViewById(R.id.ll_con) ;
            ll_con.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAdapterCallback != null) {
                        mAdapterCallback.DoSomeThing(mDataTitle.get(getLayoutPosition()));
                    }
                }
            });
        }
    }

    @Override
    public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.adapter_item, parent, false) ;
        Adapter.ViewHolder vh = new Adapter.ViewHolder(view) ;
        return vh ;
    }

    @Override
    public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
        holder.tv_title.setText(mDataTitle.get(position)) ;
    }

    @Override
    public int getItemCount() {
        return mDataTitle.size() ;
    }

}