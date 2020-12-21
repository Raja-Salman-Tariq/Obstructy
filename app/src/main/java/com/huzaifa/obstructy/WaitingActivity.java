package com.huzaifa.obstructy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.dinuscxj.progressbar.CircleProgressBar;

public class WaitingActivity extends AppCompatActivity {

    CircleProgressBar prog;
    int dur;
    String[] cmd;
    String path;

    ServiceConnection myConn;
    MyProgressService myServe;
    Integer resu; //result


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        prog=findViewById(R.id.prog);
        prog.setMax(100);

        final Intent i=getIntent();

        if (i==null){
            Log.d("WaitIntent", "onCreate: Error ! Intent was null !");
            finish();
        }
        else{
            dur=i.getIntExtra("dur", 0);
            cmd=i.getStringArrayExtra("cmd" );
            path=i.getStringExtra("dest");

            final Intent myServiceInt=new Intent(WaitingActivity.this, MyProgressService.class);
            myServiceInt.putExtra("dur",  dur);
            Log.d("myvid", "onCreate dur val*: "+dur);
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
                            resu=integer;

                            if (resu<100){
                                prog.setProgress(resu);
                            }

                            if (resu>=100) {
                                prog.setProgress(100);
                                stopService(myServiceInt);

                                Toast.makeText(getApplicationContext(), "Cropping Successful !",
                                        Toast.LENGTH_SHORT).show();

//                                startActivity(new Intent(WaitingActivity.this, EditVideo.class));
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
                    };

                    myServe.getPctg().observe(WaitingActivity.this, (androidx.lifecycle.Observer<? super Integer>) resuObsv);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };

            bindService(myServiceInt, myConn, Context.BIND_AUTO_CREATE);

        }
    }
}