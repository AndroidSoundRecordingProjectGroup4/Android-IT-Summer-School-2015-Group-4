package com.example.leebo.recordsounddemo2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MainActivity extends ActionBarActivity {

    private ImageButton stop_b;
    private ImageButton cancel_b;
    private ImageButton start_img_b;
    private Button delete_b;
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
    private boolean addtime=true;
    public int recLen=0;
    public ArrayList<FilesInf> filesInfList = new ArrayList<FilesInf>();

    public static final int SNAP_VELOCITY = 200;
    private int screenWidth;
    private int leftEdge;
    private int rightEdge = 0;
    private int menuPadding = 80;
    private String fileName = null;
    private View content;
    private View menu;
    private LinearLayout.LayoutParams menuParams;
    private float xDown;
    private float xMove;
    private float xUp;
    private boolean isMenuVisible;
    private VelocityTracker mVelocityTracker;
    private String[] time_select = new String[]{"Add time for the record","Do not add time" };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    /*get files*/
    private void getRecordFiles(){
        recordFiles=new ArrayList<String>();
        if (sdCardExit){
            File files[]=myRecDir.listFiles();
            if (files!=null){
                for (int i=0;i<files.length;i++){
                    if(files[i].getName().indexOf(".")>=0){
                        /*��ȡ.arm�ļ�*/
                        String fileS = files[i].getName().substring(files[i].getName().indexOf("."));
                        if(fileS.toLowerCase().equals(".amr")) {
                            recordFiles.add(files[i].getName());

                            /*to transfer data to ListActivity*/
                            FilesInf tempF = new FilesInf();
                            tempF.filename=files[i].getName();
                            tempF.filepath=files[i].getAbsolutePath();
                            filesInfList.add(tempF);
                        }

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
        myText=(TextView)findViewById(R.id.tips);
        timer_text=(Chronometer)findViewById(R.id.timer_text);
        timer_text.setFormat("%s");
        stop_b.setEnabled(false);
        delete_b.setEnabled(false);
        cancel_b.setEnabled(false);

        sdCardExit= Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String tpath = Environment.getExternalStorageDirectory()+"/myRecordDir";
        if(sdCardExit) {
            myRecDir = new File(tpath);
            if (!myRecDir.exists()){
                myRecDir.mkdirs();
            }
        }
        getRecordFiles();

        adapter=new ArrayAdapter<String>(this,R.layout.my_listview,recordFiles);
        /*��ArrayAdapter����ListView������*/
//        myListView1.setAdapter(adapter);
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
            Calendar c = Calendar.getInstance();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());//get time
            String str = formatter.format(curDate);
//            Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
            String tempP = null;
            if (addtime==true) {
                if (fileName != null)
                    tempP = myRecDir + "/" + fileName + "-" + str + ".amr";
                else
                    tempP = myRecDir + "/Record" + str + ".amr";
                myRecFile = new File(tempP);
                myRecFile.createNewFile();
            }else{
                if (fileName != null)
                    myRecFile=File.createTempFile(fileName+"-",".amr",myRecDir);
                else
                    myRecFile=File.createTempFile("Record-",".amr",myRecDir);
            }
//                myRecFile=File.createTempFile(fileName+"-"+str,".amr",myRecDir);
//            else
//                myRecFile=File.createTempFile("Record-"+str,".amr",myRecDir);

            rc.start_record(myRecFile);

            myText.setText("recording...");

            start_img_b.setEnabled(false);
//            play_b.setEnabled(false);
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
            /*��¼������Adapter*/
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
    /*set name*/
    public void left_set_name(View v){
        inputTitleDialog();
    }
    private void inputTitleDialog() {

        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Record Name").setView(inputServer).setNegativeButton(
                "cancel", null);
        builder.setPositiveButton("save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = inputServer.getText().toString();
                    }
                });
        builder.show();
    }
    /*add time or not*/
    public void left_add_time(View v){//time_select
        new AlertDialog.Builder(MainActivity.this).setIcon(R.drawable.time).setTitle("Add Time").setItems(time_select, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,which+"",Toast.LENGTH_SHORT).show();
                if (which == 0)
                    addtime=true;
                else
                    addtime=false;
                dialog.dismiss();
            }
        }).show();
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
    /*Left*/
    private void initValues(){
        WindowManager window = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        screenWidth = window.getDefaultDisplay().getWidth();
        menu = findViewById(R.id.main_left_drawer_layout);
        menuParams = (LinearLayout.LayoutParams) menu.getLayoutParams();
        // ��menu�Ŀ������Ϊ��Ļ��ȼ�ȥmenuPadding
        menuParams.width = screenWidth - menuPadding;
        // ���Ե��ֵ��ֵΪmenu��ȵĸ���
        leftEdge = -menuParams.width;
        // menu��leftMargin����Ϊ���Ե��ֵ��������ʼ��ʱmenu�ͱ�Ϊ���ɼ�
        menuParams.leftMargin = leftEdge;
        // ��content�Ŀ������Ϊ��Ļ���
        content.getLayoutParams().width = screenWidth;
    }

    public boolean onTouch(View v,MotionEvent event){
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // ��ָ����ʱ����¼����ʱ�ĺ�����
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                // ��ָ�ƶ�ʱ���ԱȰ���ʱ�ĺ����꣬������ƶ��ľ��룬������menu��leftMarginֵ���Ӷ���ʾ������menu
                xMove = event.getRawX();
                int distanceX = (int) (xMove - xDown);
                if (isMenuVisible) {
                    menuParams.leftMargin = distanceX;
                } else {
                    menuParams.leftMargin = leftEdge + distanceX;
                }
                if (menuParams.leftMargin < leftEdge) {
                    menuParams.leftMargin = leftEdge;
                } else if (menuParams.leftMargin > rightEdge) {
                    menuParams.leftMargin = rightEdge;
                }
                menu.setLayoutParams(menuParams);
                break;
            case MotionEvent.ACTION_UP:
                // ��ָ̧��ʱ�������жϵ�ǰ���Ƶ���ͼ���Ӷ������ǹ�����menu���棬���ǹ�����content����
                xUp = event.getRawX();
                if (wantToShowMenu()) {
                    if (shouldScrollToMenu()) {
                        scrollToMenu();
                    } else {
                        scrollToContent();
                    }
                } else if (wantToShowContent()) {
                    if (shouldScrollToContent()) {
                        scrollToContent();
                    } else {
                        scrollToMenu();
                    }
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }
    private boolean wantToShowContent() {
        return xUp - xDown < 0 && isMenuVisible;
    }
    private boolean wantToShowMenu() {
        return xUp - xDown > 0 && !isMenuVisible;
    }
    private boolean shouldScrollToMenu() {
        return xUp - xDown > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }
    private boolean shouldScrollToContent() {
        return xDown - xUp + menuPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
    }
    private void scrollToMenu() {
        new ScrollTask().execute(30);
    }
    private void scrollToContent() {
        new ScrollTask().execute(-30);
    }
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }
    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int leftMargin = menuParams.leftMargin;
            // ���ݴ�����ٶ����������棬������������߽���ұ߽�ʱ������ѭ����
            while (true) {
                leftMargin = leftMargin + speed[0];
                if (leftMargin > rightEdge) {
                    leftMargin = rightEdge;
                    break;
                }
                if (leftMargin < leftEdge) {
                    leftMargin = leftEdge;
                    break;
                }
                publishProgress(leftMargin);
                // Ϊ��Ҫ�й���Ч��������ÿ��ѭ��ʹ�߳�˯��20���룬�������۲��ܹ���������������
                sleep(20);
            }
            if (speed[0] > 0) {
                isMenuVisible = true;
            } else {
                isMenuVisible = false;
            }
            return leftMargin;
        }
        private void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... leftMargin) {
            menuParams.leftMargin = leftMargin[0];
            menu.setLayoutParams(menuParams);
        }

        @Override
        protected void onPostExecute(Integer leftMargin) {
            menuParams.leftMargin = leftMargin;
            menu.setLayoutParams(menuParams);
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
        switch (item.getItemId()){
            case R.id.goto_list:
                filesInfList.clear();
                getRecordFiles();
                Intent intent = new Intent();
                intent.putExtra("key",filesInfList);
                intent.setClass(MainActivity.this, ListActivity.class);
                startActivity(intent);
                return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
