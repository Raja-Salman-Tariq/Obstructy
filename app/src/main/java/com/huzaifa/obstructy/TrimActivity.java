package com.huzaifa.obstructy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

public class TrimActivity extends AppCompatActivity {

    Uri uri;
    ImageView playpause;
    VideoView vidview;
    TextView start, stop;
    Button btn;

    RangeSeekBar seek;

    boolean isPlaying;
    int dur;
    String filePrefix;
    String[] cmd;
    File dest;
    String originalPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        isPlaying=false;
        playpause=findViewById(R.id.playpauseAct);
        vidview=findViewById(R.id.vidviewAct);
        start=findViewById(R.id.st);
        stop=findViewById(R.id.et);
        seek=findViewById(R.id.seek);
        btn=findViewById(R.id.trimbtn);

        Intent i=getIntent();

        if (i!=null){
            uri= Uri.parse(i.getStringExtra("uri"));
            vidview.setVideoURI(uri);
            vidview.start();
            isPlaying=true;
        }

        else{
            Log.d("getIntent", "onCreate: Trim Act: intent was null");
            finish();
        }
        
        setListeners();
    }

    private void setListeners()
    {

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout linlay=new LinearLayout(TrimActivity.this);
                linlay.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams myPams=new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                myPams.setMargins(50,0,50,100);

                final EditText inp=new EditText(TrimActivity.this);
                inp.setLayoutParams(myPams);
                inp.setGravity(Gravity.TOP| Gravity.START);
                inp.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                linlay.addView(inp, myPams);


                final AlertDialog.Builder prompt= new AlertDialog.Builder(TrimActivity.this);
                prompt.setMessage("Enter a name to save your cropped video.");
                prompt.setTitle("Trim And Save Video");
                prompt.setView(linlay);


                prompt.setPositiveButton("Trim !", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        filePrefix=inp.getText().toString();

                        trimVideo(seek.getSelectedMinValue().intValue()*1000,
                                seek.getSelectedMaxValue().intValue()*1000,
                                filePrefix);

                        Intent in=new Intent(TrimActivity.this, WaitingActivity.class);
                        in.putExtra("dur", dur);
                        Log.d("myvid", "onCreate dur val* in trimact: "+dur);
                        in.putExtra("cmd", cmd);
                        in.putExtra("dest", dest.getAbsolutePath());
                        startActivity(in);


                        finish();
                        dialogInterface.dismiss();

                    }
                });

                prompt.show();

            }
        });

        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying)
                {
                    playpause.setImageResource(R.drawable.ic_pause_foreground);
                    vidview.pause();
                    isPlaying=false;
                }
                else
                {
                    vidview.start();
                    playpause.setImageResource(R.drawable.ic_play_foreground);
                    isPlaying=true;
                }
            }
        });


        vidview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                dur=mediaPlayer.getDuration()/1000;
                start.setText(getFormattedTime(dur));
                mediaPlayer.setLooping(true);
                seek.setRangeValues(0, dur);
                seek.setSelectedMaxValue(dur);
                seek.setSelectedMinValue(0);
                seek.setEnabled(true);
                seek.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        vidview.seekTo((int)minValue*1000);
                        start.setText(getFormattedTime((int)bar.getSelectedMinValue()));
                        stop.setText(getFormattedTime((int)bar.getSelectedMaxValue()));
                    }
                });

                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (vidview.getCurrentPosition()>=seek.getSelectedMaxValue().intValue()*1000)
                        {
                            vidview.seekTo(seek.getSelectedMinValue().intValue()*1000);
                        }
                    }
                },1000);
            }
        });
    }

    private void trimVideo(int start, int stop, String filePrefix) {

        File dirUpr= new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"/Obstructy");

//            File dir=new File(dest.getPath(),"/Obstructy");
        if (! dirUpr.exists()){
            if (! dirUpr.mkdirs()){
                Log.d("myvid", "failed to create directory UPR");
                return;
            }
            else{
                Log.d("myvid", "dir made at abs: "+dirUpr.getAbsolutePath()
                        +", w path: "+dirUpr.getPath());
//                        +", w can path: "+dir.getCanonicalPath());
            }
        }
        else{
            Log.d("myvid", "dir existed at abs: "+dirUpr.getAbsolutePath()
                    +", w path: "+dirUpr.getPath());
//                    +", w can path: "+dir.getCanonicalPath());
        }

        File dir=new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)+"/Obstructy","/Cropped");

        if (! dir.exists()){
            if (! dir.mkdirs()){
                Log.d("myvid", "failed to create directory");
                return;
            }
            else{
                Log.d("myvid", "dir made at abs: "+dir.getAbsolutePath()
                        +", w path: "+dir.getPath());
//                        +", w can path: "+dir.getCanonicalPath());
            }
        }
        else{
            Log.d("myvid", "dir existed at abs: "+dir.getAbsolutePath()
                    +", w path: "+dir.getPath());
//                    +", w can path: "+dir.getCanonicalPath());
        }

        dest=new File(dir.getAbsolutePath(),filePrefix+".mp4");
        Log.d("myvid", "trimVideo dest state: "+dest.exists()+dest.getAbsolutePath());
        originalPath=getOrgPathFrmUri(getApplicationContext(), uri);

        dur=(stop-start)/1000;

        cmd=new String[]{"-ss", ""+start/1000, "-y", "-i", originalPath, "-t", ""+(stop-start)/1000,
        "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050",
        dest.getAbsolutePath()};
    }

    private String getOrgPathFrmUri(Context ctx, Uri uri)
    {
        Cursor c=null;

        try {
            String[] project = {MediaStore.Images.Media.DATA};

            c = ctx.getContentResolver().query(uri, project, null, null, null);
            int colIdx = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            c.moveToFirst();

            return c.getString(colIdx);
        } catch (Exception e){
            e.printStackTrace();
            return "";
        } finally {
            if (c!=null)
                c.close();
        }

    }


    private String getFormattedTime(int dur)
    {
        int h=dur/3600,
                rem=dur%3600,
                min=rem/60,
                sec=rem%60;

        return String.format("%02d:", h)+
                String.format("%02d:", min)+
                String.format("%02d:", sec);


    }


}