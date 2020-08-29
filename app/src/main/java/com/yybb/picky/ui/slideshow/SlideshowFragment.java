package com.yybb.picky.ui.slideshow;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yybb.picky.R;
import com.yybb.picky.ui.utils.msg;
import com.yybb.picky.ui.utils.permissionChecker;
import com.yybb.picky.ui.utils.repoManager;

import java.util.ArrayList;

public class SlideshowFragment extends Fragment {
    private SlideshowViewModel slideshowViewModel;
    private static final String TAG = "SlideshowFragment";
    private ArrayList<String> LIST_OF_REPOS = new ArrayList<String>();
    private ArrayList<String> LIST_OF_ALIAS = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
               // textView.setText(s);

            }
        });

        Activity this_tmp = getActivity();
        permissionChecker.requestRead(this_tmp);
        permissionChecker.requestWrite(this_tmp);

        if( !permissionChecker.canRead(this_tmp) || !permissionChecker.canWrite(this_tmp)
                ||  repoManager.getListOfRepos().size() < 1
        ){
            if( !permissionChecker.canRead(this_tmp) ){
                msg.shrtf(".-- - ..--");
            }
            else{
                msg.shrtf("莫得东西");
            }

            return root;
        }

        initRecyclerView(root);
        return root;
    }

    private void initRecyclerView(View root){
        Log.d(TAG,"initRecyclerView called.");

        for( repoManager.RepoNotation notation : repoManager.getListOfRepos() ){
            LIST_OF_REPOS.add(notation.repo_name);
            LIST_OF_ALIAS.add(notation.repo_alias);
        }

        // msg.shrt("Init Recyclerview");

        RecyclerView recyclerView = root.findViewById(R.id.fragment_slideshow_recyclerview);
        RepoListRecyclerViewAdapter adapter = new RepoListRecyclerViewAdapter(getActivity(), LIST_OF_REPOS, LIST_OF_ALIAS);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
    }
}