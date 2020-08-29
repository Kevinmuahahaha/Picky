package com.yybb.picky.ui.addrepo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.yybb.picky.R;
import com.yybb.picky.RepoSetupService;
import com.yybb.picky.ui.utils.msg;
import com.yybb.picky.ui.utils.permissionChecker;
import com.yybb.picky.ui.utils.repoManager;

import java.net.MalformedURLException;
import java.net.URL;

public class AddRepoFragment extends Fragment {

    private AddRepoViewModel addRepoViewModel;
    private String m_Text_url = null; // <--- fetched Repo String
    private String m_Text_name = null;
    private static final String INFORM_DOWNLOAD_LINK = "inform_download_link";
    private static final String INFORM_DOWNLOAD_LOCATION = "inform_download_location";
    private static final String INFORM_DOWNLOAD_REPO_NAME = "inform_download_repo_name";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        addRepoViewModel =
                ViewModelProviders.of(this).get(AddRepoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        addRepoViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

                  //  textView.setText("bab");
            }
        });
        ProgressBar pb = root.findViewById(R.id.add_repo_progressbar);
        repoManager.add_repo_indecator_bar = pb;
        ImageView add_repo = (ImageView)root.findViewById(R.id.button_add_repo);
        add_repo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity this_tmp = getActivity();
                permissionChecker.requestRead(this_tmp);
                permissionChecker.requestWrite(this_tmp);

                if( !permissionChecker.canWrite(this_tmp) || !permissionChecker.canRead(this_tmp) ){
                    msg.shrtf("需要权限哦");
                    return;
                }

                if(repoManager.hasRunningSetupTask()){
                    msg.shrtf("上一个任务还没结束");
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("添加Repo");

// Set up the input
                final EditText input_url = new EditText(getContext());
                final EditText input_name = new EditText(getContext());

                input_url.setHint("Repo地址");
                input_name.setHint("给Repo取一个名称");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input_url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                input_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                layout.addView(input_url);
                layout.addView(input_name);

                builder.setView(layout);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text_url = input_url.getText().toString();
                        m_Text_name = input_name.getText().toString();
                        // check connection
                        // download
                        if( m_Text_name.length() > 0 && m_Text_url.length() > 0  ){
                            try{
                                URL test = new URL(m_Text_url);
                            }
                            catch (MalformedURLException e){
                                msg.shrt("Repo链接不可用");
                                return;
                            }
                            if( ! m_Text_url.endsWith("thumbs.zip")){
                                //! m_Text_url.startsWith("http://q34hxm3.xyz/storage/"
                                msg.shrt("Repo链接不可用");
                                return;
                            }
                            /*

                             fetch link "head"
                             e.g  from:   http://q34hxm3.xyz/storage/RIPS/rips/reddit_sub_EarthPorn/thumbs.zip
                                  to:     http://q34hxm3.xyz/storage/RIPS/rips/reddit_sub_EarthPorn/
                             so that on image click, there'll be a valid download link

                             */
                            String preceedingPath = m_Text_url.substring(0, m_Text_url.lastIndexOf('/'));
                            //msg.shrt("Head: " + preceedingPath);
                            msg.shrt("Picky正为您全速配置");
                            // Setup Routine, 2 lines below
                            if ( repoManager.createRepoWithName(m_Text_name,preceedingPath) ){
                                Intent setupService = new Intent(getActivity(), RepoSetupService.class);
                                setupService.putExtra(INFORM_DOWNLOAD_LINK, m_Text_url);
                                setupService.putExtra(INFORM_DOWNLOAD_REPO_NAME, m_Text_name);
                                getActivity().startService(setupService);
                            }
                            else{
                                msg.shrt("无法创建Repo记录");
                            }
                        }
                        else{
                            msg.shrt("信息不全，无法配置");
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
        return root;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
}