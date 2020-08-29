package com.yybb.picky.ui.utils;

import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

public class DownloadUI {
    private static boolean  NINTY_FIVE_MARK_SHOWN = false;
    private static boolean  SEVENTY_FIVE_MARK_SHOWN = false;
    private static ProgressBar mSharedDownloadProgressBar = null;
    private static int total_length = -1;
    private static int downloaded_bytes = 0;
    private static boolean show_encouragement = false;
    public synchronized  static void cleanUp(){
        total_length = -1;
        downloaded_bytes = 0;
        if(mSharedDownloadProgressBar != null ) mSharedDownloadProgressBar.setVisibility(View.INVISIBLE);
        show_encouragement = false;
        NINTY_FIVE_MARK_SHOWN = false;
    }
    public synchronized  static void showProgressBar(){
        if(mSharedDownloadProgressBar != null ) mSharedDownloadProgressBar.setVisibility(View.VISIBLE);
    }
    public synchronized  static void setProgressBar(ProgressBar pb){
        mSharedDownloadProgressBar = pb;
    }
    public synchronized static void setProgressBarValue(int val){
        if(mSharedDownloadProgressBar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mSharedDownloadProgressBar.setProgress(val, true);
        }
        else{
            mSharedDownloadProgressBar.setProgress(val);
        }
    }
    public static void setShowEncouragement(boolean show){
        show_encouragement = show;
    }
    public synchronized static void updateDownloadedBytes(int byte_count){
        // do update only when infos are complete
        if( mSharedDownloadProgressBar != null && total_length > 0 ){
            downloaded_bytes += byte_count;
            if( downloaded_bytes >= total_length ){
                setProgressBarValue(100);
                if(show_encouragement){
                    msg.shrtf("再等一下就行");
                }
            }
            else if( downloaded_bytes > 0 ){
                int length_now = (int)(downloaded_bytes*100/total_length);
                if(length_now >= 0) setProgressBarValue( length_now-5 );
                if(show_encouragement) encourage(length_now);
            }
        }
    }
    public synchronized static void updateTotalLength(int length){
        total_length = length;
    }
    private static void encourage(int progress){
        if(!NINTY_FIVE_MARK_SHOWN && progress>=95){
            NINTY_FIVE_MARK_SHOWN = true;
            msg.shrtf("再等等");
        }
        if(!SEVENTY_FIVE_MARK_SHOWN && progress>=75 && progress<95){
            SEVENTY_FIVE_MARK_SHOWN = true;
            msg.shrtf("百分之75了");
        }
    }
}
