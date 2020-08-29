package com.yybb.picky;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yybb.picky.ui.utils.DownloadByRange;
import com.yybb.picky.ui.utils.DownloadUI;
import com.yybb.picky.ui.utils.fileChecker;
import com.yybb.picky.ui.utils.msg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class kleinUndSchnell extends AsyncTask<String, Integer, String> {
    private static final String TAG = "kleinUndSchnell";
    private final int STATUS_PARAMETERS_OK = -1;
    private final int STATUS_INFORM_FILE_LENGTH = 2;
    private final String STATUS_PARAMETERS_ERROR = "param_error";
    private final String STATUS_DOWNLOAD_ERROR = "download_error";
    private final String STATUS_URL_ERROR = "url_error";
    private final String  STATUS_CONNECTION_ERROR = "connection_error";
    private final String STATUS_DOWNLOAD_DONE = "download_done";
    private final String STATUS_FILE_SAVE_ERROR = "file_save_error";
    private boolean downloadSuccess = false;
    private  String testLink = null;
    private  String testLocaltion =  null;
    private int mSplinterCount = 25;
    private int mRescueSplinterCount = 2;
    private int current_download_length = -1;
    private byte[] storage_in_ram;
    private ArrayList<Thread> DownloadThread_Array = new ArrayList<Thread>();

    public  boolean isSuccess(){
        return downloadSuccess;
    }
    public void setSplinterCount(int count){
        // separate file into how many pieces?
        mSplinterCount = count;
    }
    public void setRescueSplinterCount(int count){
        mRescueSplinterCount = count;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if( values[0] == STATUS_PARAMETERS_OK ){
          //  msg.text("Download Starts:\n" + testLink + "\n    to\n" + testLocaltion);
        }
        if( values [0] == STATUS_INFORM_FILE_LENGTH ){
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    DownloadUI.showProgressBar();
                    DownloadUI.updateTotalLength(current_download_length);
                    DownloadUI.setProgressBarValue(0);
                }
            });

        }
    }

    @Override
    protected String doInBackground(String... strings) {
        try{
            if(strings[0] != null && strings[1] != null){// parameters check
                publishProgress(1);
                testLink = strings[0];
                testLocaltion = strings[1];
            }
        }
        catch (IndexOutOfBoundsException e){
            //publishProgress(-1);
            return STATUS_PARAMETERS_ERROR;
        }

        HttpURLConnection connection = null; //url format check
        URL url = null;
        try {
            url = new URL(testLink);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_URL_ERROR;
        }

        try {   //connection check
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            current_download_length = connection.getContentLength();
            if(current_download_length == -1){
                return STATUS_CONNECTION_ERROR;
            }
            storage_in_ram = new byte[current_download_length];
            connection.disconnect();
            publishProgress(STATUS_INFORM_FILE_LENGTH);
        } catch (IOException e) {
            e.printStackTrace();
            return STATUS_CONNECTION_ERROR;
        }

        // init downloader
        int increment = (current_download_length / mSplinterCount) - 1;
        if( increment <= 0) increment = 1;
        int tmp_start = 0;
        int tmp_end = tmp_start + increment;
        if(tmp_end>(current_download_length-1)) tmp_end = current_download_length -1;

        int thread_count = 1;


        int total_content_length = connection.getContentLength();
        // launching
        while ( tmp_start <= current_download_length - 1 ){
            DownloadByRange download_ranger = new DownloadByRange();
            download_ranger.assign(testLink,storage_in_ram,tmp_start,tmp_end);
            download_ranger.threadCount = thread_count ++;
            download_ranger.mRescueSplinterCount = mRescueSplinterCount;
            download_ranger.setCurrentGroup(DownloadThread_Array);
            Thread tmp_add = new Thread(download_ranger);
            DownloadThread_Array.add(tmp_add);
            tmp_add.start();
            tmp_start = tmp_end + 1;
            tmp_end = tmp_start + increment;
            if(tmp_end>(current_download_length-1)) tmp_end = current_download_length -1;
        }
        for(Thread item : DownloadThread_Array){
            try {
                item.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.wtf(TAG,"Cannot join an DownloadByRange thread.");
            }
        }
        /*
        *   Description: ---------------vvvvvvvvvvv
        *   [ 5 threads ]
        *   [ total = file size count from 1 ]
        *   increment = total / 5 - 1
        *   start = 0
        *   end   = increment
        * ---------<<<>>>--------------
        *   start = last_end + 1
        *   end = start + increment
        * ---------<<<>>>--------------
        *   if end > total - 1
        *       end = total - 1
        *   if start > total - 1   <<< don't use else if
        *       break;
        *   ^^^^^^^^^^^^^^-------------------------
        * */

        fileChecker.folderInit(); // write to file --- task ends
        try {
            FileOutputStream fos = new FileOutputStream(testLocaltion);
            fos.write(storage_in_ram);
            fos.close();
            downloadSuccess = true;
            /*
            *   TODO:
            *    add error reports.
            *
            * */
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return STATUS_FILE_SAVE_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
            return STATUS_FILE_SAVE_ERROR;
        }
        if(isSuccess())
            return STATUS_DOWNLOAD_DONE;
        else{
            return STATUS_DOWNLOAD_ERROR;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                DownloadUI.cleanUp();
            }
        });

        switch ( s ){
            case STATUS_CONNECTION_ERROR:
                msg.shrt("Can't connect to Server.");
                break;
            case STATUS_DOWNLOAD_ERROR:
               // msg.shrt("Download Error Occurred.");
                break;
            case STATUS_PARAMETERS_ERROR:
              //  msg.popup("Please contact the author.\nTell him/her that KleinUndSchnell\nneeds (link, location)\nas parameters.");
                break;
            case STATUS_FILE_SAVE_ERROR:
                break;
            case STATUS_URL_ERROR:
               // msg.shrt("URL Error.");
                break;
            case STATUS_DOWNLOAD_DONE:
              //  msg.shrt("Download Success.");
        }
    }
}
