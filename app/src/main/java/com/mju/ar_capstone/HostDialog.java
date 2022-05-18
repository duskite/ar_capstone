package com.mju.ar_capstone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;


public class HostDialog extends Dialog {

    private Context context;
    private CustomDialogClickListener customDialogClickListener;
    private Button dialogBtnOk, dialogBtnDelete, btnImgMakePuzzle;
    private RadioGroup dialogRdGroup;
    private RadioButton dialogRdBtnText,dialogRdBtnImg, dialogRdBtnMp3, dialogRdBtnKey, dialogRdBtnBox;

    //화면 회전에 따라 다른 레이아웃 보여줌
    private static boolean ORIENTATION = false;

    public enum AnchorType{
        text,
        image,
        mp3,
        key,
        box
    }
    AnchorType anchorType;

    public EditText dialogEdt;
    public ImageView dialogImg;
    private TextView textView, dialogKey, dialogBox;

    private LinearLayout dialogMp3;
    private ImageButton audioRecordImageBtn, mp3play;
    private TextView audioRecordText;


    private int userType;

    public HostDialog(@NonNull Context context, boolean orientation, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.ORIENTATION = orientation;
        this.context = context;
        this.customDialogClickListener = customDialogClickListener;
        this.userType = userType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ORIENTATION){ //세로일때
            setContentView(R.layout.dialog_select);
        }else{ //가로일때
            setContentView(R.layout.dialog_select_landscape);
        }
            dialogBtnOk = (Button) findViewById(R.id.dialog_btn_ok);
            dialogBtnDelete = (Button) findViewById(R.id.dialog_btn_delete);
            dialogRdGroup = (RadioGroup) findViewById(R.id.dialog_rd_group);
            dialogRdBtnText = (RadioButton) findViewById(R.id.dialog_rdbtn_text);
            dialogRdBtnImg = (RadioButton) findViewById(R.id.dialog_rdbtn_img);
            dialogRdBtnMp3 = (RadioButton) findViewById(R.id.dialog_rdbtn_mp3);
            dialogRdBtnKey = (RadioButton) findViewById(R.id.dialog_rdbtn_key);
            dialogRdBtnBox = (RadioButton) findViewById(R.id.dialog_rdbtn_box);

            dialogEdt = (EditText) findViewById(R.id.dialog_edt);
            dialogImg = (ImageView) findViewById(R.id.dialog_img);
            dialogMp3 = (LinearLayout) findViewById(R.id.dialog_mp3);

            dialogBox = (TextView) findViewById(R.id.dialog_box);
            dialogKey = (TextView) findViewById(R.id.dialog_key);

            audioRecordImageBtn = (ImageButton) findViewById(R.id.audioRecordImageBtn);
            mp3play = (ImageButton) findViewById(R.id.mp3play);
            audioRecordText = (TextView) findViewById(R.id.audioRecordText);

            textView = (TextView) findViewById(R.id.dialog_tv_test);

            btnImgMakePuzzle = (Button) findViewById(R.id.btnMakeImgPuzzle);

            dialogRdGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.dialog_rdbtn_text) {
                        textView.setText("텍스트 모드");
                        dialogEdt.setVisibility(View.VISIBLE);
                        dialogImg.setVisibility(View.GONE);
                        btnImgMakePuzzle.setVisibility(View.GONE);
                        dialogMp3.setVisibility(View.GONE);
                        dialogBox.setVisibility(View.GONE);
                        dialogKey.setVisibility(View.GONE);
                        anchorType = anchorType.text;

                        Log.d("순서", "텍스트 모드 눌림");

                    } else if (checkedId == R.id.dialog_rdbtn_img) {
                        textView.setText("이미지 모드");
                        dialogImg.setVisibility(View.VISIBLE);
                        dialogEdt.setVisibility(View.GONE);
                        dialogMp3.setVisibility(View.GONE);
                        btnImgMakePuzzle.setVisibility(View.VISIBLE);
                        dialogBox.setVisibility(View.GONE);
                        dialogKey.setVisibility(View.GONE);

                        anchorType = anchorType.image;

                        Log.d("순서", "이미지 모드 눌림");

                    } else if (checkedId == R.id.dialog_rdbtn_mp3) {
                        textView.setText("음성 녹음 모드");
                        dialogImg.setVisibility(View.GONE);
                        dialogEdt.setVisibility(View.GONE);
                        dialogMp3.setVisibility(View.VISIBLE);
                        btnImgMakePuzzle.setVisibility(View.GONE);
                        dialogBox.setVisibility(View.GONE);
                        dialogKey.setVisibility(View.GONE);

                        anchorType = anchorType.mp3;
                    }else if(checkedId == R.id.dialog_rdbtn_key){
                        textView.setText("열쇠 생성");
                        dialogImg.setVisibility(View.GONE);
                        dialogEdt.setVisibility(View.GONE);
                        dialogMp3.setVisibility(View.GONE);
                        btnImgMakePuzzle.setVisibility(View.GONE);
                        dialogBox.setVisibility(View.GONE);
                        dialogKey.setText("열쇠는 잠긴상자를 여는데 사용됩니다. 이것을 획득한 참가자는 상자를 열고\n" +
                                "게임에서 승리할 수 있습니다.");
                        dialogKey.setVisibility(View.VISIBLE);

                        anchorType = anchorType.key;
                    }else if(checkedId == R.id.dialog_rdbtn_box){
                        textView.setText("잠긴 상자 생성");
                        dialogImg.setVisibility(View.GONE);
                        dialogEdt.setVisibility(View.GONE);
                        dialogMp3.setVisibility(View.GONE);
                        btnImgMakePuzzle.setVisibility(View.GONE);
                        dialogBox.setText("잠긴 상자는 열쇠를 통해 열 수 있으며, 참가자들의 최종 목표입니다.\n" +
                                "모든 상자는 모든 열쇠로 열립니다. 배치 개수를 적절히 조정하세요.");
                        dialogBox.setVisibility(View.VISIBLE);
                        dialogKey.setVisibility(View.GONE);

                        anchorType = anchorType.box;
                    }
                }
            });


            dialogBtnOk.setOnClickListener(v -> {

                String tmpText = dialogEdt.getText().toString();
                this.customDialogClickListener.onPositiveClick(tmpText, anchorType);

                dismiss();
            });

            dialogBtnDelete.setOnClickListener(v -> {
                this.customDialogClickListener.onNegativeClick();
                dismiss();
            });

            dialogImg.setOnClickListener(v -> {
                this.customDialogClickListener.onImageClick(dialogImg);
            });


            //음성 녹음
            audioRecordImageBtn.setOnClickListener(v -> {
                this.customDialogClickListener.onRecordClick(audioRecordText, audioRecordImageBtn);
            });
            mp3play.setOnClickListener(v->{
                this.customDialogClickListener.onPlayClick();
            });

            //이미지 퍼즐
            btnImgMakePuzzle.setOnClickListener(v ->{
                this.customDialogClickListener.onImgPuzzleClick(btnImgMakePuzzle);
            });

    }

    public interface CustomDialogClickListener{
        void onPositiveClick(String tmpText, AnchorType anchorType);
        void onNegativeClick();

        void onImageClick(ImageView dialogImg);

        void onRecordClick(TextView audioRecordText, ImageButton audioRecordImageBtn);
        void onPlayClick();
        void onImgPuzzleClick(Button btnImgMakePuzzle);
    }
}
