package com.example.leebo.recordsounddemo2;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.plus.PlusShare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListActivity extends ActionBarActivity {
    private ListView lv;//files list
    private int positonX = -1 ;
    private int fposition = 0;
    private boolean longclick = false;
    private File deleteFile;
//    private File control_file;
    private FilesInf control_file;
    private String control_path = null;
    private SeekBar seekBar;//progress bar
    private MediaPlayer mp;
    private AudioManager audioManager; //sound
    private ArrayList<FilesInf> data=new ArrayList<FilesInf>() ;
    private String reName=null;// new name
    private String share_name=null;
    private TextView playtime_t;//current time
    private TextView totaltime_t;//total time
    private int maxPro;// 进度条显示最大值
    private int currentPro = 0;// 进度条当前值
    private boolean stopThread = false;// 是否停止进度条更新
    private TextView musicDesTextView;// 歌曲名称
    private ImageView imageView;//歌曲专辑封面
    private ToggleButton tbMute;//静音/正常
    private ImageButton preButton;// 上一首
    private ImageButton nextButton;// 下一首
    private ImageButton playButton;// 播放
    private ImageButton pauseButton;// 暂停
    private SimpleAdapter simpleAdapter;
    private Scan sc;
    private Handler handler;

//    private MyListAdapter adapter;
//    private ImageButton play_pause;
//    private ImageButton play_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                currentPro = msg.what;
                if(currentPro<maxPro){
                    seekBar.setProgress(currentPro);
                    playtime_t.setText(formatDuring(currentPro));
                }
                super.handleMessage(msg);
            }
        };

        initView();
    }

    /*initialize*/
    private void initView() {
//        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE) ;
        mp = new MediaPlayer();
        sc = new Scan();
        lv = (ListView) findViewById(R.id.list_of_records);
        audioManager=(AudioManager)getSystemService(Service.AUDIO_SERVICE);
        seekBar = (SeekBar)findViewById(R.id.play_seekbar);
        musicDesTextView = (TextView) findViewById(R.id.musicdes);
        playtime_t = (TextView)findViewById(R.id.playtime);
        totaltime_t = (TextView)findViewById(R.id.alltime);
        tbMute=(ToggleButton)findViewById(R.id.tbMute);
//        preButton = (ImageButton) findViewById(R.id.preButton);
//        nextButton = (ImageButton) findViewById(R.id.nextButton);
        playButton = (ImageButton) findViewById(R.id.playButton);
        pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        data.clear();
        data = sc.simpleScanning(new File(Environment.getExternalStorageDirectory() + "/myRecordDir"));

        setListView();
        //move the seekbar
        seekBar.setSelected(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentPro = seekBar.getProgress();
                mp.seekTo(currentPro);
                playtime_t.setText(formatDuring(currentPro));
                mp.start();
            }
        });
        //play
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                mp.start();
            }
        });
        //pause
        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                mp.pause();
            }
        });
        //Mute
        tbMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, !isChecked); //设置是否静音
            }
        });
//        adapter = new MyListAdapter(this,seekBar);
//        lv.setAdapter(adapter);
    }

    private void setListView() {
        // TODO Auto-generated method stub
        // 获取音频文件数据
        ArrayList<HashMap<String,Object>> recordsInf = new ArrayList<HashMap<String,Object>>();
        int r_length = data.size();
        for(int i=0;i<r_length;i++){
            HashMap<String,Object> map = new HashMap<String,Object>();
            map.put("name",data.get(i).filename);
            map.put("delete",R.drawable.delete);
            recordsInf.add(map);
        }
        simpleAdapter = new SimpleAdapter(this,recordsInf,R.layout.details_of_listinf,new String[]{"name","delete"},new int[]{R.id.record_name,R.id.delete_item_button});
        lv.setAdapter(simpleAdapter);

        /*play when click item*/
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                playRecord(data.get(position).filepath);
                longclick=false;
                invalidateOptionsMenu();
//                notification.tickerText = "playing:";
            }
        });
        /*rename or delete when long click item*/
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                longclick=true;
                invalidateOptionsMenu();
                control_path = data.get(position).filepath;
                share_name = data.get(position).filename;
                control_file = data.get(position);
                fposition = position;
                return true;
            }
        });
