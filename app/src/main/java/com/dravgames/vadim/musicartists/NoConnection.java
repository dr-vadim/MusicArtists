package com.dravgames.vadim.musicartists;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.os.Bundle;
import android.view.View;

public class NoConnection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.internet_connetction_title_txt);
    }

    /**
     * Method calls when user click refresh button.
     * @param view
     */
    public void refreshConnection(View view){
        //Check internet connection
        if(Utils.isOnline(this)){
            //if internet connection is open. Open the main activity.
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }else{
            //Else show alert message
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle( R.string.attention_txt )
                    .setMessage(R.string.need_connection_txt)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {

                        }
                    }).show();
        }
    }
}
