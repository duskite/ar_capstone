package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mju.ar_capstone.ArSfActivity;
import com.mju.ar_capstone.R;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class FireStorageManager {

    private FirebaseManager firebaseManager;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference tmpReferece;

    //이미지 파일명 동적 생성
    private static final String strImageID = "imageID_";
    private static final String FILE_TYPE = ".jpg";
    private String currentImageID;

    public FireStorageManager(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        tmpReferece = storageReference.child("image");
    }

    public synchronized void setFirebaseManager(FirebaseManager firebaseManager) {this.firebaseManager = firebaseManager;}

    public String makeImageFileID(){
        currentImageID = strImageID + String.valueOf(firebaseManager.getNextImageNum());
        return currentImageID;
    }

    public String getImagePath(){
        return makeImageFileID();
    }

    // 적용할 모델과 로딩된 뷰렌더러블 하나 들고옴
    public void downloadImage(Context context, String path, TransformableNode model, ViewRenderable viewRenderable){
        Log.d("다운로드", "이미지 다운로드 시작");

        tmpReferece.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("다운로드", "이미지 다운로드 성공");
                ImageView imageView = (ImageView) viewRenderable.getView().findViewById(R.id.imgView);
                if(uri == null){
                    Log.d("다운로드", "uri가 null임");
                }
                Log.d("다운로드","Uri: " + String.valueOf(uri));
                Glide.with(context).load(uri).into(imageView);
                Log.d("다운로드", model.getName());
                Log.d("다운로드", String.valueOf(System.identityHashCode(model)));

                model.setRenderable(viewRenderable);

                Log.d("다운로드", "모델에 렌더링 적용완료");
                Log.d("다운로드", "리플레이스 성공");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("이미지", "다운 실패");
            }
        });
    }

    //액티비티에서 Uri 넘겨줘야함
    public void uploadImage(Uri file){
        UploadTask uploadTask = tmpReferece.child(currentImageID + FILE_TYPE).putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("순서 업로드", "업로드 실패");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("순서 업로드", "업로드 성공");
            }
        });
    }

}
