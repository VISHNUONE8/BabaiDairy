package com.andrayudu.babaidairy.utility;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.andrayudu.babaidairy.R;

public class ProgressButton {


    private CardView cardView;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private TextView textView;
    private String btnName;
    Animation fade_in;

     public ProgressButton(Context ct, View view,String BtnName){
        cardView = view.findViewById(R.id.CardLoading);
        relativeLayout = view.findViewById(R.id.cardInside);
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.progressBtnText);
        btnName = BtnName;
        textView.setText(btnName);
    }

    public void buttonActivated(){
         textView.setVisibility(View.GONE);
         progressBar.setVisibility(View.VISIBLE);

    }



   public void buttonFinished(){
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        if (btnName == "LOG IN"){
            textView.setText("LOG IN");
        }
        else if (btnName == "REGISTER"){
            textView.setText("Register");
        }
        else if(btnName == "ORDER NOW"){
            textView.setText("ORDER NOW");
        }
        else if (btnName == "NOTIFY"){
            textView.setText("NOTIFY");
        }
        else if (btnName == "DISPATCH"){
            textView.setText("DISPATCH");
        }
        else if (btnName == "UPDATE"){
            textView.setText("UPDATE");
        }
        else if (btnName == "SEND LINK"){
            textView.setText("SEND LINK");
        }
   }

}
