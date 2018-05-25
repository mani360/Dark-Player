package com.andygopu.androidantariksa.blackplay;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.andygopu.androidantariksa.blackplay.slidingPanel.SlidingUpPanelLayout;

public class PlayerActivity extends AppCompatActivity {
    SlidingUpPanelLayout panelLayout;
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
    }
}
