package com.mju.ar_capstone.helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mju.ar_capstone.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FireStorageManager {

    private FirebaseManager firebaseManager;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    public StorageReference imgReferece;
    public StorageReference mp3Reference;

    //이미지 파일명 동적 생성
    private static final String strImageID = "imageID_";
    private static final String JPG_TYPE = ".jpg";
    private String currentImageID;

    //mp3 파일명 동적 생성
    private static final String strMp3ID = "mp3ID_";
    private static final String MP3_TYPE = ".3gp";
    private String currentMp3ID;
    private ArrayList<Uri> mp3UriList = new ArrayList<>();

    public FireStorageManager(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        imgReferece = storageReference.child("image");
        mp3Reference = storageReference.child("mp3");
    }
    public FireStorageManager(String channel){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        imgReferece = storageReference.child("image").child(channel);
        mp3Reference = storageReference.child("mp3").child(channel);
    }

    public synchronized void setFirebaseManager(FirebaseManager firebaseManager) {this.firebaseManager = firebaseManager;}

    public void deleteAnchor(String text_or_path){
        if(text_or_path != null){
            if(text_or_path.contains(JPG_TYPE)){
                imgReferece.child(text_or_path).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("앵커 삭제", "이미지 삭제 성공");
                    }
                });
            }else if(text_or_path.contains(MP3_TYPE)){
                mp3Reference.child(text_or_path).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("앵커 삭제", "음성 삭제 성공");
                    }
                });
            }
        }
    }
    //저장소 공간에서 삭제해버림
    //지금으로써는 잘 돌아감
    public void deleteChannelStorage(){
        // 폴더쨰로 날릴 수 없어서 고민중
        // 파일 하나하나 삭제해야 하는데, 삭제할 데이터가 많을때 사용자 런타임동안 지워지는지 체크 필요함
        imgReferece.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                List<StorageReference> list = listResult.getItems();
                for(int i=0; i<list.size(); i++){
                    list.get(i).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("파일 삭제", "삭제 성공");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("파일 삭제", "삭제 실패");
                        }
                    });
                }
            }
        });
        mp3Reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                List<StorageReference> list = listResult.getItems();
                for(int i=0; i<list.size(); i++){
                    list.get(i).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("파일 삭제", "삭제 성공");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("파일 삭제", "삭제 실패");
                        }
                    });
                }
            }
        });
    }

    public String makeImageFileID(){
        currentImageID = strImageID + String.valueOf(firebaseManager.getNextImageNum());
        return currentImageID;
    }

    public String getImagePath(){
        return makeImageFileID();
    }

    public String makeMp3FileID(){
        currentMp3ID = strMp3ID + String.valueOf(firebaseManager.getNextMp3Num());
        return currentMp3ID;
    }
    public String getMp3Path(){
        return makeMp3FileID();
    }
    public ArrayList<Uri> getMp3ListUri(){
        return mp3UriList;
    }
    public void clearMp3UriList(){
        mp3UriList.clear();
    }

    // 적용할 모델과 로딩된 뷰렌더러블 하나 들고옴
    public void downloadImage(Context context, String path, TransformableNode model, ViewRenderable viewRenderable){
        Log.d("다운로드", "이미지 다운로드 시작");

        imgReferece.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        UploadTask uploadTask = imgReferece.child(currentImageID + JPG_TYPE).putFile(file);

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

    //액티비티에서 Uri 넘겨줘야함
    public void uploadMp3(String fileName) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
        }catch (FileNotFoundException e){
            Log.d("mp3", "파일이 없음");
        }
        UploadTask uploadTask = mp3Reference.child(currentMp3ID + MP3_TYPE).putStream(inputStream);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("3gp", "업로드 실패");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("3gp", "업로드 성공");
            }
        });
    }

    // 적용할 모델과 로딩된 뷰렌더러블 하나 들고옴
    public void downloadMp3(Context context, String path){
        Log.d("음성다운", "음성 다운로드 시작");

        mp3Reference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("음성다운", String.valueOf(uri));
                Log.d("음성다운", "다운 성공");
                mp3UriList.add(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("음성다운", "다운 실패");
            }
        });
    }

}
