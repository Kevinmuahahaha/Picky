package com.yybb.picky;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.yybb.picky.ui.utils.fileChecker;
import com.yybb.picky.ui.utils.msg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;
import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    public static final String TAG = "RecyclerViewAdapter";
    // private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;
    private int colorCounter = 0;
    private String mRepoName = null;
    private boolean mCanViewFullImage = false;
    private String mPathToDownloadedContent = null;
    private String mUrlPrefix = null;
    private static int mCurrentTotal = 0;
    private View mDecorView = null;
    private final String EXTRA_PATH_TO_IAMGE = "intent_extra_path_to_image";
    private int mColor_R = 255;
    private int mColor_G = 255;
    private int mColor_B = 255;
    private boolean mDirectionColorRAdd = false;
    private boolean mDirectionColorGAdd = false;
    private boolean mDirectionColorBAdd = false;
    private int last_position = 0;
    private Random color_block_id_generator;

    private void colorSetter(int position){
        if(mDirectionColorRAdd){
            mColor_R += Math.abs(color_block_id_generator.nextInt())%3;
            if(mColor_R>=255){
                mDirectionColorRAdd = false;
                mColor_R=255;
            }
        }
        else if(!mDirectionColorRAdd){
            mColor_R -= Math.abs(color_block_id_generator.nextInt())%3;
            if(mColor_R<=0){
                mDirectionColorRAdd = true;
                mColor_R=0;
            }
        }

        if(mDirectionColorGAdd){
            mColor_R += Math.abs(color_block_id_generator.nextInt())%3;
            if(mColor_R>=255){
                mDirectionColorGAdd = false;
                mColor_R=255;
            }
        }
        else if(!mDirectionColorGAdd){
            mColor_R -= Math.abs(color_block_id_generator.nextInt())%3;
            if(mColor_R<=0){
                mDirectionColorGAdd = true;
                mColor_R=0;
            }
        }

        if(mDirectionColorBAdd){
            mColor_R += Math.abs(color_block_id_generator.nextInt())%3;
            if(mColor_R>=255){
                mDirectionColorBAdd = false;
                mColor_R=255;
            }
        }
        else if(!mDirectionColorBAdd){
            mColor_R -= Math.abs(color_block_id_generator.nextInt())%3;
            if(mColor_R<=0){
                mDirectionColorBAdd = true;
                mColor_R=0;
            }
        }

        /*
        if(position > last_position){
            mColor_R -=3;
            mColor_G -=3;
            mColor_B -=3;
        }
        else if(position > last_position){
            mColor_R +=3;
            mColor_G +=3;
            mColor_B +=3;
        }
        last_position = position;
        ----------------------------------------I hate it
         */
        /*
         *   Test Color Code
         *       0x01 0x56 0x68
         *       0x26 0x3f 0x44
         *       0xff 0xd3 0x69
         *       0xff 0xf1 0xcf
         * */
        /*
        if(position<mCurrentTotal/4){
            if(mColor_R>0x01)mColor_R--;
            if(mColor_R<0x01)mColor_R++;
            if(mColor_G>0x56)mColor_G--;
            if(mColor_G<0x56)mColor_G++;
            if(mColor_B>0x68)mColor_B--;
            if(mColor_B<0x68)mColor_B++;
        }
        else if(position<mCurrentTotal*2/4 && position>mCurrentTotal/4){
            if(mColor_R>0x26)mColor_R--;
            if(mColor_R<0x26)mColor_R++;
            if(mColor_G>0x3f)mColor_G--;
            if(mColor_G<0x3f)mColor_G++;
            if(mColor_B>0x44)mColor_B--;
            if(mColor_B<0x44)mColor_B++;
        }
        else if(position>mCurrentTotal*2/4 && position<mCurrentTotal*3/4){
            if(mColor_R>0xff)mColor_R--;
            if(mColor_R<0xff)mColor_R++;
            if(mColor_G>0xd3)mColor_G--;
            if(mColor_G<0xd3)mColor_G++;
            if(mColor_B>0x69)mColor_B--;
            if(mColor_B<0x69)mColor_B++;
        }
        else{
            if(mColor_R>0xff)mColor_R--;
            if(mColor_R<0xff)mColor_R++;
            if(mColor_G>0xf1)mColor_G--;
            if(mColor_G<0xf1)mColor_G++;
            if(mColor_B>0xcf)mColor_B--;
            if(mColor_B<0xcf)mColor_B++;
        }
        I don't like it.
         */
    } //piece of shit, dumped


    DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();


    private String getImagePath(int position){
            String image_name = mImages.get(position);
            String image_name_no_prefix_postfix
                    = image_name.substring(image_name.lastIndexOf('/')+1, image_name.length());
            //Log.wtf(TAG,image_name_no_prefix_postfix);
            String downloadable_file_name =  image_name_no_prefix_postfix
                    .substring(image_name_no_prefix_postfix.indexOf('_')+1
                            ,image_name_no_prefix_postfix.lastIndexOf('_'));
            String constructed_download_link =
                    mUrlPrefix + "/" + downloadable_file_name;
            //Log.wtf(TAG,"Downloading: " + constructed_download_link);
            String path_to_downloaded_file =
                    mPathToDownloadedContent + "/" + downloadable_file_name;
            return path_to_downloaded_file;
    }

    RecyclerViewAdapter(Context context,ArrayList<String> images,String repoName, View backgroundView){
        fileChecker.folderInit();
        color_block_id_generator = new Random();
        color_block_id_generator.setSeed(new Date().getTime());
        mDecorView = backgroundView;
        mImages = images;
        mContext = context;
        mRepoName = repoName;
        mPathToDownloadedContent = folderPrefix + "/" + mRepoName + "/downloaded";
        String path_to_description = folderPrefix + "/" + mRepoName + "/repo.desc";
        File file_path_to_descript = new File(path_to_description);
        try { // setting up download url prefix
            FileInputStream fis = new FileInputStream(path_to_description);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,"utf-8"));
            StringBuilder sb = new StringBuilder();
            sb.append(br.readLine());

            mUrlPrefix = sb.toString();

            fis.close();
            File create_folder = new File(mPathToDownloadedContent);
            if ( !create_folder.exists() ){
                if ( create_folder.mkdirs() ){
                    mCanViewFullImage = true;
                }
                else{
                    mCanViewFullImage = false;
                }
            }
            else{
                mCanViewFullImage = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCanViewFullImage = false;
        }
        mCurrentTotal = getItemCount();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        colorCounter++; colorCounter%=4;
        int tmp_color_id = 0;
        if( colorCounter == 0 ) tmp_color_id = R.drawable.scheme_sea_placeholder_block_blue_1;
        if( colorCounter == 1 ) tmp_color_id = R.drawable.placeholder_block_blue;
        if( colorCounter == 2 ) tmp_color_id = R.drawable.scheme_sea_placeholder_block_blue_2;
        if( colorCounter == 3 ) tmp_color_id = R.drawable.scheme_sea_placeholder_block_blue_3;


        if(new File(getImagePath(position)).exists()){
            /*
            Downloaded images have out line:

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.image.setBackground(mContext.getDrawable(R.drawable.circle));
            }
            Looks Okay, but I don't like it
             */
            Glide.with(mContext)
                    .asBitmap()
                    .load(mImages.get(position))
                    .centerCrop()
                    .transition(withCrossFade(factory))
                    .placeholder(tmp_color_id)
                    .into(holder.image);
        }
        else{
            Glide.with(mContext)
                    .asBitmap()
                    .load(tmp_color_id)
                    .centerCrop()
                    .placeholder(tmp_color_id)
                    .transition(withCrossFade(factory))
                    .into(holder.image);
        }

        holder.parentLayout.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        msg.context = mContext;
                        if( DownloadAndShowImage.hasOngoingDownload() ){
                            msg.shrt("一张张看哦～");
                            return  false;
                        }
                        //Log.d(TAG,"Clicked on " + mImages.get(position));
                        String image_name = mImages.get(position);
                        //msg.shrt("Clicked on " + image_name);

                        // show thumb
                        Glide.with(mContext)
                                .asBitmap()
                                .load(mImages.get(position))
                                .centerCrop()
                                .transition(withCrossFade(factory))
                                .into(holder.image);

                        if( mCanViewFullImage ){
                            String image_name_no_prefix_postfix
                                    = image_name.substring(image_name.lastIndexOf('/')+1, image_name.length());
                            Log.wtf(TAG,image_name_no_prefix_postfix);
                            String downloadable_file_name =  image_name_no_prefix_postfix
                                    .substring(image_name_no_prefix_postfix.indexOf('_')+1
                                            ,image_name_no_prefix_postfix.lastIndexOf('_'));
                            String constructed_download_link =
                                    mUrlPrefix + "/" + downloadable_file_name;
                            Log.wtf(TAG,"Downloading: " + constructed_download_link);
                            String path_to_downloaded_file =
                                    mPathToDownloadedContent + "/" + downloadable_file_name;
                            if(!new File(path_to_downloaded_file).exists()){

                                msg.shrt("准备图片中...");
                                DownloadAndShowImage dasi = new DownloadAndShowImage(mContext,constructed_download_link,path_to_downloaded_file);
                                new Thread(dasi).start();
                                Log.wtf(TAG,"Preparing Image: " + path_to_downloaded_file);
                                //in thread
                                //wait for download an start DisplayOneImage

                            }else{
                                Intent showOneImage = new Intent(mContext,ShowOneImageActivity.class);
                                Intent intent = showOneImage.putExtra(EXTRA_PATH_TO_IAMGE, path_to_downloaded_file);
                                mContext.startActivity(showOneImage);
                            }
                        }
                        else{
                            msg.text("repo.desc缺失，无法下载");
                        }
                        return false;
                    }
                }
        );
        // holder.imageurl.setText
        holder.parentLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if( !new File(getImagePath(position)).exists()){
                            // show thumb if image hasn't been downloaded
                            Glide.with(mContext)
                                    .asBitmap()
                                    .load(mImages.get(position))
                                    .centerCrop()
                                    .transition(withCrossFade(factory))
                                    .into(holder.image);
                        }
                        else{
                            String path_to_downloaded_file = getImagePath(position);
                            Intent showOneImage = new Intent(mContext,ShowOneImageActivity.class);
                            Intent intent = showOneImage.putExtra(EXTRA_PATH_TO_IAMGE, path_to_downloaded_file);
                            mContext.startActivity(showOneImage);
                        }


                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public class  ViewHolder extends RecyclerView.ViewHolder{
        // the adapter holds the images in memory
        // each one of them
        ImageView image;
        // String image_url;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.test_image);
            // image_url = download_name
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
