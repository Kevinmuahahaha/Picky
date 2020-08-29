package com.yybb.picky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.ortiz.touchview.TouchImageView;
import com.yybb.picky.ui.utils.msg;

import java.io.File;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;



public class ShowOneImageActivity extends AppCompatActivity {
    private final String EXTRA_PATH_TO_IAMGE = "intent_extra_path_to_image";
    private String mPathToImage = null;

    private void openInAnotherApp(String image_path){

        try
        {
            //msg.shrtf(image_path);
            File file = new File(image_path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(photoURI, "image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Open In"));
        }
        catch (Exception e)
        {
            msg.textf(e.getMessage().toString());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_one_image);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mPathToImage = getIntent().getStringExtra(EXTRA_PATH_TO_IAMGE);
        if(mPathToImage == null){
            msg.text("未提供图片位置，无法显示");
            return;
        }
        TouchImageView displayImageView = (TouchImageView)findViewById(R.id.showImageHolderView);
        //displayImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        //displayImageView.setZoomEnabled(true);
        displayImageView.setScaleType(ImageView.ScaleType.CENTER);
        displayImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mPathToImage != null)  openInAnotherApp(mPathToImage);
                return true;
            }
        });
        int tmp_color_id = 0;
        tmp_color_id = R.drawable.placeholder_black_screen;
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(this)
                .asBitmap()
                .load(mPathToImage)
                //.centerCrop()
                .transition(withCrossFade(factory))
              //  .placeholder(tmp_color_id)
                .into(displayImageView);
    }
}
