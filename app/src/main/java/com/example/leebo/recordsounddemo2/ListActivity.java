package com.example.leebo.recordsounddemo2;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListActivity extends ActionBarActivity {
    private ListView lv;
    private MediaPlayer mp;
    private String reName=null;
    static public ArrayList<FilesInf> filesName = new ArrayList<FilesInf>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
//        getSupportActionBar().hide();



        lv = (ListView)findViewById(R.id.list_of_records);
        ArrayList<FilesInf> arr_files=(ArrayList<FilesInf>)getIntent().getSerializableExtra("key");
        final ArrayList<HashMap<String,String>> listItem = new ArrayList<HashMap<String,String>>();
        ArrayList<String> pathinf = new ArrayList<String>();

        for(FilesInf mfilesinf:arr_files){
            HashMap<String,String> map = new HashMap<String,String>();
            mp = new MediaPlayer();
            try {
                mp.setDataSource(mfilesinf.filepath);
            }catch (IOException e){
                e.printStackTrace();
            }
            int soundTime = mp.getDuration()/1000;
            String sTime = (soundTime/60+":"+soundTime%60);
            map.put("name", mfilesinf.filename);
            map.put("path", mfilesinf.filepath);
            map.put("time", sTime);

            listItem.add(map);
            pathinf.add(mfilesinf.filepath);
        }
        final SimpleAdapter simpleAdapter = new SimpleAdapter(this,listItem,R.layout.details_of_listinf,new String[]{"name","time"},new int[]{R.id.record_name,R.id.record_time});
        lv.setAdapter(simpleAdapter);

        /*play the record when click it*/
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                setTitle(listItem.get(position).get("path"));
                try {
                    mp = new MediaPlayer();
                    mp.setDataSource(listItem.get(position).get("path"));
                    mp.prepare();
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
//                getSupportActionBar().show();
                final EditText inputServer = new EditText(ListActivity.this);
                inputServer.setFocusable(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Rename").setView(inputServer).setNegativeButton(
                        "cancel", null);
                builder.setPositiveButton("save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reName = inputServer.getText().toString();
                                listItem.get(position).get("name").replace(listItem.get(position).get("name"), reName);
                                File tf = new File(listItem.get(position).get("path"));
                                tf.renameTo(new File(Environment.getExternalStorageDirectory() + "/myRecordDir/" + reName + ".amr"));
                                lv.setAdapter(simpleAdapter);
                            }
                        });
                builder.show();

                return false;
            }
        });
    }
    public static void simpleScanning(File folder) {
        //指定正则表达式
        Pattern mPattern = Pattern.compile("([^\\.]*)\\.([^\\.]*)");
        // 当前目录下的所有文件
        final String[] filenames = folder.list();
        // 当前目录的名称
        //final String folderName = folder.getName();
        // 当前目录的绝对路径
        //final String folderPath = folder.getAbsolutePath();
        if (filenames != null) {
            // 遍历当前目录下的所有文件
            for (String name : filenames) {
                File file = new File(folder, name);
                // 如果是文件夹则继续递归当前方法
                if (file.isDirectory()) {
                    simpleScanning(file);
                }
                // 如果是文件则对文件进行相关操作
                else {
                    Matcher matcher = mPattern.matcher(name);
                    if (matcher.matches()) {
                        // 文件名称
                        String fileName = matcher.group(1);
                        // 文件后缀
                        String fileExtension = matcher.group(2);
                        // 文件路径
                        String filePath = file.getAbsolutePath();

                        if (fileExtension.toLowerCase().equals("amr")) {
                            FilesInf tempF = new FilesInf();
                            tempF.filename=fileName;
                            tempF.filepath=filePath;
                            filesName.add(tempF);
                        }
                    }
                }
            }
        }
    }
//    /*readFile*/
//    private String readFile(String filename) {
//        String reads = "";
//        try {
//            FileInputStream fis = this.openFileInput(filename);
//            byte[] b = new byte[1024];
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            while (fis.read(b) != -1) {
//                baos.write(b, 0, b.length);
//            }
//            baos.close();
//            fis.close();
//            reads = baos.toString();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return reads;
//    }
//    /*saveFile*/
//    private void saveFile(String str, String filename) {
//        FileOutputStream fos;
//        try {
//            fos = this.openFileOutput(filename, this.MODE_PRIVATE);
//            fos.write(str.getBytes());
//            fos.flush();
//            fos.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.list_delete:
                return true;
            case R.id.list_rename:
                return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
