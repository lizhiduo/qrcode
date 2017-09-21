package com.example.lizhiduo.synoqrcodedemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ccb.dev.interfaces.Qrcode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isTestOpen = false;

    private boolean isRun = false;
    private boolean isOpen = false;

    private final String TAG = "syno_dev_demo";

    private static final int READ_OK = 1;


    private int PORT_SPEED = 115200;

    TextView tv, state_tv;
    Button read_btn, open_close_btn;
    EditText time_ed;
    Button test_open ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        state_tv = (TextView) findViewById(R.id.state_tv);
        read_btn = (Button) findViewById(R.id.read);
        open_close_btn = (Button) findViewById(R.id.open_close);
        time_ed = (EditText) findViewById(R.id.ed_time);
        test_open = (Button) findViewById(R.id.testOpen);


        if(isTestOpen){
            test_open.setVisibility(View.VISIBLE);
        }

        if(!isOpen){
            read_btn.setEnabled(false);
        }



    }


    @Override
    public void onClick(View view) {
        int ret;

        switch (view.getId()){
            case R.id.state_tv :
                ret = Qrcode.touch_qrcode_dev();
                state_tv.setText("getstate :" + ret);
                break;

            case R.id.tv:
                tv.setText("read:");
                break;

            case R.id.open_close:
                if(isOpen){
                    ret = Qrcode.close_qrcode_dev();
                    if(ret < 0){
                        Log.d(TAG, "close failed ...");
                        break;
                    }
                    isOpen = false;
                    isRun = false;
                    open_close_btn.setText("open");
                    read_btn.setEnabled(false);
                }else{
                    Qrcode.set_coded_format("UTF-8");
                    Qrcode.set_port_speed(PORT_SPEED);

                    ret = Qrcode.open_qrcode_dev();
                    if(ret<0){
                        Log.e(TAG, "open failed ...");
                        break;
                    }
                    isOpen = true;
                    open_close_btn.setText("close");
                    read_btn.setEnabled(true);
                }
                break;

            case R.id.testOpen:
                if(isRun){
                    Toast.makeText(this, "Thread is running ...", Toast.LENGTH_SHORT).show();
                }else{
                    new MyThread().start();
                }
                break;

            case R.id.read:
                if(!isRun){
                    tv.setText("read:");
                    new MyThread().start();
                }else{
                    Log.d(TAG, "Thread is runing...");
                    Toast.makeText(this, "is reading... \nplease wait...", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            if(msg.what == READ_OK){
                String data = ""+msg.obj;
                Log.d(TAG, "data: "+ data);

                if(data != null){
                    tv.setText("lenth: " + data.length() + "\n" + "read: " + data);
                }else {
                    tv.setText("read: " + data);
                }
            }
        }
    };

    public class MyThread extends Thread{
        @Override
        public void run() {

            if(isTestOpen){
                isRun = true;
                int count=0;

                for(int i=0; i<100; i++){
                    int ret =  Qrcode.open_qrcode_dev();
                    if(ret<0){
                        Log.d(TAG, "open fail...");
                    }else{
                        count++;
                        Log.d(TAG, "open successful..." + count);
                        Qrcode.close_qrcode_dev();
                    }
                    try{
                        Thread.sleep( 500); //ms
                    }catch(Exception e){
                        Log.d(TAG,""+e);
                    }

                }
                isRun = false;

            }else{
                int wait_time ;

                isRun = true;

                if(time_ed.getText().length()  != 0){
                    String str = time_ed.getText().toString();
                    wait_time = Integer.parseInt(str);
                    if(wait_time > 25500){
                        wait_time = 25500;
                        Log.d(TAG, "time is too long, set time=25500");
                    }
                }else{
                    wait_time = -1;
                }

                Log.d(TAG, "waittime: "+ wait_time);


                String rdata = Qrcode.read_qrcode_dev(wait_time); //ms

                //
                Message message = Message.obtain();
                message.obj = rdata;
                message.what = READ_OK;
                handler.sendMessage(message);

                isRun = false;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isOpen){
            Log.d(TAG, "close dev...");
            Qrcode.close_qrcode_dev();
        }
    }
}