//        for (int i = 0;i<lv.getChildCount();i++){
//            lv.getChildAt(1).setVisibility(View.VISIBLE);
//        lv.getFocusedChild().findViewById(R.id.delete_item_button).setVisibility(View.VISIBLE);
//        }
//        /*slide to left to delete*/
//        lv.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        positonX =(int) event.getX();//开始记录  按下的位置,用于与后面的事件发生位置对比
//                        holder.button_delete.setVisibility(View.GONE) ;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        if(positonX-event.getX()>80){//左移事件
//                            holder.button_delete.setVisibility(View.VISIBLE) ;
//                        }
//                        break;
//
//                    return false;
//            }
//        });

    }
    /*play*/
    public void playRecord(String f){
        try {
            mp.reset();
            mp.setDataSource(f);
            mp.prepare();
            totaltime_t.setText(formatDuring(mp.getDuration()));
            playtime_t.setText("0:0:0");
            mp.start();
            mp.seekTo(0);
            maxPro = (Integer) mp.getDuration();
            currentPro = 0;
            seekBar.setMax(maxPro);
            seekBar.setProgress(currentPro);
            //set seekbar
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (currentPro<maxPro){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!stopThread){
                            Message msg = new Message();
                            msg.what = mp.getCurrentPosition();
                            ListActivity.this.handler.sendMessage(msg);
                        }else
                            break;
                    }
                }
            }).start();
            //show the information of record
            musicDesTextView.setText(new File(f).getName());
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }catch (IOException e){
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
    /*rename*/
    public void renameRecord(final String path,final FilesInf f){
        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename").setView(inputServer).setNegativeButton(
                "cancel", null);
        builder.setPositiveButton("save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        File rn = new File(path);
                        reName = inputServer.getText().toString();
//                        f.filename.replace(f.filename, reName);

                        File tf = new File(path);
                        int r_length = data.size();
                        int i=0;
                        if (reName.equals(null))
                            Toast.makeText(ListActivity.this,"Please put something~",Toast.LENGTH_SHORT).show();
                        else {
                            for (i = 0; i < r_length; i++) {
                                if (data.get(i).filename.equals(reName)) {
                                    Toast.makeText(ListActivity.this, "The name has already exist,please change name", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            if (i == r_length) {
                                tf.renameTo(new File(Environment.getExternalStorageDirectory() + "/myRecordDir/" + reName + ".amr"));
                                data.clear();
                                data = sc.simpleScanning(new File(Environment.getExternalStorageDirectory() + "/myRecordDir"));
                                simpleAdapter.notifyDataSetChanged();
                                setListView();
                            }
                        }
                    }
                });
        builder.show();
        longclick=false;
        control_path = null;
        invalidateOptionsMenu();
    }
    /*delete*/
    public void deleteRecord(final String path,int p){
        deleteFile = new File(path);
        if (deleteFile.exists())
            deleteFile.delete();
        longclick=false;
        control_path = null;
        data.remove(p);
        invalidateOptionsMenu();
        setListView();
        simpleAdapter.notifyDataSetChanged();
    }
    public void shareRecord(){
        // Launch the Google+ share dialog with attribution to your app.
        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText("I am using Group 4's recording application for Android, it is really great! I have made " + data.size() + " recordings. I am listening to "+share_name )
                .setContentUrl(Uri.parse("https://github.com/AndroidSoundRecordingProjectGroup4/Android-IT-Summer-School-2015-Group-4")) //we will change this to be either a download to the recording or to an apk download
                .getIntent();
        startActivityForResult(shareIntent, 0);
    }
    /*change to time*/
    public static String formatDuring(long mss) {
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return hours + ":" + minutes + ":" + seconds;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        MenuItem list_delete_i = menu.findItem(R.id.list_delete);
        MenuItem list_rename_i = menu.findItem(R.id.list_rename);
//        MenuItem list_search_i = menu.findItem(R.id.list_search);
        MenuItem share_b = menu.findItem(R.id.share_button);
        if(longclick==true){
            list_delete_i.setVisible(true);
            list_rename_i.setVisible(true);
//            list_search_i.setVisible(false);
            share_b.setVisible(true);
        }else{
            list_delete_i.setVisible(false);
            list_rename_i.setVisible(false);
//            list_search_i.setVisible(true);
            share_b.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.list_back:
                finish();
                return true;
            case R.id.list_delete:
                deleteRecord(control_path,fposition);
                return true;
            case R.id.list_rename:
                renameRecord(control_path,control_file);
                return true;
            case R.id.share_button:
                shareRecord();
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
