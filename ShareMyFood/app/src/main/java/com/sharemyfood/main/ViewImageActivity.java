package com.sharemyfood.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sharemyfood.R;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.commons.Commons;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

public class ViewImageActivity extends BaseActivity {

    ImageView downloader;
    private PhotoView image;
    String imageStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        imageStr = getIntent().getStringExtra("image");
        if (Commons.bImageViewState){
            ((ImageView)findViewById(R.id.download)).setVisibility(View.GONE);
            Commons.bImageViewState = false;
        }

        image=(PhotoView) findViewById(R.id.image);
        Picasso.with(getApplicationContext()).load(imageStr).into(image);

        Log.d("firebaseURL==========>", imageStr);
        downloader=(ImageView)findViewById(R.id.download);
        downloader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageToDonwloadUrl(imageStr);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Commons.food.getTitle());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle(Commons.food.getTitle());

    }

    private Drawable LoadImageFromWebOperations(String url)
    {
        try
        {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        }catch (Exception e) {
            System.out.println("Exc="+e);
            return null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return base64ToBitmap(encodedString);
        }
    }

    public Bitmap base64ToBitmap(String base64Str){
        Bitmap bitmap;
        final String pureBase64Encoded = base64Str.substring(base64Str.indexOf(",")  + 1);
        final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        return bitmap;
    }
    private void imageToDonwloadUrl(String path){

        String temp = imageStr.replace("https://firebasestorage.googleapis.com/v0/b/sharemyfood-1567613607368.appspot.com/o/","");
        String childImageStr[] = temp.split("\\?");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "ShareMyFood");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }
        File fileNameOnDevice = new File(rootPath, childImageStr[0]);
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showToast("Cannot use storage.");
            return;
        }

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://sharemyfood-1567613607368.appspot.com");
        StorageReference fileReference = firebaseStorage.getReference();
        StorageReference downloadRef = fileReference.child(childImageStr[0]);
        downloadRef.getFile(fileNameOnDevice)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("File RecylerView", "downloaded the file");
                        showToast("Downloaded:\nShareMyFood/"+childImageStr[0]);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("File RecylerView", "Failed to download the file");

                    }
                });
    }
}
