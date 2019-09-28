package com.android.example.lintjat.Utils;

import com.android.tools.lint.detector.api.Project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;

public class FilleUtils {

    public Project project;

    public FilleUtils(Project project) {
        this.project = project;
    }

    public  void   intput(String key,String  value){
        File dir = project.getDir();
        File  detFile=new File(dir,"log.txt");
        BufferedWriter bufferedWriter=null;
        try {
            if (!detFile.exists()){
                detFile.createNewFile();
            }
            bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(detFile,true)));
            bufferedWriter.append(key+":");
            bufferedWriter.append(value);
            bufferedWriter.append("\r\n");
//            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void   intput(Map<String,String> map){
        Set<String> keySet=map.keySet();
        if (keySet==null||keySet.size()==0)
            return;
        BufferedWriter bufferedWriter=null;
        try {
            File dir = project.getDir();
            File  detFile=new File(dir,"log.txt");
            bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(detFile,true)));
            if (!detFile.exists()){
                detFile.createNewFile();
            }
            for (String key:keySet){
                String value=   map.get(key);
                bufferedWriter.write(key+":");
                bufferedWriter.write(value);
                bufferedWriter.write("\r\n");
            }
        }catch (Exception e){

        }finally {
            if (bufferedWriter!=null){
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
