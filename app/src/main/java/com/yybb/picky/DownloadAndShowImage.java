package com.yybb.picky;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yybb.picky.ui.utils.DownloadUI;

import java.util.concurrent.ExecutionException;

public class DownloadAndShowImage implements Runnable {
    private static final String TAG = "DownloadAndShowImage";
    private final String STATUS_DOWNLOAD_DONE = "download_done";
    private final String EXTRA_PATH_TO_IAMGE = "intent_extra_path_to_image";
    private Context mContext = null;
    private  String mDownloadLink = null;
    private  String mPathToDownloadedFile = null;

    private static boolean mHasOnGoingDownload = false;
    DownloadAndShowImage(Context from_context,String download_link, String path_to_downloaded_file){
        mContext = from_context;
        mDownloadLink = download_link;
        mPathToDownloadedFile = path_to_downloaded_file;
    }
    public static  boolean hasOngoingDownload(){
        return mHasOnGoingDownload;
    }
    @Override
    public void run() {
        mHasOnGoingDownload = true;
        kleinUndSchnell kus = new kleinUndSchnell();
        kus.execute(mDownloadLink,mPathToDownloadedFile);
        try {
            kus.get();
            if(kus.isSuccess()){
                Intent displayOneImage = new Intent(mContext,ShowOneImageActivity.class);
                Intent intent = displayOneImage.putExtra(EXTRA_PATH_TO_IAMGE, mPathToDownloadedFile);
                mContext.startActivity(displayOneImage);
            }
            else{
                //TODO: this is shit, change the following code to make it work.
                // Preferably with notification
                Log.wtf(TAG, "Error: Fail to download");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.wtf(TAG, "Error: Fail to download");
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.wtf(TAG, "Error: Fail to download");
        }
        mHasOnGoingDownload = false;
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                DownloadUI.cleanUp();
            }
        });

    }
}
