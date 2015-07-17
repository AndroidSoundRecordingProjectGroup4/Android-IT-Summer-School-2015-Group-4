package com.example.leebo.recordsounddemo2;

import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MainActivity extends ActionBarActivity {

    private ImageButton stop_b;
    private ImageButton cancel_b;
    private ImageButton start_img_b;
    private Button delete_b;
    private Button play_b;
    private ListView myListView1;
    private TextView myText;
    private Chronometer timer_text;
    private File myRecFile;
    private File myRecDir;
    private File myPlayFile;

    private ArrayList<String> recordFiles;
    private ArrayAdapter<String> adapter;

//    private MediaRecorder mr;
    private MediaPlayer mp;
    private RecordClass rc=new RecordClass(MainActivity.this);
    private boolean sdCardExit;
    private boolean isRecording;
    private boolean isPlaying;
    public int recLen=0;

//    final Handler handler = new Handler(){
//        public void handleMessage(Message msg){
//            switch (msg.what){
//                case 1:
//                    recLen++;
//                    timer_text.setText(recLen);
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };
//    TimerTask task = new TimerTask() {
//        @Override
//        public void run() {
//            Message message = new Message();
//            message.what = 1;
//            handler.sendMessage(message);
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


        myListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                play_b.setEnabled(true);
                delete_b.setEnabled(true);

                myPlayFile = new File(myRecDir.getAbsolutePath()+File.separator+((CheckedTextView)view).getText());
                myText.setText("what you choose is"+((CheckedTextView) view).getText());
            }
        });

    }

    private void getRecordFiles(){
        recordFiles=new ArrayList<String>();
        if (sdCardExit){
            File files[]=myRecDir.listFiles();
            if (files!=null){
                for (int i=0;i<files.length;i++){
                    if(files[i].getName().indexOf(".")>=0){
                        /*读取.arm文件*/
                        String fileS = files[i].getName().substring(files[i].getName().indexOf("."));
                        if(fileS.toLowerCase().equals(".amr"))
                            recordFiles.add(files[i].getName());
                    }
                }
            }
        }
    }

    /*init*/
    public void init(){
        stop_b=(ImageButton)findViewById(R.id.stop_b);
        cancel_b=(ImageButton)findViewById(R.id.cancel_b);
        start_img_b=(ImageButton)findViewById(R.id.start_img_b);
        stop_b.setOnTouchListener(TouchDark);
        cancel_b.setOnTouchListener(TouchDark);
        start_img_b.setOnTouchListener(TouchDark);
        delete_b=(Button)findViewById(R.id.delete_b);
        play_b=(Button)findViewById(R.id.play_b);
        myListView1=(ListView)findViewById(R.id.listview1);
        myText=(TextView)findViewById(R.id.tips);
        timer_text=(Chronometer)findViewById(R.id.timer_text);

        timer_text.setFormat("%s");

        stop_b.setEnabled(false);
        delete_b.setEnabled(false);
        play_b.setEnabled(false);

        sdCardExit= Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(sdCardExit)
            myRecDir=Environment.getExternalStorageDirectory();
        getRecordFiles();

        adapter=new ArrayAdapter<String>(this,R.layout.my_listview,recordFiles);
        /*将ArrayAdapter存入ListView对象中*/
        myListView1.setAdapter(adapter);
    }

    /*start record*/
    public void start_record_b(View v){
        try {

            if (!sdCardExit){
                Toast.makeText(MainActivity.this,"There is no SD Card",Toast.LENGTH_SHORT).show();
                return;
            }
            timer_text.setBase(SystemClock.elapsedRealtime());
            timer_text.stop();
            timer_text.start();//start to timer
            myRecFile=File.createTempFile("Test",".amr",myRecDir);

            rc.start_record(myRecFile);

            myText.setText("recording...");

            start_img_b.setEnabled(false);
            play_b.setEnabled(false);
            stop_b.setEnabled(true);
            delete_b.setEnabled(false);
            cancel_b.setEnabled(true);


        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /*stop and save the record*/
    public void stop_record_b(View v){
        if (myRecFile!=null){
            /*将录音名给Adapter*/
            adapter.add(myRecFile.getName());

            rc.stop_record();//use the method of RecordClass

            myText.setText("Stop:" + myRecFile.getName());//show the name
            start_img_b.setEnabled(true);
            stop_b.setEnabled(false);
            cancel_b.setEnabled(false);
            timer_text.stop();
        }
    }
    /*start to play the record*/
    public void start_play_b(View v){
        if (myPlayFile!=null&&myPlayFile.exists()) {
            try {
                mp = new MediaPlayer();

                mp.setDataSource(myPlayFile.getAbsolutePath());
                mp.prepare();
                mp.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            Toast.makeText(MainActivity.this,"There is no record~",Toast.LENGTH_SHORT).show();
    }
    /*cancel to save the record*/
    public void cancel_record_b(View v){
        rc.cancel_record();
        stop_b.setEnabled(false);
        start_img_b.setEnabled(true);
        myText.setText("cancel recording");

        timer_text.setBase(SystemClock.elapsedRealtime());
        timer_text.stop();
    }
    /*delete this record*/
    public void delete_record_b(View v){
        if (myPlayFile!=null){
            adapter.remove(myPlayFile.getName());
            if (myPlayFile.exists())
                myPlayFile.delete();
            myText.setText("Delete successfully");
        }
    }

    /*change the color of ImageButton when touch it*/
    public static final View.OnTouchListener TouchLight = new View.OnTouchListener() {
        public final float[] BT_SELECTED = new float[]{1,0,0,0,50,0,1,0,0,50,0,0,1,0,50,0,0,0,1,0};
        public final float[] BT_NOT_SELECTED = new float[]{1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0};
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                v.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }else if(event.getAction()==MotionEvent.ACTION_UP) {
                v.getBackground().setColorFilter(
                        new ColorMatrixColorFilter(BT_NOT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }
            return false;
        }
    };
    public static final View.OnTouchListener TouchDark = new View.OnTouchListener() {
        public final float[] BT_SELECTED = new float[] {1,0,0,0,-50,0,1,0,0,-50,0,0,1,0,-50,0,0,0,1,0};
        public final float[] BT_NOT_SELECTED = new float[] {1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0};
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.getBackground().setColorFilter(
                        new ColorMatrixColorFilter(BT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.getBackground().setColorFilter(
                        new ColorMatrixColorFilter(BT_NOT_SELECTED));
                v.setBackgroundDrawable(v.getBackground());
            }
            return false;
        }
    };




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
