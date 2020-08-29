package com.yybb.picky.ui.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yybb.picky.R;
import com.yybb.picky.kleinUndSchnell;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.yybb.picky.ui.utils.fileChecker.folderInit;
import static com.yybb.picky.ui.utils.sharedStatus.folderPrefix;

public class repoManager {
    private static final String TAG = "repoManager";
    private static final String STATUS_DOWNLOAD_DONE = "download_done";
    private static boolean has_running_setup_task = false;
    public static  ProgressBar add_repo_indecator_bar = null;
    public static class RepoNotation{
        public String repo_name = null;
        public String repo_alias = null;
    }
    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }
    public static void unzipDaemon(File zipFile, File targetDirectory) {
        new asyncDaemon().execute(zipFile, targetDirectory);
    }
    private static class asyncDaemon extends AsyncTask<File, Integer, String>{
        @Override
        protected String doInBackground(File... files) {
            try {
                unzip(files[0], files[1]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if( s== "ok" ){
                msg.shrt("Done unzipping.");
            }
            else if( s== "error"){
                msg.shrt("Fail to unzip");
            }
            else{
                msg.shrt("Undefined result code");
            }
        }
    }
    public static boolean createRepoWithName(String repo_name, String content_url_prefix){
        folderInit();
        //try to make sure repo directory exists
        String path_to_repo = folderPrefix + "/" + "repo_" + repo_name + "/";
        File file_path_to_repo = new File(path_to_repo);
        File file_path_to_description = new File(path_to_repo + "repo.desc");
        Log.wtf(TAG,"Generating Repo Description");
        if( !file_path_to_repo.exists() ) file_path_to_repo.mkdirs();
        try {
            // record url prefix for generating download link when viewing full content
            FileOutputStream fos = new FileOutputStream(file_path_to_description);
            fos.write(content_url_prefix.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }
        return  true;
    }
    public static ArrayList<RepoNotation> getListOfRepos(){
        ArrayList<RepoNotation> ret = new ArrayList<RepoNotation>();
        if( folderPrefix == null ) folderInit();
        File directory = new File(folderPrefix);
        for( File f : directory.listFiles() ){
            RepoNotation tmp = new RepoNotation();
            if( f.getName().startsWith("repo_") ){
                tmp.repo_name = f.getName();
                tmp.repo_alias = "none"; // TODO: Place holder
                ret.add(tmp);
            }
            // TODO: implement get_alias()
            // ....
        }
        return ret;
    }
    public static class SimpleRepoSetup implements Runnable {
        private static class NotificationID {
            private final static AtomicInteger c = new AtomicInteger(0);
            public static int getID() {
                return c.incrementAndGet();
            }
        }
        private  String repoName;
        private  String downloadUrl;
        public static  Context mContext;
        private final String CHANNEL_ID = "PICKY_NOTIFICATION_CHANNEL";
        public SimpleRepoSetup(String repo_name, String download_url, Context context){
            repoName = repo_name;
            downloadUrl = download_url;
            mContext = context;
        }
        @Override
        public void run() {
            has_running_setup_task = true;
            // TODO: how to notify user??
            boolean status = setupRepoWithDownload(repoName,downloadUrl);
            createNotificationChannel();

            if (  status ){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setContentTitle("Repo添加成功")
                        .setSmallIcon(R.drawable.placeholder_block_blue)
                        .setContentText("可以查看 " + repoName + " 了")
                        //.setStyle(new NotificationCompat.BigTextStyle()
                          //      .bigText("Much longer text that cannot fit one line..."))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                notificationManager.notify(NotificationID.getID(),builder.build());
            }
            else{
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setContentTitle("Repo添加失败")
                        .setSmallIcon(R.drawable.placeholder_block_yellow)
                        .setContentText("Repo: " + repoName + " 添加失败")
                        //.setStyle(new NotificationCompat.BigTextStyle()
                          //      .bigText("Much longer text that cannot fit one line..."))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                notificationManager.notify(NotificationID.getID(),builder.build());
            }
            has_running_setup_task = false;
        }
        private void createNotificationChannel() {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Picky";
                String description = "RepoAdded";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NotificationManager.class);
                channel.enableVibration(true); //Set if it is necesssary
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    public static boolean hasRunningSetupTask(){ return has_running_setup_task; }
    public static boolean setupRepoWithDownload(String repo_name, String download_url){ // simply give it a suitable name
        // Downloads the thumbs from server
        //      only thumbs !!
        // Setup repo directory
        folderInit();
        String zip_file_name = repo_name + ".zip";
        String extract_repo_name = "repo_" + repo_name;
        String path_download_zip = folderPrefix + "/" + zip_file_name;
        String path_extract_zip_directory = folderPrefix + "/" + extract_repo_name + "/thumbs/";
        if(add_repo_indecator_bar!=null)  DownloadUI.setProgressBar(add_repo_indecator_bar);
        DownloadUI.setShowEncouragement(true);
        kleinUndSchnell kus = new kleinUndSchnell();
        //msg.textf("开始下载...");
        kus.setSplinterCount(55);
        kus.setRescueSplinterCount(5);
        kus.execute(download_url, path_download_zip);

        try {
            if(kus.get() == STATUS_DOWNLOAD_DONE){
                File zip_file = new File(path_download_zip);
                File extract_directory = new File(path_extract_zip_directory);
                try {
                    unzip(zip_file, extract_directory);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            else{
                return false;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
