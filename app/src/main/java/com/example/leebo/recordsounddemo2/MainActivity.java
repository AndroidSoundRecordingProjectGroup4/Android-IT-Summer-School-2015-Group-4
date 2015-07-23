package com.example.leebo.recordsounddemo2;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;


import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.LogRecord;


public class MainActivity extends ActionBarActivity {

    private ImageButton cancel_b;
    private ImageButton start_img_b;
    private TextView myText;
    private ImageView left_p;
    private Chronometer timer_text;
    private File myRecFile;
    private File myRecDir;


    private MediaPlayer mp;
    private ProgressBar progress_b;
    private RecordClass rc=new RecordClass(MainActivity.this);
    private boolean sdCardExit;
    private boolean isRecording = false;
    private boolean addtime=true;
    public ArrayList<FilesInf> filesInfList = new ArrayList<FilesInf>();

    public static final int SNAP_VELOCITY = 200;
    private int screenWidth;
    private int leftEdge;
    private int rightEdge = 0;
    private int menuPadding = 80;
    private int progressBarStatus = 0;
    private String fileName = null;
    private View content;
    private View menu;
    private int time_limit = 0;
    private LinearLayout.LayoutParams menuParams;
    private float xDown;
    private float xMove;
    private int x = 0;
    private Handler handler;
    private Handler progressBarbHandler = new Handler();
    private float xUp;
    private boolean isMenuVisible;
    private VelocityTracker mVelocityTracker;
    private String[] time_select = new String[]{"Add time for the record","Do not add time" };

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private ProgressDialog mPlusClient;
//    private ConnectionResult



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        clearNotification();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==0){
                    if (isRecording==true)
                        stop_record_b();
                    else ;
                }
                super.handleMessage(msg);
            }
        };
    }
    @Override
    protected void onStop() {
        showNotification();
        super.onStop();
    }

    @Override
    protected void onStart() {
        clearNotification();
        super.onStart();
    }

    /*init*/
    public void init(){
        cancel_b=(ImageButton)findViewById(R.id.cancel_b);
        start_img_b=(ImageButton)findViewById(R.id.start_img_b);
        cancel_b.setOnTouchListener(TouchDark);
        start_img_b.setOnTouchListener(TouchDark);
        myText=(TextView)findViewById(R.id.tips);
        left_p = (ImageView)findViewById(R.id.slid_left);
        timer_text=(Chronometer)findViewById(R.id.timer_text);
        timer_text.setFormat("%s");
        cancel_b.setEnabled(false);
        progress_b=(ProgressBar)findViewById(R.id.progress_b);

        sdCardExit= Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String tpath = Environment.getExternalStorageDirectory()+"/myRecordDir";
        if(sdCardExit) {
            myRecDir = new File(tpath);
            if (!myRecDir.exists()){
                myRecDir.mkdirs();
            }
        }
    }

    /*start record*/
    public void start_record_b(View v){
        if (isRecording==false) {
            x=1;
            try {
                timer_text.setBase(SystemClock.elapsedRealtime());
//                timer_text.stop();
                timer_text.start();//start to timer
                if (time_limit>0)
                    time_Limit_record(time_limit);//call a time limit
                start_img_b.setBackgroundResource(R.drawable.recording_b);
                if (!sdCardExit) {
                    Toast.makeText(MainActivity.this, "There is no SD Card", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar c = Calendar.getInstance();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());//get time
                String str = formatter.format(curDate);
//            Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
                String tempP = null;
                if (addtime == true) {
                    if (fileName != null)
                        tempP = myRecDir + "/" + fileName + "-" + str + ".amr";
                    else
                        tempP = myRecDir + "/Record" + str + ".amr";
                    myRecFile = new File(tempP);
                    myRecFile.createNewFile();
                } else {
                    if (fileName != null)
                        myRecFile = File.createTempFile(fileName + "-", ".amr", myRecDir);
                    else
                        myRecFile = File.createTempFile("Record-", ".amr", myRecDir);
                }
                rc.start_record(myRecFile);

                cancel_b.setEnabled(true);
                isRecording=true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            stop_record_b();
        }
    }
    /*stop and save the record*/
    public void stop_record_b(){
        start_img_b.setBackgroundResource(R.drawable.start_b_img);
        if (myRecFile!=null){
            rc.stop_record();//use the method of RecordClass

            myText.setText("Stop:" + myRecFile.getName());//show the name
            isRecording=false;
            cancel_b.setEnabled(false);
            timer_text.stop();
        }
    }
    /*cancel to save the record*/
    public void cancel_record_b(View v){
        rc.cancel_record();
        isRecording = false;
        start_img_b.setBackgroundResource(R.drawable.start_b_img);
        myText.setText("cancel recording");
        start_img_b.setEnabled(true);
        cancel_b.setEnabled(false);
        x=0;
        progress_b.setVisibility(View.INVISIBLE);

//                        Toast.makeText(context, data.get(position).filepath+"", Toast.LENGTH_LONG).show();
        if (myRecFile.exists())
            myRecFile.delete();
        timer_text.setBase(SystemClock.elapsedRealtime());
        timer_text.stop();
    }

    /*set name*/
    public void left_set_name(View v){
        inputNameDialog();
    }
    private void inputNameDialog() {

        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Record Name").setView(inputServer).setNegativeButton(
                "cancel", null);
        builder.setPositiveButton("save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = inputServer.getText().toString();
                        if(fileName.length()<4){
                            Toast.makeText(MainActivity.this,"Please input more than 3 words",Toast.LENGTH_SHORT).show();
                            fileName = "Record";
                        }
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
//    /*set save location*/
//    public void set_save_location(View v){
//
//    }
    /*set limited time*/
    public void left_set_limited_time(View v){
        inputTimeDialog();
    }
    private void inputTimeDialog() {

        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Time Limit").setView(inputServer).setNegativeButton(
                "cancel", null);
        builder.setPositiveButton("save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        time_limit = Integer.valueOf(inputServer.getText().toString());
                    }
                });
        builder.show();
    }
    /*cancel time limit*/
    public void left_cancel_limited_time(View v){
        time_limit = 0;
        x=0;
        progress_b.setVisibility(View.INVISIBLE);
    }

    //fonction time limit
    public void time_Limit_record(final int timeLimit) {
        progress_b.setVisibility(View.VISIBLE);
        progress_b.setMax(timeLimit);
        progress_b.setProgress(0);
        progressBarStatus = 0;
        new Thread(new Runnable() {
            public void run() {
                while (progressBarStatus <= timeLimit) {
                    if(x==0){break;}
                    progressBarStatus++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBarbHandler.post(new Runnable() {
                        public void run() {
                            progress_b.setProgress(progressBarStatus);
                        }
                    });
                }
                Message msg = new Message();
                msg.what = 0;
                MainActivity.this.handler.sendMessage(msg);

            }
        }).start();

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
    /*change the color of ImageButton when touch it*/
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
        // 将menu的宽度设置为屏幕宽度减去menuPadding
        menuParams.width = screenWidth - menuPadding;
        // 左边缘的值赋值为menu宽度的负数
        leftEdge = -menuParams.width;
        // menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
        menuParams.leftMargin = leftEdge;
        // 将content的宽度设置为屏幕宽度
        content.getLayoutParams().width = screenWidth;
    }
    /*Active Left Page */
    public boolean onTouch(View v,MotionEvent event){
        createVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时，记录按下时的横坐标
                xDown = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整menu的leftMargin值，从而显示和隐藏menu
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
                // 手指抬起时，进行判断当前手势的意图，从而决定是滚动到menu界面，还是滚动到content界面
                xUp = event.getRawX();
                if (wantToShowMenu()) {
                    if (shouldScrollToMenu()) {
                        scrollToMenu();
                        left_p.setVisibility(View.GONE);
                    } else {
                        scrollToContent();
                        left_p.setVisibility(View.VISIBLE);
                    }
                } else if (wantToShowContent()) {
                    if (shouldScrollToContent()) {
                        scrollToContent();
                        left_p.setVisibility(View.VISIBLE);
                    } else {
                        scrollToMenu();
                        left_p.setVisibility(View.GONE);
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
    /*Left Page*/
    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int leftMargin = menuParams.leftMargin;
            // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
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
                // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
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
    /*notification*/
    private void showNotification() {
        NotificationManager barmanager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notice = new Notification(android.R.drawable.ic_media_play,"Record is running...",System.currentTimeMillis());
        notice.icon = R.drawable.play_n;
        notice.flags= Notification.FLAG_INSISTENT;
        Intent appIntent = new Intent(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setComponent(new ComponentName(this.getPackageName(), this.getPackageName() + "." + this.getLocalClassName()));
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//启动
        PendingIntent contentIntent =PendingIntent.getActivity(this, 0,appIntent,0);
        notice.setLatestEventInfo(this,"Record","running", contentIntent);
        barmanager.notify(0,notice);

    }


    private void clearNotification()
    {
        // 启动后删除之前我们定义的通知
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
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
//                getRecordFiles();
                Intent intent = new Intent();
//                intent.putExtra("key",filesInfList);
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
