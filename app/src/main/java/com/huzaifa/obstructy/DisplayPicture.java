package com.huzaifa.obstructy;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;

public class DisplayPicture extends AppCompatActivity {

    ImageButton back;
    Button save;
    ImageView displayPicture;
    byte[] value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_picture);

        value = getIntent().getExtras().getByteArray("DecodedBytes");

        back=findViewById(R.id.displayPictureBack);
        save=findViewById(R.id.saveImage);
        displayPicture=findViewById(R.id.displayPicture);
        Glide.with(DisplayPicture.this).load(value).fitCenter().into(displayPicture);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photo=new File(Environment.getExternalStorageDirectory(), "Unobstructed.jpg");

                if (photo.exists()) {
                    photo.delete();
                }

                try {
                    FileOutputStream fos=new FileOutputStream(photo.getPath());
                    Toast.makeText(DisplayPicture.this, "Saved", Toast.LENGTH_SHORT).show();
                    fos.write(value);
                    fos.close();
                }
                catch (java.io.IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }
            }
        });
    }
}