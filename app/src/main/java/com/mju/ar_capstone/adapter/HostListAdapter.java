package com.mju.ar_capstone.adapter;

import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_IMG;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_SOUND;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_TEXT;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.mju.ar_capstone.MessageDialog;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.WrappedAnchor;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.io.IOException;
import java.util.ArrayList;

public class HostListAdapter extends RecyclerView.Adapter<HostListAdapter.ViewHolder> {
    private static String TAG = HostListAdapter.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    ArrayList<WrappedAnchor> hostItemObjs;
    FireStorageManager fireStorageManager;
    Context context;
    int adapterType;
    /**
     * 주최자에 리스트를 보여주는 아이템 Adapter
     * @param hostObjs
     * firebase rtdb에서 가져온 리스트 데이터
     * @param fireStorageManager
     * 데이터를 URI형태로 받아서 사용할 수 있도록 하는 매니저
     * @param firebaseManager
     * firebase 매니저를 통해 롱클릭시 삭제 구현
     */
    public HostListAdapter(ArrayList<WrappedAnchor> hostObjs, FireStorageManager fireStorageManager, FirebaseManager firebaseManager, int adapterType) {
        hostItemObjs = hostObjs;
        this.fireStorageManager = fireStorageManager;
        this.firebaseManager = firebaseManager;
        this.adapterType = adapterType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.host_grid_item, parent, false);
        HostListAdapter.ViewHolder vh = new HostListAdapter.ViewHolder(view) ;
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WrappedAnchor hostItem= hostItemObjs.get(position);
        holder.item_txt.setVisibility(View.GONE);
        holder.item_img.setVisibility(View.GONE);
        holder.item_sound.setVisibility(View.GONE);

        if(hostItem.getAnchorType() == ANCHOR_TEXT&&adapterType==ANCHOR_TEXT){
            Log.d(TAG, "onBindViewHolder ANCHOR_TEXT: ");
            holder.item_txt.setVisibility(View.VISIBLE);
            holder.item_txt.setText(hostItem.getTextOrPath());
        }else if(hostItem.getAnchorType() == ANCHOR_IMG&&adapterType==ANCHOR_IMG){
            holder.item_img.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder ANCHOR_IMG: ");
            fireStorageManager.imgReferece.child(hostItem.getTextOrPath()).getDownloadUrl().addOnSuccessListener(uri ->{
                try {
                    Glide.with(context).load(uri).into(holder.item_img);
                }catch (IllegalArgumentException e){

                }
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                Toast.makeText(context,hostItem.getTextOrPath()+context.getString(R.string.file_none_err), Toast.LENGTH_SHORT).show();
            });
        }else if(hostItem.getAnchorType() == ANCHOR_SOUND&&adapterType==ANCHOR_SOUND){
            Log.d(TAG, "onBindViewHolder ANCHOR_SOUND: ");
            holder.item_sound.setVisibility(View.VISIBLE);
            holder.item_sound_txt.setText(hostItem.getTextOrPath());
        }
    }

    @Override
    public int getItemCount() {
        return hostItemObjs.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        TextView item_txt,item_sound_txt;
        ImageView item_img;
        ImageButton item_sound_ic;
        ConstraintLayout item_sound;

        ViewHolder(View itemView) {
            super(itemView);
            item_txt =  itemView.findViewById(R.id.item_txt);
            item_sound_txt = itemView.findViewById(R.id.item_sound_txt);
            item_img = itemView.findViewById(R.id.item_img);
            item_sound = itemView.findViewById(R.id.item_sound);
            item_sound_ic = itemView.findViewById(R.id.item_sound_ic);
            item_sound_ic.setOnClickListener(view -> {//음성 실행
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if(mediaPlayer == null) {
                        fireStorageManager.mp3Reference.child(hostItemObjs.get(pos).getTextOrPath()).getDownloadUrl().addOnSuccessListener(uri -> {
                            if (uri == null) {
                                return;
                            }
                            mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setDataSource(context, uri);
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.setOnCompletionListener(mp -> stopAudio());
                        }).addOnFailureListener(e -> {
                            e.printStackTrace();
                            Toast.makeText(context, hostItemObjs.get(pos).getTextOrPath() + context.getString(R.string.file_none_err), Toast.LENGTH_SHORT).show();
                        });
                    }else{
                        stopAudio();
                    }
                }
            });
            itemView.setOnLongClickListener(view -> {//삭제 다이얼로그 부분
                int pos = getAdapterPosition();
                new MessageDialog(context, context.getString(R.string.delete_msg), (deletion) ->
                {
                    if(deletion){
                        //다이얼로그 확인시 삭제
                        Log.d(TAG, "ViewHolder long click: "+pos);
                        firebaseManager.deleteContent(hostItemObjs.get(pos).getCloudAnchorId());
                        Log.d(TAG, "ViewHolder long click: " + new Gson().toJson(hostItemObjs.get(pos)));
                        hostItemObjs.remove(pos);
                        notifyDataSetChanged();
                    }
                }
                ).show();

                return false;
            });
        }
    }
    //사운드 종료하는 함수.
    private void stopAudio() {
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }
}
