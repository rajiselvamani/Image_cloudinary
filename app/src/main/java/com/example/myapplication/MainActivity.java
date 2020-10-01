package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;


import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.AlgorithmParameterGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    String[] size = { "30-40", "300-450", "1000-800" };
    ImageButton upload;
    GridView simpleList;
    ArrayList PhotoList=new ArrayList<Item>();
    private static final int PERMISSION_CODE =1;
    private static final int PICK_IMAGE=1;
    Integer width,height;
    URL imageURL;

    String filePath;
    Map config = new HashMap();
    private void configCloudinary() {
        config.put("cloud_name", "dfmw65bqd");
        config.put("api_key", "913985126868684");
        config.put("api_secret", "FF5vVsKxif4w5NswTuDepxkVdUE");
        MediaManager.init(MainActivity.this, config);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spin = (Spinner) findViewById(R.id.spinner1);
        configCloudinary();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, size);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(this);
        simpleList = (GridView) findViewById(R.id.simpleGridView);
        PhotoList.add(new Item("Photo 1",R.drawable.ic_launcher_background));
        PhotoList.add(new Item("Photo 1",R.drawable.ic_launcher_foreground));

        MyAdapter myAdapter=new MyAdapter(this,R.layout.activity_grid_view_items,PhotoList);
        simpleList.setAdapter(myAdapter);
        upload=(ImageButton) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_image();

            }
        });
    }
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(), "Selected Size: "+size[position] , Toast.LENGTH_SHORT).show();
        width=Integer.parseInt(size[position].split("-")[0]);
        height=Integer.parseInt(size[position].split("-")[1]);
        try {
            downloadimage(width,height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO - Custom Code
    }
    private void upload_image()
    {
        requestPermission();
        uploadToCloudinary(filePath);
    }
    private void requestPermission(){
        if(ContextCompat.checkSelfPermission
                (MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ){
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessTheGallery();
            }else {
                Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void accessTheGallery(){
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //get the image's file location
        filePath = getRealPathFromUri(data.getData(), MainActivity.this);

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            try {
                //set picked image to the mProfile
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                //mProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromUri(Uri imageUri, Activity activity){
        Cursor cursor = activity.getContentResolver().query(imageUri, null, null, null, null);

        if(cursor==null) {

            return imageUri.getPath();

        }else{
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void uploadToCloudinary(String filePath) {

        MediaManager.get().upload(filePath).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

                Toast.makeText(MainActivity.this,"start",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Toast.makeText(MainActivity.this,"uploading...",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Toast.makeText(MainActivity.this,"image URL: "+resultData.get("url").toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Toast.makeText(MainActivity.this,"err"+error.getDescription(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Toast.makeText(MainActivity.this,"reschedule",Toast.LENGTH_LONG).show();
            }
        }).dispatch();
    }
    private void downloadimage(int width,int height) throws IOException {

        ReadableByteChannel rbc = Channels.newChannel(imageURL.openStream());
        FileOutputStream fos = new FileOutputStream("image.jpg");
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }
    }
