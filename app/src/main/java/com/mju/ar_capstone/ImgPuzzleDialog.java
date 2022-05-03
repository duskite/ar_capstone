package com.mju.ar_capstone;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImgPuzzleDialog extends Dialog {

    private PuzzleDialogClickListener puzzleDialogClickListener;

    ImageButton imgBtn0, imgBtn1, imgBtn2, imgBtn3;
    ArrayList<Bitmap> tmpBitmap;

    public ImgPuzzleDialog(@NonNull Context context, ArrayList<Bitmap> tmpBitmap, PuzzleDialogClickListener puzzleDialogClickListener) {
        super(context);
        this.puzzleDialogClickListener = puzzleDialogClickListener;
        this.tmpBitmap = tmpBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_img_puzzle);

        imgBtn0 = findViewById(R.id.table_0);
        imgBtn1 = findViewById(R.id.table_1);
        imgBtn2 = findViewById(R.id.table_2);
        imgBtn3 = findViewById(R.id.table_3);

        Glide.with(getContext()).load(tmpBitmap.get(0)).into(imgBtn0);
        Glide.with(getContext()).load(tmpBitmap.get(1)).into(imgBtn1);
        Glide.with(getContext()).load(tmpBitmap.get(2)).into(imgBtn2);
        Glide.with(getContext()).load(tmpBitmap.get(3)).into(imgBtn3);

        imgBtn0.setOnClickListener(v -> {
            this.puzzleDialogClickListener.onImgBtn0Click(tmpBitmap.get(0));

            dismiss();
        });
        imgBtn1.setOnClickListener(v -> {
            this.puzzleDialogClickListener.onImgBtn1Click(tmpBitmap.get(1));

            dismiss();
        });
        imgBtn2.setOnClickListener(v -> {
            this.puzzleDialogClickListener.onImgBtn2Click(tmpBitmap.get(2));

            dismiss();
        });
        imgBtn3.setOnClickListener(v -> {
            this.puzzleDialogClickListener.onImgBtn3Click(tmpBitmap.get(3));

            dismiss();
        });

    }

    public interface PuzzleDialogClickListener {

        void onImgBtn0Click(Bitmap bitmap);
        void onImgBtn1Click(Bitmap bitmap);
        void onImgBtn2Click(Bitmap bitmap);
        void onImgBtn3Click(Bitmap bitmap);

    }
}
