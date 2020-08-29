package com.yybb.picky.ui.slideshow;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.yybb.picky.DisplayOneRepo;
import com.yybb.picky.R;

import java.util.ArrayList;

import static com.yybb.picky.ui.utils.sharedStatus.REPO_NOTIFICATION;

public class RepoListRecyclerViewAdapter extends  RecyclerView.Adapter<RepoListRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RepoListRecyclerViewAda";
    private ArrayList<String> mLIST_OF_REPOS;
    private ArrayList<String> mLIST_OF_ALIAS;
    private String mNewAlias;
    private Activity mActivity;
    public RepoListRecyclerViewAdapter(Activity activity, ArrayList<String> list_of_repos, ArrayList<String> list_of_alias) {
        mLIST_OF_REPOS = list_of_repos;
        mLIST_OF_ALIAS = list_of_alias;
        mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_repo_list_item, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RepoListRecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder called.");
        holder.repo_name.setText(mLIST_OF_REPOS.get(position));

        holder.repo_name.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DisplayOneRepo.class);
                        intent.putExtra(REPO_NOTIFICATION,mLIST_OF_REPOS.get(position));
                        mActivity.startActivity(intent);
                    }
                }
        );
        /*
        holder.repo_alias.setOnLongClickListener(
                new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle("修改别名");

// Set up the input
                        final EditText input = new EditText(mActivity);
                        input.setHint("新别名");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                        builder.setView(input);

// Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNewAlias = input.getText().toString();
                                holder.repo_alias.setText(mNewAlias);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                        return false;
                    }
                }
        );


        holder.repo_alias.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, DisplayOneRepo.class);
                        intent.putExtra(REPO_NOTIFICATION,mLIST_OF_REPOS.get(position));
                        mActivity.startActivity(intent);
                    }
                }
        );

        */

    }

    @Override
    public int getItemCount() {
        return mLIST_OF_REPOS.size();
    }
    public class  ViewHolder extends RecyclerView.ViewHolder{
        TextView repo_name;
        TextView repo_alias;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            repo_name = itemView.findViewById(R.id.repo_name);

            parentLayout = itemView.findViewById(R.id.repo_list_item_parent_layout);
        }
    }
}
