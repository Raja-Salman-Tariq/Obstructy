package com.huzaifa.obstructy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.huzaifa.obstructy.LoadVideo.getPath;

public class EditVideo extends AppCompatActivity {
    MediaPlayer soundCtrlr;
    MediaController soundMediaCtrl;

    TextView start, stop;
    //<=================Progress=================>//
    CircleProgressBar progress;
    ServiceConnection myConn;
    MyProgressService myServe;
    //<------------------------------------------>//


    //<=================TRIM SHEET VARIABLES=================>//
    TextView startTrim, stopTrim;
    ImageButton trimDone;
    ImageButton trimClose;
    RangeSeekBar seek;
    //<------------------------------------------>//


    //<=================SAVE SHEET VARIABLES=================>//
    ImageButton saveDone, saveClose;
    EditText saveFileName;
    //<------------------------------------------>//


    //<=================SAVING VARIABLES=================>//
    int dur;
    String filePrefix;
    static File _dest;
    //<------------------------------------------>//


    RelativeLayout trimPopup, musicpopup,savePopup;
    BottomSheetBehavior bsheetTrim, bsheetMusic,bsheetSave;
    String auFilePath, dest;
    String [] cmd;


    //<=================EDIT VIDEO LAYOUT VARIABLES=================>//
    private ImageButton removeObstruction;
    private ImageButton extractFrame;
    private ImageButton addFilter;
    private ImageButton addMusic;
    private ImageButton trimVideo;
    private ImageButton saveVideo;
    private ImageButton shareVideo;
    static String selectedVideoPath;
    VideoView videoView;
    MediaController mediaController;
    //<------------------------------------------>//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());    //FOR FACEBOOK SHARING
        setContentView(R.layout.activity_edit_video);

        filePrefix="";

        //<==================FOR FACEBOOK SHARING=================>//
        printKeyHash();
        CallbackManager cbm=CallbackManager.Factory.create();
        final ShareDialog shareDialog=new ShareDialog(this);

        selectedVideoPath=getIntent().getStringExtra("videoPath");
        setTrimViews();
        setSaveViews();
        assignSheetViews();
        setVideoView();
        assignEditViews();

        addMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bsheetMusic.setState(BottomSheetBehavior.STATE_EXPANDED);
                setListeners();
                getDest();
//                selectAudioFile(); // adding musinc called in on activity result of select audio file fun
            }
        });

        addFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsheetMusic.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        shareVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder alert=new AlertDialog.Builder(EditVideo.this);
                View mView=getLayoutInflater().inflate(R.layout.share_dialogue,null);


                ImageView whatsapp_icon=mView.findViewById(R.id.whatsapp_icon);
                ImageView twitter_icon=mView.findViewById(R.id.twitter_icon);
                ImageView facebook_icon=mView.findViewById(R.id.facebook_icon);
                ImageView instagram_icon=mView.findViewById(R.id.instagram_icon);

                alert.setView(mView);

                final AlertDialog alertDialog=alert.create();
                alertDialog.setCanceledOnTouchOutside(false);

                whatsapp_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                        if (intent != null)
                        {
                            Uri shareBody=Uri.parse(selectedVideoPath);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setPackage("com.whatsapp");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, shareBody);
                            shareIntent.setType("video/mp4");
                            startActivity(shareIntent);
                        }
                        else
                        {
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("market://details?id="+"com.instagram.android"));
                            startActivity(intent);
                        }
                        alertDialog.dismiss();
                    }
                });

                twitter_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
                        if (intent != null)
                        {
                            Uri shareBody=Uri.parse(selectedVideoPath);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setPackage("com.twitter.android");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, shareBody);
                            shareIntent.setType("video/mp4");
                            startActivity(shareIntent);
                        }
                        else
                        {
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("market://details?id="+"com.instagram.android"));
                            startActivity(intent);
                        }
                        alertDialog.dismiss();
                    }
                });

                facebook_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //for facebook sharing//
                        LoadVideo.videoFB=new ShareVideo.Builder()
                                .setLocalUrl(LoadVideo.uri)
                                .build();
                        LoadVideo.videoContentFB=new ShareVideoContent.Builder()
                                .setContentTitle("this is title")
                                .setContentDescription("this is description")
                                .setVideo(LoadVideo.videoFB)
                                .build();
                        if(shareDialog.canShow(ShareVideoContent.class))
                            shareDialog.show(LoadVideo.videoContentFB);
                        alertDialog.dismiss();
                    }
                });

                instagram_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                        if (intent != null)
                        {
                            Uri shareBody=Uri.parse(selectedVideoPath);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setPackage("com.instagram.android");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, shareBody);
                            shareIntent.setType("video/mp4");
                            startActivity(shareIntent);
                        }
                        else
                        {
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("market://details?id="+"com.instagram.android"));
                            startActivity(intent);
                        }
                        alertDialog.dismiss();
                    }
                });

                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        });

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

        saveVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsheetSave.setState(BottomSheetBehavior.STATE_EXPANDED);
                saveVideo();
            }
        });

        trimVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsheetTrim.setState(BottomSheetBehavior.STATE_EXPANDED);
                setListeners();
            }
        });

        soundCtrlr=new MediaPlayer();
        soundMediaCtrl=null;
        soundCtrlr.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("sound", "onPrepared: "+soundCtrlr.getDuration());
                soundCtrlr.start();
            }
        });

        findViewById(R.id.cross_icon_musicSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsheetMusic.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        findViewById(R.id.done_icon_musicSheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addAudio();
                } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
                    e.printStackTrace();
                }

