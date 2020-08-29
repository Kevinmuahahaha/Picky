package com.yybb.picky;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.yybb.picky.ui.utils.DownloadByRange;
import com.yybb.picky.ui.utils.fileChecker;
import com.yybb.picky.ui.utils.msg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.yybb.picky.ui.utils.repoManager.unzip;
import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class RepoSetupService extends IntentService {
    private static final String TAG = "RepoSetupService";
    private String downloadLink;
    private String downloadLocation;
    private String downloadRepoName;
    private static final String INFORM_DOWNLOAD_LINK = "inform_download_link";
   // private static final String INFORM_DOWNLOAD_LOCATION = "inform_download_location";
    private static final String INFORM_DOWNLOAD_REPO_NAME = "inform_download_repo_name";

    public RepoSetupService() {
        super("RepoSetupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        fileChecker.folderInit();
        downloadLink = intent.getStringExtra(INFORM_DOWNLOAD_LINK);
        //downloadLocation = intent.getStringExtra(INFORM_DOWNLOAD_LOCATION);
        // ^^^^^^ using default download location
        downloadRepoName = intent.getStringExtra(INFORM_DOWNLOAD_REPO_NAME);
        downloadLocation = folderPrefix + "/" + downloadRepoName + "_bundle.zip";
        if( downloadLocation == null ||
                downloadLink == null ||
                downloadRepoName == null
        ){
            Log.wtf(TAG,"Information insufficient for setup. Please read my doc.");
            return;
        }

        fileChecker.folderInit();
        String extract_repo_name = "repo_" + downloadRepoName;
        String path_extract_zip_directory = folderPrefix + "/" + extract_repo_name + "/.thumbs/";
        try {
            Log.wtf(TAG,"service started");
            serviceKleinUndSchnell skus = new serviceKleinUndSchnell();
            skus.setRescueSplinterCount(4);
            skus.setSplinterCount(80);
            skus.doDownload(downloadLink,downloadLocation);
            Log.wtf(TAG,"Download completed.");
            if(skus.isSuccess()){
                File zip_file = new File(downloadLocation);
                File extract_directory = new File(path_extract_zip_directory);
                try {
                    unzip(zip_file, extract_directory);
                    msg.notify(this,downloadRepoName + ": 准备完毕");
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.notify(this,"翻车...");
                }
            }
            else{
                msg.notify(this,"添加失败");
            }

        } catch (Exception e) {
            // Restore interrupt status.
            e.printStackTrace();
        }
        Log.wtf(TAG,"service ends");
        stopSelf();
    }

    private class serviceKleinUndSchnell{
        private static final String TAG = "serviceKleinUndSchnell";
        private final String STATUS_PARAMETERS_ERROR = "param_error";
        private final String STATUS_DOWNLOAD_ERROR = "download_error";
        private final String STATUS_URL_ERROR = "url_error";
        private final String  STATUS_CONNECTION_ERROR = "connection_error";
        private final String STATUS_DOWNLOAD_DONE = "download_done";
        private final String STATUS_FILE_SAVE_ERROR = "file_save_error";
        private boolean downloadSuccess = false;
        private  String testLink = null;
        private  String testLocaltion =  null;
        private int mSplinterCount = 10;
        private int mRescueSplinterCount = 4;
        private int current_download_length = -1;
        private byte[] storage_in_ram;
        private ArrayList<Thread> DownloadThread_Array = new ArrayList<Thread>();

        public void setSplinterCount(int count){
            // separate file into how many pieces?
            mSplinterCount = count;
        }
        public void setRescueSplinterCount(int count){
            mRescueSplinterCount = count;
        }
        public  boolean isSuccess(){
            return downloadSuccess;
        }

        public String doDownload(String downloadLink, String downloadLocation){
            try{
                if(downloadLink != null && downloadLocation != null){// parameters check
                    testLink = downloadLink;
                    testLocaltion = downloadLocation;
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
    }
}

