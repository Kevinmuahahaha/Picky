package com.yybb.picky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.yybb.picky.ui.utils.DownloadUI;
import com.yybb.picky.ui.utils.fileChecker;
import com.yybb.picky.ui.utils.msg;
import com.yybb.picky.ui.utils.permissionChecker;

import java.io.File;
import java.util.ArrayList;

import static com.yybb.picky.ui.utils.sharedStatus.REPO_NOTIFICATION;
import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class DisplayOneRepo extends AppCompatActivity {
    private static final String TAG = "DisplayOneRepo";
    private String CurrentRepo = null;
    private String CurrentRepoThumbsPath = null;
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private RecyclerView recyclerView = null;
    private View decorView = null;
    private int mColor_R = 0;
    private int mColor_G = 0;
    private int mColor_B = 0;
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    // private ArrayList<String> mImageDownloadLinks = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_display_one_repo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //set up for background color change
        decorView = getWindow().getDecorView();
        ProgressBar downloadProgressBar = findViewById(R.id.progress_bar_show_repo);
        downloadProgressBar.setProgress(0);
        downloadProgressBar.setVisibility(View.INVISIBLE);
        DownloadUI.setProgressBar(downloadProgressBar); // pass progress bar to DownloadUI

        setTitle("Repo Display");

        // Build recyclerView adapter:
        // adapts the individual list_item(s)  to the main container
        // can't just shove them in, a seperate class is needed
        String repoName = getIntent().getStringExtra(REPO_NOTIFICATION);
        if( repoName == null ){
            Log.d(TAG,"No Repo to be displayed");
            msg.text("No Repo to be displayed");
            return;
        }
        fileChecker.folderInit();
        CurrentRepo = repoName;
        //CurrentRepoThumbsPath = folderPrefix + "/repo_" + CurrentRepo + "/thumbs/";
        CurrentRepoThumbsPath = folderPrefix + "/" + CurrentRepo + "/.thumbs/";
        initImageBitmaps(repoName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // use configManager.write ...
        // write recyclerView position to storage
        // reload only once in OnCreate()
    }

    private void initImageBitmaps(String repo_name){
        Log.d(TAG,"DisplayOneRepo: Preparing bitmaps");
        fileChecker.folderInit();
        if(!permissionChecker.canRead(this)){
            permissionChecker.requestRead(this);
        }
        if(!permissionChecker.canRead(this) || ! new File(CurrentRepoThumbsPath).exists() ){
            msg.shrt("无法读取RepoThumbs: " + CurrentRepo.toString());
            return;
        }
        for( String img_file_names : fileChecker.imageNamesInFolder(CurrentRepoThumbsPath)){
            mImageUrls.add(CurrentRepoThumbsPath + img_file_names);
        }
        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG,"Init initRecyclerVIew");
        recyclerView = findViewById(R.id.repo_recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mImageUrls, CurrentRepo, decorView);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(50);
        /*
        recyclerView.setOnFlingListener(
                new RecyclerView.OnFlingListener() {
                    @Override
                    public boolean onFling(int velocityX, int velocityY) {

                        if (Math.abs(velocityY) > velocityY*3/4) {
                            velocityY = velocityY*3/4 * (int) Math.signum((double)velocityY);
                            recyclerView.fling(velocityX, velocityY);
                            return true;
                        }

                        return false;
                    }
                }
        );
         */
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new GridLayoutManager(this,4));

        //( (GridLayoutManager)recyclerView.getLayoutManager() ).scrollToPosition(50);
    }
}