//                startActivity(new Intent(EditVideo.this, WaitingActivity.class));
            }
        });

        findViewById(R.id.musicChange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAudioFile();
            }
        });

        findViewById(R.id.musicPlayPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditVideo.this,
                        "Working...",
                        Toast.LENGTH_SHORT).show();
                if (soundCtrlr!=null){
//                    soundCtrlr.setDataSource(EditVideo.this, auFilePath);
                    if (soundCtrlr.isPlaying()) {
                        soundCtrlr.pause();
                        findViewById(R.id.musicPlayPause).setBackgroundResource(R.drawable.ic_music_play);
                    }
                    else {
                        soundCtrlr.start();
                        findViewById(R.id.musicPlayPause).setBackgroundResource(R.drawable.ic_music_pause);
                    }
                }
                else{
                    Toast.makeText(EditVideo.this,
                            "Please select an audio file first",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setSaveViews()
    {
        saveDone=findViewById(R.id.done_icon_saveSheet);
        saveClose=findViewById(R.id.cross_icon_saveSheet);
        saveFileName=findViewById(R.id.saveFileName_saveSheet);
    }

    private void assignSheetViews()
    {
        trimPopup=findViewById(R.id.trim_bottom_sheet_dialogue);
        bsheetTrim= BottomSheetBehavior.from(trimPopup);
        bsheetTrim.setState(BottomSheetBehavior.STATE_HIDDEN);

        musicpopup=findViewById(R.id.musicpopup);
        bsheetMusic=BottomSheetBehavior.from(musicpopup);
        bsheetMusic.setState(BottomSheetBehavior.STATE_HIDDEN);

        savePopup=findViewById(R.id.saveBottomSheet);
        bsheetSave= BottomSheetBehavior.from(savePopup);
        bsheetSave.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void assignEditViews()
    {
        removeObstruction=findViewById(R.id.removeObstructionOption);
        extractFrame=findViewById(R.id.extractFrameOption);
        addFilter=findViewById(R.id.addFilterOption);
        addMusic=findViewById(R.id.addMusicOption);
        trimVideo=findViewById(R.id.trimOption);
        saveVideo=findViewById(R.id.saveOption);
        shareVideo=findViewById(R.id.shareOption);
    }

    private void setListeners()
    {
        trimDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trimVideo(seek.getSelectedMinValue().intValue()*1000,seek.getSelectedMaxValue().intValue()*1000, "");
                //INITIATE TRIMMING TASK//
                bsheetTrim.setState(BottomSheetBehavior.STATE_HIDDEN);
                initiateTask();
            }
        });

        trimClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsheetTrim.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    private void initiateTask()
    {
        //<======================================SETTING PROGRESS CIRCLE AND CALLING SERVICE FOR TRIMMING===================================>//
        progress=findViewById(R.id.prog_editVideo);
        progress.setMax(100);

        if (_dest==null)
            manageDestNDirs();
        String path=_dest.getAbsolutePath();

        final Intent myServiceInt=new Intent(EditVideo.this, MyProgressService.class);
        myServiceInt.putExtra("dur",  dur);
        myServiceInt.putExtra("cmd", cmd);
        myServiceInt.putExtra("dest", path);
        startService(myServiceInt);

        myConn=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MyProgressService.LocalBinder bi= (MyProgressService.LocalBinder)iBinder;
                myServe=bi.getServiceInstance();
                myServe.registerClient(getParent());

                final Observer<Integer> resuObsv=new androidx.lifecycle.Observer<Integer>(){

                    @Override
                    public void onChanged(Integer integer) {
                        int resu=integer;
                        progress.setVisibility(View.VISIBLE);

                        if (resu<100){
                            progress.setProgress(resu);
                        }

                        if (resu>=100)
                        {
                            progress.setProgress(100);
                            stopService(myServiceInt);

                            Toast.makeText(getApplicationContext(), "Task Successful !", Toast.LENGTH_SHORT).show();
                            selectedVideoPath=_dest.getAbsolutePath();
                            LoadVideo.uri= Uri.parse(EditVideo.selectedVideoPath);
                            progress.setVisibility(View.INVISIBLE);
                            setVideoView();
                        }
                    }
                };
                myServe.getPctg().observe(EditVideo.this, (androidx.lifecycle.Observer<? super Integer>) resuObsv);
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {    }
        };
        bindService(myServiceInt, myConn, Context.BIND_AUTO_CREATE);
        //<=================================================================================================================================>//
    }

    private void setTrimViews() {
        startTrim=findViewById(R.id.startTV_trimSheet);
        stopTrim=findViewById(R.id.stopTV_trimSheet);
        seek=findViewById(R.id.seek_trimSheet);
        trimDone=findViewById(R.id.done_icon_trimSheet);
        trimClose=findViewById(R.id.cross_icon_trimSheet);
    }

    private void setVideoView() {
        videoView=findViewById(R.id.videoView);
        videoView.setVideoPath(selectedVideoPath);
        mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.seekTo( 1 );
        videoView.start();

        //<==================================>//
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer)
            {
                dur=videoView.getDuration()/1000;
                Log.d("dur", "onPrepared: "+dur);
                startTrim.setText(getFormattedTime(0));
                stopTrim.setText(getFormattedTime(dur));
                //mediaPlayer.setLooping(true);
                seek.setRangeValues(0, dur);
                seek.setSelectedMaxValue(dur);
                seek.setSelectedMinValue(0);
                seek.setEnabled(true);
                seek.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int)minValue*1000);
                        startTrim.setText(getFormattedTime((int)bar.getSelectedMinValue()));
                        stopTrim.setText(getFormattedTime((int)bar.getSelectedMaxValue()));
                    }
                });

                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView.getCurrentPosition()>=seek.getSelectedMaxValue().intValue()*1000)
                        {
                            videoView.seekTo(seek.getSelectedMinValue().intValue()*1000);
                        }
                    }
                },1000);
            }
        });
    }

    private void printKeyHash() {
        try
        {
            PackageInfo info=getPackageManager().getPackageInfo("com.huzaifa.obstructy", PackageManager.GET_SIGNATURES);
            for(Signature signature: info.signatures)
            {
                MessageDigest md=MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
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


    void saveVideo(){
        saveDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsheetSave.setState(BottomSheetBehavior.STATE_HIDDEN);

                String fileName=saveFileName.getText().toString();
                if(fileName.equals(""))
                {
                    Toast.makeText(myServe, "Enter New File Name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try {
                        Uri u= getIntent().getData();
                        InputStream is = getContentResolver().openInputStream(u);

                        DataInputStream dis = new DataInputStream(is);

                        byte[] buffer = new byte[1024];
                        int length;

                        Uri dest;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        {
                            dest = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                        }
                        else
                        {
                            dest = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        }

                        File dir= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"/Obstructy");
                        if (! dir.exists())
                        {
                            if (! dir.mkdirs())
                            {
                                return;
                            }
                            else
                            {
                                Log.d("myvid", "dir made at abs: "+dir.getAbsolutePath() +", w path: "+dir.getPath() +", w can path: "+dir.getCanonicalPath());
                            }
                        }
                        else
                        {
                            Log.d("myvid", "dir existed at abs: "+dir.getAbsolutePath() +", w path: "+dir.getPath()+", w can path: "+dir.getCanonicalPath());
                        }

                        File mFile=new File(dir.getAbsolutePath()+File.separator+ fileName +".mp4");

                        FileOutputStream fos = new FileOutputStream(mFile);

                        while ((length = dis.read(buffer))>0) {
                            fos.write(buffer, 0, length);
                        }

                        Toast.makeText(EditVideo.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    } catch (MalformedURLException mue) {
                        Log.e("SYNC getUpdate", "malformed url error", mue);
                    } catch (IOException ioe) {
                        Log.e("SYNC getUpdate", "io error", ioe);
                    } catch (SecurityException se) {
                        Log.e("SYNC getUpdate", "security error", se);
                    }
                }
            }
        });

        saveClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void getDest() {
        File dirUpr= new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"/Obstructy");

//            File dir=new File(dest.getPath(),"/Obstructy");
        if (! dirUpr.exists()){
            if (! dirUpr.mkdirs()){
                Log.d("myvid", "failed to create directory");
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

        dest=dir.getAbsolutePath();

    }

    private void selectAudioFile() {

        Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        i.setType("audio/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(i, 3360);

    }

    private void addAudio() throws FFmpegNotSupportedException, FFmpegCommandAlreadyRunningException {

        String dt=(new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())).format(new Date());
        cmd = new String[] {"-i", selectedVideoPath, "-i", auFilePath,
                "-c", "copy", "-map", "0:0", "-map", "1:0",
                "-shortest", dest+"/BGM"+dt+".mp4"};

        bsheetMusic.setState(BottomSheetBehavior.STATE_HIDDEN);

        initiateTask();

//        Log.d("myvid","HALUUUUUUU");
        Toast.makeText(EditVideo.this, "Done", Toast.LENGTH_SHORT).show();
//        selectedVideoPath=_dest.getAbsolutePath();
        setVideoView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && requestCode==3360){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri=data.getData();
                auFilePath= getPath(getApplicationContext() ,uri );

                String tmp="";
                StringTokenizer stok=new StringTokenizer(auFilePath, "/");

                while (stok.hasMoreTokens())
                    tmp=stok.nextToken();
                Log.d("stok", "onActivityResult: "+tmp);
                TextView tv=findViewById(R.id.musicFile);
                tv.setText(tmp);

                try {
                    soundCtrlr.reset();
                    soundCtrlr.setDataSource(EditVideo.this, uri);
                    soundCtrlr.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Log.d("add", "onActivityResult: Error, version issues...");
                finish();
            }
        }
    }


    private void trimVideo(int start, int stop, String filePrefix) {

        manageDestNDirs();

        dur=(stop-start)/1000;

        cmd=new String[]{"-ss", ""+start/1000, "-y", "-i", selectedVideoPath, "-t", ""+(stop-start)/1000,
                "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                _dest.getAbsolutePath()};
    }

    private void manageDestNDirs() {
        File dirUpr= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"/Obstructy");

        if (! dirUpr.exists()){
            if (! dirUpr.mkdirs())
            {
                return;
            }
            else
            {
                Log.d("myvid", "dir made at abs: "+dirUpr.getAbsolutePath() +", w path: "+dirUpr.getPath());
            }
        }
        else
        {
            Log.d("myvid", "dir existed at abs: "+dirUpr.getAbsolutePath() +", w path: "+dirUpr.getPath());
        }

        File dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Obstructy","/Cropped");

        if (! dir.exists())
        {
            if (! dir.mkdirs())
            {
                return;
            }
            else
            {
                Log.d("myvid", "dir made at abs: "+dir.getAbsolutePath()+", w path: "+dir.getPath());
            }
        }
        else
        {
            Log.d("myvid", "dir existed at abs: "+dir.getAbsolutePath() +", w path: "+dir.getPath());
        }

        String dt=(new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())).format(new Date());
        if(filePrefix.equals(""))
        {
            _dest=new File(dir.getAbsolutePath(),dt+".mp4");
        }
        else
        {
            _dest=new File(dir.getAbsolutePath(),filePrefix+".mp4");
        }
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