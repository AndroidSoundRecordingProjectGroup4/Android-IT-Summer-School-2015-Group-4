package com.example.leebo.recordsounddemo2;

import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private Button start_b;
    private Button stop_b;
    private Button delete_b;
    private Button play_b;
    private ListView myListView1;
    private TextView myText;
    private File myRecFile;
    private File myRecDir;
    private File myPlayFile;

    private ArrayList<String> recordFiles;
    private ArrayAdapter<String> adapter;

    private MediaRecorder mr;
    private MediaPlayer mp;
    //    private RecordClass rc=new RecordClass(MainActivity.this);
    private boolean sdCardExit;
    private boolean isStopRecord;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_b=(Button)findViewById(R.id.record_b);
        stop_b=(Button)findViewById(R.id.stop_b);
        delete_b=(Button)findViewById(R.id.delete_b);
        play_b=(Button)findViewById(R.id.play_b);
        myListView1=(ListView)findViewById(R.id.listview1);
        myText=(TextView)findViewById(R.id.text1);

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

        /*play*/
        play_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (myPlayFile!=null&&myPlayFile.exists()){
//                    openFile(myPlayFile);
//                }
                try{
                    mp=new MediaPlayer();

                    mp.setDataSource(myPlayFile.getAbsolutePath());
                    mp.prepare();
                    mp.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        /*delete*/
        delete_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myPlayFile!=null){
                    adapter.remove(myPlayFile.getName());
                    if (myPlayFile.exists())
                        myPlayFile.delete();
                    myText.setText("Delete successfully");
                }
            }
        });

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
    @Override
    protected void onStop(){
        if(mr!=null&&!isStopRecord){
            mr.stop();
            mr.release();
            mr=null;
        }
        super.onStop();
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

    /*开启播放录音文件的程序*/
    private void openFile(File f){
        Intent intent=new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        String type=getMIMEType(f);
        intent.setDataAndType(Uri.fromFile(f),type);
        startActivity(intent);
    }
    private String getMIMEType(File f){
        String end=f.getName().substring(f.getName().lastIndexOf(".")+1,f.getName().length()).toLowerCase();
        String type="";
        if(end.equals("mp3")||end.equals("aac")||end.equals("aac")||end.equals("amr")||end.equals("mpeg")||end.equals("mp4")){
            type = "audio";
        }else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||end.equals("jpeg")){
            type = "image";
        }else{
            type="*";
        }
        type+="*";
        return type;
    }
    public void start_record_b(View v){
        try {
            if (!sdCardExit){
                Toast.makeText(MainActivity.this,"There is no SD Card",Toast.LENGTH_SHORT).show();
                return;
            }
            myRecFile=File.createTempFile("Test",".amr",myRecDir);

            mr=new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);//从MIC获取声音
            mr.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mr.setOutputFile(myRecFile.getAbsolutePath());

            mr.prepare();
            mr.start();

            myText.setText("recording...");

            play_b.setEnabled(false);
            stop_b.setEnabled(true);
            delete_b.setEnabled(false);

            isStopRecord=false;
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void stop_record_b(View v){
        if (myRecFile!=null){
            mr.stop();

            /*将录音名给Adapter*/
            adapter.add(myRecFile.getName());

            mr.release();
            mr=null;
            //todo
//            addCapturedAudioToMediaStore();

            myText.setText("Stop:"+myRecFile.getName());
            stop_b.setEnabled(false);
            isStopRecord=true;
        }
    }




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
