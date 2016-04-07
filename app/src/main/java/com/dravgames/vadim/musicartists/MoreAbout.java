package com.dravgames.vadim.musicartists;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class MoreAbout extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_about);
    }

    @Override
    public void onBackPressed() {
        Intent k = new Intent(this, MainActivity.class);
        startActivity(k);
    }
}
