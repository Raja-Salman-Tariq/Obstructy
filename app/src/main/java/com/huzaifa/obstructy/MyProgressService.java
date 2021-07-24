package com.huzaifa.obstructy;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


public class MyProgressService extends Service {

    public static int myTime;

    FFmpeg myFfmpegObj;
    int dur;
    String[] cmd;

    Callbacks act;

    public MutableLiveData<Integer> pctg;
    IBinder bi=new LocalBinder();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            loadFFMpegBin();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        pctg=new MutableLiveData<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null)
        {
            dur=intent.getIntExtra("dur", 0);
            cmd=intent.getStringArrayExtra("cmd");

            try {
                loadFFMpegBin();
                execCmd();
            } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void execCmd() throws FFmpegCommandAlreadyRunningException {
        myFfmpegObj.execute(cmd, new ExecuteBinaryResponseHandler(){
            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
            }

            @Override
            public void onProgress(String message) {
                String arr[];

                if (message.contains("time="))
                {
                    arr=message.split("time=");

                    String _1=arr[1];
                    String _2[]=_1.split(":");
                    String[]  _3=_2[2].split(" ");

                    String secs=_3[0];
                    int hrs=(Integer.parseInt(_2[0]))*3600;

                    int min=(Integer.parseInt(_2[1]))*60;

                    float fsecs= Float.valueOf(secs);

                    float totalTime=hrs+min+fsecs;

                    pctg.setValue((int)((totalTime/dur)*100));

                    myTime=(int) (dur-totalTime);

                }
                super.onProgress(message);
            }

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Log.d("prog", "onFailure: "+message);
                pctg.setValue(100);
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    private void loadFFMpegBin() throws FFmpegNotSupportedException {
        if (myFfmpegObj==null)
        {
            myFfmpegObj=FFmpeg.getInstance(this);
        }

        myFfmpegObj.loadBinary(new LoadBinaryResponseHandler(){

            @Override
            public void onFailure() {
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });

    }


    public MyProgressService(){
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return bi;
    }


    public class LocalBinder extends Binder
    {
        public MyProgressService getServiceInstance()
        {
            return MyProgressService.this;
        }
    }

    public void registerClient(Activity act){
        this.act=(Callbacks)act;
    }

    public MutableLiveData<Integer> getPctg()
    {
        return pctg;
    }

    public interface Callbacks
    {
        void updateClient(float data);
    }
}
