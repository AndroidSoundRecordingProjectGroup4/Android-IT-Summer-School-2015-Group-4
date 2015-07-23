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
        //ָ��������ʽ
        Pattern mPattern = Pattern.compile("([^\\.]*)\\.([^\\.]*)");
        // ��ǰĿ¼�µ������ļ�
        final String[] filenames = folder.list();
        // ��ǰĿ¼������
        //final String folderName = folder.getName();
        // ��ǰĿ¼�ľ���·��
        //final String folderPath = folder.getAbsolutePath();
        if (filenames != null) {
            // ������ǰĿ¼�µ������ļ�
            for (String name : filenames) {
                File file = new File(folder, name);
                // ������ļ���������ݹ鵱ǰ����
                if (file.isDirectory()) {
                    simpleScanning(file);
                }
                // ������ļ�����ļ�������ز���
                else {
                    Matcher matcher = mPattern.matcher(name);
                    if (matcher.matches()) {
                        // �ļ�����
                        String fileName = matcher.group(1);
                        // �ļ���׺
                        String fileExtension = matcher.group(2);
                        // �ļ�·��
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
