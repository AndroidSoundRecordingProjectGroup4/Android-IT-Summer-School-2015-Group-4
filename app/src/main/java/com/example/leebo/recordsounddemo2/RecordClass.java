package com.example.leebo.recordsounddemo2;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by LeeBo on 2015/7/16.
 */
public class RecordClass {
    private Context mContext;
    public RecordClass(Context context){
        mContext = context;
    }
    private MediaRecorder mr;

    public void start_record(File f) {
        try {
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);//¥”MICªÒ»°…˘“Ù
            mr.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mr.setOutputFile(f.getAbsolutePath());

            mr.prepare();
            mr.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop_record(){
            mr.stop();
            mr.release();
            mr=null;
    }
    public void pause_record(){
    }

}

