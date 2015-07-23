package com.example.leebo.recordsounddemo2;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lee on 2015/7/20.
 */
public class Scan {
    public ArrayList<FilesInf> filesName = new ArrayList<FilesInf>();

    public ArrayList<FilesInf> simpleScanning(File folder) {
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
                            tempF.filename=fileName+".amr";
                            tempF.filepath=filePath;
                            filesName.add(tempF);
                        }
                    }
                }
            }
        }
        return filesName;
    }
}
