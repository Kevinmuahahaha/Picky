package com.yybb.picky.ui.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;
import static com.yybb.picky.ui.utils.sharedStatus.writable;

public class fileChecker {
    public static void folderInit(){
        String folder_main = "Picky";
        folderPrefix = Environment.getExternalStorageDirectory() + "/" + folder_main;
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            boolean mkdir_stat = f.mkdirs();
            if (mkdir_stat) {
                msg.text("成功创建下载目录");
                msg.text("存储在 " + folderPrefix);
                writable = true;
            } else {
                msg.text("文件目录不可用");
                writable = false;
            }
        }
        else{ // Folder Already exists.
            if( f.canWrite() ){
                //msg.text("文件目录可用");
                writable = true;
            }
        }
    }
    public static boolean canWrite(){
        if( folderPrefix == null ){
            writable = false;
            return false;
        }
        else{
            if( new File(folderPrefix).canWrite() ){
                writable = true;
                return true;
            }
            else {
                writable = false;
                return false;
            }
        }
    }
    public static ArrayList<String> filesInFolder(String path_to_folder){
        if( folderPrefix == null ) folderInit();
        ArrayList<String> fileNames = null;
        File directory = new File(path_to_folder);
        for( File f : directory.listFiles() ){
            fileNames.add( f.getName() );
        }
        return fileNames;
    }
    public static ArrayList<String> imageNamesInFolder(String path_to_folder){
        if( folderPrefix == null ) folderInit();
        ArrayList<String> fileNames = new ArrayList<String>();
        File directory = new File(path_to_folder);
        for( File f : directory.listFiles() ){
            if( f.getName().endsWith(".jpg") || f.getName().endsWith(".png") ){
                fileNames.add( f.getName() );
            }
        }
        return fileNames;
    }
}