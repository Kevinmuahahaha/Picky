package com.yybb.picky.ui.home;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.yybb.picky.R;
import com.yybb.picky.configs.configManager;
import com.yybb.picky.ui.utils.FontCache;
import com.yybb.picky.ui.utils.msg;

import java.util.Date;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private String message_default = "";
    private String message_gus = "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n"+"All work and no play makes Jack a dull boy\n"+
            "All work and no play makes Jack a dull boy\n";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                s = "";
                textView.setText(s);
                Date date = new Date();
                String monthNumber  = (String) DateFormat.format("MM",   date); // 06
                String day          = (String) DateFormat.format("dd",   date); // 20
           //     msg.shrtf(monthNumber+"/"+day);

                // Set greeting message
                Typeface face = FontCache.get("fonts/" + "HanyiSentyCandy-color.ttf", getContext());
                TextView greet_TextView = root.findViewById(R.id.gusjohnson);
                greet_TextView.setTypeface(face);

                if ( !configManager.init(getActivity()) ){
                    greet_TextView.setText(message_default);
                    return;
                }

                String boot_count = configManager.readConfig("boot count");
                //Log.wtf(TAG,monthNumber);
                //Log.wtf(TAG,day);
                int NumberedMonth = new Integer(monthNumber);
                int NumberedDay   = new Integer(day);
                if(boot_count == null){
                    //msg.shrt("first boot");
                    configManager.writeConfig("boot count", "1");
                    greet_TextView.setText(message_default);
                }
                else if ( NumberedMonth == 2 && NumberedDay == 22 ){ // if is birthday
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        greet_TextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                        greet_TextView.setTextColor(Color.rgb(0xf6,0x72,0x80));
                        //f67280
                    }
                    greet_TextView.setText("    生日快乐！！\n"+"    \t--from yybb to yybb\n"
                    +"\n\n\n" +
                                    "       (^) (^) (^) (^)\n" +
                                    "       _i___i___i___i_\n" +
                                    "      (________cake__)\n" +
                                    "      |####|>o<|###|\n" +
                                    "      (______________)\n"


                    );
                    //msg.shrtf("set");
                }
                else{
                    int count = new Integer(boot_count);
                    if(count%7==0){
                        Greeter.greet(greet_TextView);
                    }
                    if(count%3==0){
                        Greeter.taisai(greet_TextView);
                    }
                    else{
                        switch (count){
                            case 52:
                                greet_TextView.setText("第52次点开首页");
                                break;
                            case 100:
                                greet_TextView.setText("打开首页100次了");
                                break;
                            case 13:
                                greet_TextView.setText("这是你第13次点开首页");
                                break;
                            default:
                                greet_TextView.setText(message_gus);
                                break;
                        }
                    }

                    count++;
                    configManager.writeConfig("boot count", ""+count);
                }
            }
        });
        // reset title to null???

        return root;
    }


}