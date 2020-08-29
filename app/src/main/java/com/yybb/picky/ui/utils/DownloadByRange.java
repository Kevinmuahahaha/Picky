package com.yybb.picky.ui.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadByRange  implements Runnable{

    private static final String TAG = "DownloadByRange";
    private String downloadString;
    private byte[] sharedStorageArray;
    private int rangeStart;
    private int rangeEnds;
    public int mRescueSplinterCount = 4;
    public int threadCount = -1;
    private int currentFileLength = -1;
    private int retry_count = 0;
    private boolean is_recursive = false;
    private ArrayList<Thread> currentGroup = new ArrayList<Thread>(); // keeps track of other threads
    public void assign(String target_link, byte[] storage_array, int range_starts, int range_ends){
        // String range ---- "0-24"
        //  or
        // 25-49
        // urlConnection.setRequestProperty("Range", "bytes=0-24");
        downloadString = target_link;
        sharedStorageArray = storage_array;
        rangeStart = range_starts;
        rangeEnds = range_ends;
    }

    public void setCurrentGroup(ArrayList<Thread> threads){
        currentGroup = threads;
    }

    private int startDownload(int setTimeout){
        if(!is_recursive)
            Log.wtf(TAG, "run: Running Task from " + rangeStart + "  to  " + rangeEnds);
        else{
            Log.wtf(TAG,"run: Rescue thread started. #Thread: " + threadCount);
            Log.wtf(TAG,"#Thread: " + threadCount + ", from " + rangeStart + "  to  " + rangeEnds);
        }

        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            URL url = new URL(downloadString);
            connection = (HttpURLConnection) url.openConnection();
            String constructed_byte_range = "bytes=" + rangeStart + "-" + rangeEnds;
            connection.setRequestProperty("Range", constructed_byte_range );
            connection.setReadTimeout(setTimeout);
            //connection.setReadTimeout(10000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //message.add("Response not HTTP_OK");
            }
        }
        catch (SocketTimeoutException ste){
            Log.wtf(TAG,"Thread: " + threadCount + ", timed out.");
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        int tmp_length = connection.getContentLength();
        if(tmp_length > 0) currentFileLength = tmp_length;

        int total=rangeStart;
        int absolute_total_total = 0;
        //message.add("Filelength in thread: " + currentFileLength);
        try {
            input = connection.getInputStream();
            int count = 0;
            byte data[] = new byte[1024];
            long last_check_time;
            //Time now = new Time();
            //now.setToNow();
            //last_check_time = System.currentTimeMillis();
            while ((count = input.read(data)) != -1) {
                int data_read_index = 0;
                for(int index=total;index<total+count;index++){
                    sharedStorageArray[index] = data[data_read_index++];
                }
                absolute_total_total += count;
                total = total+count;
                /* Segment A */
            }
        }
        catch (SocketTimeoutException te){
            Log.wtf(TAG,"Thread: " + threadCount + ", timed out.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Clean up -----------------------------------
        try {
            if(input != null)  input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();
        Log.wtf(TAG, "Thread: " + threadCount + ", Connection Ends");
        return absolute_total_total;
    }


    private boolean startRescueThreads(){
        int current_download_length = rangeEnds+1;
        int increment = (current_download_length / mRescueSplinterCount) - 1;
        if( increment <= 0) increment = 1;
        int tmp_start = rangeStart;
        int tmp_end = tmp_start + increment;
        if(tmp_end > rangeEnds) tmp_end = rangeEnds;
        ArrayList<Thread> thread_download_ranger = new ArrayList<Thread>();
        int thread_count = 1; // thread counting from 1
        while ( tmp_start <= current_download_length - 1 ){
            DownloadByRange download_ranger = new DownloadByRange();
            download_ranger.assign(downloadString,sharedStorageArray,tmp_start,tmp_end);
            download_ranger.is_recursive = true;
            download_ranger.threadCount = 1000 + thread_count ++ ;
            Thread tmp_add = new Thread(download_ranger);
            thread_download_ranger.add(tmp_add);
            tmp_add.start();
            tmp_start = tmp_end + 1;
            tmp_end = tmp_start + increment;
            if(tmp_end > rangeEnds) tmp_end = rangeEnds;
        }
        for(Thread item : thread_download_ranger) {
            try {
                item.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        int result = -1;
        int default_timeout = 10000;
        do{
           result = startDownload(default_timeout);
           Log.wtf(TAG,"(re)starting thread " + threadCount + ", tries left till die: " + (++retry_count) + ", Download Range: " + rangeStart + ". " + rangeEnds);
           default_timeout += 900;

           if(default_timeout >= 27000){
               if( !is_recursive ){
                   // too slow, start rescue threads
                   Log.wtf(TAG,"Rectracted Bytes: " + result);
                   startRescueThreads();
                   break;
               }
               else{
                   // set is_success to false
                   // break;
               }
           }
        }while(result != currentFileLength);


        Handler uiHandler = new Handler(Looper.getMainLooper());
        final int post_result = result;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                DownloadUI.updateDownloadedBytes(post_result);
            }
        });

    }
}
