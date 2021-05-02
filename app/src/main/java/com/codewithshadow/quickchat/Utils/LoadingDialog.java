package com.codewithshadow.quickchat.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.codewithshadow.quickchat.R;


public class LoadingDialog {
    Activity activity;
    AlertDialog dialog;
    TextView textView;

    public LoadingDialog(Activity myactivity)
    {
        activity=myactivity;
    }
    public void startloadingDialog()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        LayoutInflater inflater=activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progressdialog,null));
        builder.setCancelable(false);
        dialog=builder.create();
        dialog.show();
    }
    public void dismissDialog()
    {
        dialog.dismiss();
    }

    public  void textDiaglog(String message)
    {
        textView = dialog.findViewById(R.id.loading_text);
        textView.setText(message);
    }
}
