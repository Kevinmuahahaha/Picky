package com.yybb.picky.ui.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
/*
*   TODO:
*    - implement progress bar
*    - update progressbar UI with onProgressUpdate
*
*
* */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class fileDownloader extends AsyncTask<String, Integer, String> {
    private EditText progressText = null;
    private String original_progress_text = null;
    private int    original_progress_visibility = 0;
    private boolean file_length_reported = false;
    @SuppressLint("WrongConstant")
    public void setProgressTextIndicator(EditText editText){
        progressText = editText;
        if(editText != null){
            original_progress_visibility = editText.getVisibility();
            original_progress_text = editText.getText().toString();
        }
    }

    private boolean singleHttpDownload(String single_url_string){
        String filename = single_url_string.substring( single_url_string.lastIndexOf('/')+1, single_url_string.length() );
        File test_exist = new File(folderPrefix + "/" + filename);

        // only downloads file when it doesn't exists
        // even when the file is empty, the download is still skipped
        // thus file integrity isn't checked
        if( test_exist.exists() ){
            return false;
        }
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(single_url_string);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            if( fileLength == -1 )
                file_length_reported = false;
            else file_length_reported = true;
            // download the file
            input = connection.getInputStream();

            output = new FileOutputStream(folderPrefix + "/" + filename);
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return false;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0){
                    publishProgress((int) (total * 100 / fileLength));
                }
                else{
                    publishProgress((int)total);
                }

                output.write(data, 0, count);
            }
        } catch (Exception e) {
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
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if( progressText != null ){
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("0");
                for( int a_value : values ){
                    if( file_length_reported )
                        progressText.setText("info: " + a_value);
                    else if( !file_length_reported ){
                        progressText.setText(msg.humanReadableByteCountBin(a_value));
                    }
                }
            }
    }

    @Override
    protected String doInBackground(String... strings) {
        for( String single_string : strings ){
            /*
            *   TODO:
            *    Update UI
            *    ------------------------------
            *    When there's multiple downloads
            *    maybe get multiple download bar/text?
            *    yeah, instead of displaying the average rate
            *    multiple bar is better.
            *
            *
            * */
            singleHttpDownload(single_string);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(progressText != null){
            progressText.setText(original_progress_text);
            progressText.setVisibility(original_progress_visibility);
        }
    }
}
