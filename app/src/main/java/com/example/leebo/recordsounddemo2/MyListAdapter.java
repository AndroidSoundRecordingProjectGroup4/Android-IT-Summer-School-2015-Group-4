package com.example.leebo.recordsounddemo2;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.logging.LogRecord;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by Lee on 2015/7/20.
 */

public class MyListAdapter extends BaseAdapter{
    private Context context ;
    private ArrayList<FilesInf> data=new ArrayList<FilesInf>() ;
    private MediaPlayer mp = new MediaPlayer();
    private SeekBar seekBar;
    private File deleteFile;
    private boolean isPlaying=false;
    private boolean isPause = false;
    private String reName;
    private Scan sc;
    public ArrayList<FilesInf> filesName = new ArrayList<FilesInf>();


    public MyListAdapter(Context cxt,SeekBar seekBar) {
        this.context = cxt ;
        this.seekBar = seekBar;
        sc = new Scan();
        data.clear();
        this.data = sc.simpleScanning(new File(Environment.getExternalStorageDirectory() + "/myRecordDir"));

    }

    @Override
    public int getCount() {
        return data.size() ;
    }

    @Override
    public String getItem(int position) {
        return data.get(position).filename ;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }
    private int positonX = -1 ;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.details_of_listinf, null);
            holder = new ViewHolder();
            holder.layout = (RelativeLayout) convertView
                    .findViewById(R.id.details_r_layout);
            holder.myTextView = (TextView) convertView
                    .findViewById(R.id.record_name);
            holder.button_delete = (ImageButton) convertView.findViewById(R.id.delete_item_button) ;
//            holder.play_list = (ImageButton) convertView.findViewById(R.id.play_list_b);
//            holder.pause_list = (ImageButton)convertView.findViewById(R.id.pause_list_b);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.myTextView.setText(data.get(position).filename);
//        holder.myTextView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
////                    holder.play_list.setVisibility(View.GONE);
////                    holder.pause_list.setVisibility(View.VISIBLE);
//                    holder.pause_list.setBackgroundResource(R.drawable.pause_b);
//                    isPlaying = false;
//                    mp.reset();
//                    mp.setDataSource(data.get(position).filepath);
//                    mp.prepare();
//                    mp.start();
//                    startProgressUpdate();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

//        holder.pause_list.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isPlaying) {
//                    holder.pause_list.setBackgroundResource(R.drawable.play_b);
//                    mp.pause();
//                    isPlaying = true;
//                }else{
//                    mp.start();
//                    holder.pause_list.setBackgroundResource(R.drawable.pause_b);
//                    isPlaying = false;
//                }
//            }
//        });
        holder.button_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Animation animation = (Animation) AnimationUtils.loadAnimation(
                        context, R.anim.delete_to_left);// 左移动时播放的动画
                animation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    /**
                     * 动画播放完后做删除操作
                     */
                    public void onAnimationEnd(Animation animation) {

                        deleteFile = new File(data.get(position).filepath);
//                        Toast.makeText(context, data.get(position).filepath+"", Toast.LENGTH_LONG).show();
                        if (deleteFile.exists())
                            deleteFile.delete();
                        data.remove(position);
//                        Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();
                        MyListAdapter.this.notifyDataSetChanged();
                    }
                });
                holder.layout.startAnimation(animation);// 开始播放动画
                holder.button_delete.setVisibility(View.GONE);
            }
        }) ;
//        convertView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        /*rename*/
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final EditText inputServer = new EditText(context);
                inputServer.setFocusable(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Rename").setView(inputServer).setNegativeButton(
                        "cancel", null);
                builder.setPositiveButton("save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reName = inputServer.getText().toString();
                                data.get(position).filename.replace(data.get(position).filename, reName);
                                File tf = new File(data.get(position).filepath);
                                tf.renameTo(new File(Environment.getExternalStorageDirectory() + "/myRecordDir/" + reName + ".amr"));
                                data.clear();
                                data = sc.simpleScanning(new File(Environment.getExternalStorageDirectory() + "/myRecordDir"));
                                MyListAdapter.this.notifyDataSetChanged();
                            }
                        });
                builder.show();

                return true;
            }
        });
        convertView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        positonX =(int) event.getX();//开始记录  按下的位置,用于与后面的事件发生位置对比
                        holder.button_delete.setVisibility(View.GONE) ;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(positonX-event.getX()>80){//左移事件
                            holder.button_delete.setVisibility(View.VISIBLE) ;
                        }
                        break;
                }
                return true;
            }
        });
        /*SeekBar*/
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int dest = seekBar.getProgress();
                int mMax = mp.getDuration();//get the time
                int sMax = seekBar.getMax();
                mp.seekTo(mMax*dest/sMax);
            }
        });
        return convertView;
    }
    public void startProgressUpdate(){
        DelayThread dThread = new DelayThread(100);
        dThread.start();
    }
    private Handler mHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            int position = mp.getCurrentPosition();

            int mMax = mp.getDuration();
            int sMax = seekBar.getMax();

            seekBar.setProgress(position*sMax/mMax);
        }
    };

    public class DelayThread extends Thread{
        int milliseconds;
        public DelayThread(int i){
            milliseconds = i;
        }
        public void run(){
            while (true){
                try {
                    sleep(milliseconds);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                mHandle.sendEmptyMessage(0);
            }
        }
    }
    public class ViewHolder {
        public RelativeLayout layout;
        public TextView myTextView;
        private ImageButton button_delete ;
//        private ImageButton play_list;
//        private ImageButton pause_list;
    }
}
