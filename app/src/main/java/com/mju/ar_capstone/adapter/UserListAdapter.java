package com.mju.ar_capstone.adapter;

import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_IMG;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_SOUND;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_TEXT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.mju.ar_capstone.InventoryActivity;
import com.mju.ar_capstone.MainActivity;
import com.mju.ar_capstone.MessageDialog;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.WrappedAnchor;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.helpers.ItemTouchHelperListner;
import com.mju.ar_capstone.invenfragments.UserInvenFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> implements ItemTouchHelperListner{
    private static String TAG = UserListAdapter.class.getSimpleName();
    private FirebaseManager firebaseManager;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    public ArrayList<WrappedAnchor> userItemObjs;
    FireStorageManager fireStorageManager;
    Context context;
    Activity activity;
    int adapterType;

    /**
     * ???????????? ???????????? ???????????? ????????? AdapteronItemMove
     * @param userObjs
     * firebase rtdb?????? ????????? ????????? ?????????
     * @param fireStorageManager
     * ???????????? URI????????? ????????? ????????? ??? ????????? ?????? ?????????
     * @param firebaseManager
     * firebase ???????????? ?????? ???????????? ?????? ??????
     */
    public UserListAdapter(ArrayList<WrappedAnchor> userObjs, FireStorageManager fireStorageManager, FirebaseManager firebaseManager, int adapterType) {
        userItemObjs = userObjs;
        this.fireStorageManager = fireStorageManager;
        this.firebaseManager = firebaseManager;
        this.adapterType = adapterType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        this.activity=activity;
        View view = LayoutInflater.from(context).inflate(R.layout.host_grid_item, parent, false);
        UserListAdapter.ViewHolder vh = new UserListAdapter.ViewHolder(view) ;

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WrappedAnchor hostItem= userItemObjs.get(position);
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
                try{
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
        return userItemObjs.size();
    }

    @Override
    public boolean onItemMove(int from_position, int to_position) {
        //?????? ??????
        Collections.swap(userItemObjs, from_position, to_position);

        notifyItemMoved(from_position, to_position);
        return true;
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
            item_sound_ic.setOnClickListener(view -> {//?????? ??????
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if(mediaPlayer == null) {
                        fireStorageManager.mp3Reference.child(userItemObjs.get(pos).getTextOrPath()).getDownloadUrl().addOnSuccessListener(uri -> {
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
                            Toast.makeText(context, userItemObjs.get(pos).getTextOrPath() + context.getString(R.string.file_none_err), Toast.LENGTH_SHORT).show();
                        });
                    }else{
                        stopAudio();
                    }
                }
            });
            item_img.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    WrappedAnchor hostItem= userItemObjs.get(pos);
                    View dialogView = (View) View.inflate(context, R.layout.dialog_user, null);
                    AlertDialog.Builder dlg = new AlertDialog.Builder(context);
                    ImageView userimg = (ImageView) dialogView.findViewById(R.id.userImg);
                    fireStorageManager.imgReferece.child(hostItem.getTextOrPath()).getDownloadUrl().addOnSuccessListener(uri ->{
                        try{
                            Glide.with(context).load(uri).into(userimg);
                        }catch (IllegalArgumentException e){

                        }
                    });
                    dlg.setView(dialogView);
                    dlg.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //??????????????? ????????? ??????
                            int pos = getAdapterPosition();
                            userItemObjs.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    });
                    dlg.setPositiveButton("??????",null);
                    dlg.show();
                }
            });
            item_txt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    View dialogView = (View) View.inflate(context, R.layout.dialog_user_text, null);
                    AlertDialog.Builder dlg = new AlertDialog.Builder(context);
                    TextView usertxt = (TextView) dialogView.findViewById(R.id.userTxT);
                    String a1 = item_txt.getText().toString();
                    usertxt.setText(a1);
                    dlg.setView(dialogView);
                    dlg.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //??????????????? ????????? ??????
                            int pos = getAdapterPosition();
                            userItemObjs.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    });
                    dlg.setPositiveButton("??????",null);
                    dlg.show();
                }
            });
            item_sound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View dialogView = (View) View.inflate(context, R.layout.dialog_user, null);
                    AlertDialog.Builder dlg = new AlertDialog.Builder(context);
                    dlg.setView(dialogView);
                    dlg.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //??????????????? ????????? ??????
                            int pos = getAdapterPosition();
                            userItemObjs.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    });
                    dlg.setPositiveButton("??????",null);
                    dlg.show();


                }
            });

        }
    }

    //????????? ???????????? ??????.
    private void stopAudio() {
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }
}
