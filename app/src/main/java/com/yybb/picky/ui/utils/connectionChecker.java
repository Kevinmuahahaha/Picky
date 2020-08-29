package com.yybb.picky.ui.utils;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.EditText;

import com.yybb.picky.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.yybb.picky.ui.utils.sharedStatus.test_result_can_download;
import static com.yybb.picky.ui.utils.sharedStatus.test_result_internet_connected;
import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class connectionChecker extends AsyncTask<String, Integer, String>  {
    WeakReference<Activity> testActivity;
    private static String testLink = "http://q34hxm3.xyz";
    private static String testDownloadLink = "http://q34hxm3.xyz/storage/RIPS/rips/reddit_sub_aww/etyuvy-I_did_a_great_job_at_the_vet_today_-gv3gsn6bj0d41.jpg";
    private String testingTask = "connect";
    public connectionChecker(Activity activity, String task){
        testActivity = new WeakReference<Activity>(activity);
        testingTask = task;
    }
    private static boolean singleHttpConnection(){
        HttpURLConnection connection = null;
        URL url = null;
        try {
            url = new URL(testLink);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        connection.disconnect();
        return true;
    }
    private synchronized boolean singleHttpDownload(){
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(testDownloadLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            // if cannot make single http connection
            if ( !singleHttpConnection() ) {
                return false;
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(folderPrefix + "/test.jpg");
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    test_result_can_download = false;
                    return false;
                }
                total += count;
                // publishing the progress....
                int publish_counter = 0;
                if (fileLength > 0){
                    if( publish_counter % 20 == 0 ){
                        publishProgress((int) (total * 100 / fileLength));
                        publish_counter = 0;
                    }
                    publish_counter ++;
                    // only if total length is known
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            test_result_can_download = false;
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        test_result_can_download = true;
        return true;
    }
    protected String doInBackground(String... strParams) {
        if( testingTask == "connect" ){
            boolean state = singleHttpConnection();
            if( state ){
                test_result_internet_connected = true;
                return "Connection Success";
            }
            test_result_internet_connected = false;
            return "Connection Failed";
        }
        else if( testingTask == "download"){
            boolean can_connect = singleHttpConnection();
            if( can_connect ){
                boolean single_download_success = singleHttpDownload();
                if( single_download_success )
                    return "downloaded";
                else{
                    return "not_downloaded";
                }
            }

        }
        return "undefined task";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(testingTask == "download"){
            msg.shrt("开始测试下载");
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Activity activity = testActivity.get();
        EditText progress_text = (EditText)activity.findViewById(R.id.app_bar_progress_text);
        if( progress_text != null ){
            progress_text.setEnabled(false);

            for(Integer v : values){
                if( progress_text != null ){
                    progress_text.setText(v.toString());
                }
                if( v == 100 ){
                    progress_text.setText("done.");
                }
            }
        }

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if( testingTask == "connect" ){
            if(test_result_internet_connected){
                msg.shrt("网络已链接");
            }
            else{
                msg.shrt("网络阻断");
            }
        }
        else if( testingTask == "download" ){
            if(s == "downloaded" ){
                msg.shrt("成功下载");
                test_result_can_download = true;
            }
            else if( s=="not_downloaded" ){
                msg.shrt("下载失败");
                test_result_can_download = false;
            }
        }

    }
}
