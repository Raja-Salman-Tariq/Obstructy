package com.huzaifa.obstructy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
}