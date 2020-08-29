package com.yybb.picky.configs;

import android.app.Activity;

import com.yybb.picky.ui.utils.fileChecker;
import com.yybb.picky.ui.utils.permissionChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class configManager {
    // TAG should be string
    //  which in the file would look like [TAG]
    //  val:    (TAG="background color", Value="orange")
    //  conf:   [background color]=orange

    // call .init() before using this class
    // return false/null when read/write is disabled
    private static String mConfigFileName = ".global.config";
    private static String mPathToConfig = null;
    private static Activity mActivity = null;
    private static  boolean init_ok(){
        if(mActivity == null || mPathToConfig == null)return  false;
        return permissionChecker.canRead(mActivity) && permissionChecker.canWrite(mActivity);
    }

    public static boolean init(Activity activity){
        fileChecker.folderInit();
        mPathToConfig = folderPrefix + "/" + mConfigFileName;
        mActivity = activity;
        return init_ok();
    }

    public static  boolean writeConfig(String TAG, String content){
        if(!init_ok()) return false;
        ArrayList<String> list_of_configs = new ArrayList<String>();
        boolean need_to_append = true;
        String match_tag = "[" + TAG + "]";
        // ^^^^^^^^^^^^^^^^^^^^^^ record and rewrite
        // if there's an existing config
        fileChecker.folderInit();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(mPathToConfig));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,"utf-8"));
            String current_line = null;
            while( (current_line = br.readLine()) != null ){
                if(current_line.startsWith( match_tag )){
                    list_of_configs.add(match_tag+"="+content);
                    need_to_append = false;
                    // rewrite that line
                    // otherwise append a new line
                }
                else{
                    list_of_configs.add(current_line);
                }
            }
            fis.close();
        } catch (Exception e) {
            //e.printStackTrace();
            //return false;
            //file not exist, create new file in codes below
        }

        try { // re-write config file
            FileOutputStream fos = new FileOutputStream(new File(mPathToConfig));
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            if(need_to_append){
                osw.write(match_tag+"="+content+"\n");
            }
            for( String item : list_of_configs ){
                osw.write(item+"\n");
            }

            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static  String readConfig(String TAG){
        if(!init_ok()) return null;
        fileChecker.folderInit();
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(new File(mPathToConfig));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,"utf-8"));
            String current_line = null;
            //StringBuilder sb = new StringBuilder();

            while( (current_line = br.readLine()) != null ){
                if(current_line.startsWith('['+TAG+']')){
                    return current_line.substring(current_line.indexOf('=')+1,current_line.length());
                }
            }
            //sb.append(br.readLine());
            //mUrlPrefix = sb.toString();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null; // if no such config
    }

}
