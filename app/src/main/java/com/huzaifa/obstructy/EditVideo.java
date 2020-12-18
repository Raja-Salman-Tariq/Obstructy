package com.huzaifa.obstructy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditVideo extends AppCompatActivity {

    private TextView removeObstruction, backgroundMusic,filters,extractImage;
    String selectedVideoPath;
    VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        selectedVideoPath=getIntent().getStringExtra("videoPath");
        videoView=findViewById(R.id.videoView);
        videoView.setVideoPath(selectedVideoPath);
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        removeObstruction=findViewById(R.id.removeObstruction);
        backgroundMusic=findViewById(R.id.backgroundMusic);
        filters=findViewById(R.id.filters);
        extractImage=findViewById(R.id.extractFrame);

        removeObstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postUrl= "http://192.168.100.34:5000/";

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    FileInputStream fis = new FileInputStream(new File(selectedVideoPath));
                    byte[] buf = new byte[1024];
                    int n;
                    while (-1 != (n = fis.read(buf)))
                        stream.write(buf, 0, n);

                    byte[] byteArray = stream.toByteArray();

                    RequestBody postBodyImage = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("video", "videoClip.mp4", RequestBody.create(MediaType.parse("video/mp4"), byteArray))
                            .build();

                    Toast.makeText(EditVideo.this, "Uploading To Server",Toast.LENGTH_LONG).show();

                    postRequest(postUrl, postBodyImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        extractImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVideo("");
            }

        });
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditVideo.this, "Uploading Failed",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(EditVideo.this, response.body().string(),Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    @Nullable
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID" + ".mp4");


        return mediaFile;
    }

    void saveVideo(Uri video){

        Uri uri=getOutputMediaFileUri();

    }


    void saveVideo(String url){
        Log.d("myvid", "saveVideo: started");
        // if url ! null/"", cuz a null url means local file...
        try {
            Log.d("myvid", "saveVideo: entered");
            //uncomment and use the following if url not null/""... :
//            URL u = new URL(url);
//            InputStream is = u.openStream();
            Uri u= getIntent().getData();
            InputStream is = getContentResolver().openInputStream(u);

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            Uri dest;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dest = MediaStore.Video.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                dest = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }

            Log.d("myvid", "saveVideo: ext stg st8:" + Environment.getExternalStorageState()
                + ", loc path: " + Environment.getExternalStorageDirectory().getAbsolutePath());

            Log.d("myvid", "saveVideo: dest path "+dest.getPath());

            File dir= new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),"/Obstructy");

//            File dir=new File(dest.getPath(),"/Obstructy");
            if (! dir.exists()){
                if (! dir.mkdirs()){
                    Log.d("myvid", "failed to create directory");
                    return;
                }
                else{
                    Log.d("myvid", "dir made at abs: "+dir.getAbsolutePath()
                            +", w path: "+dir.getPath()
                            +", w can path: "+dir.getCanonicalPath());
                }
            }
            else{
                Log.d("myvid", "dir existed at abs: "+dir.getAbsolutePath()
                        +", w path: "+dir.getPath()
                        +", w can path: "+dir.getCanonicalPath());
            }

//            Time now=new Time();
            String dt=(new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())).format(new Date());
            Log.d("myvid", "vid time: "+ dt);

            File mFile=new File(dir.getAbsolutePath()+File.separator+
                    dt +".mp4");

            FileOutputStream fos = new FileOutputStream(mFile);

            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

            Log.d("myvid", "saveVideo: successfull");

        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
    }
}