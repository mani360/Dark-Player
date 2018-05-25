package com.andygopu.androidantariksa.blackplay;

import android.app.TabActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Resources;

import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bttplaynow=findViewById(R.id.btn_nowplaylist);

        Intent playerServiceIntent = new Intent(this, MusicService.class);
        startService(playerServiceIntent);

        Resources res = getResources();
        TabHost tabHost=getTabHost();
        TabSpec spec;
        Intent intent;

        intent = new Intent().setClass(this, AlbumPlaylist.class);
        spec = tabHost.newTabSpec("playlist_album").setIndicator(res.getString(R.string.album), res.getDrawable(R.drawable.ic_tab_file_browser_grey)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, TrackPlaylist.class);
        spec = tabHost.newTabSpec("playlist_track").setIndicator(res.getString(R.string.track), res.getDrawable(R.drawable.ic_launcher_background)).setContent(intent);
        tabHost.addTab(spec);


        intent = new Intent().setClass(this, ArtistPlaylist.class);
       spec = tabHost.newTabSpec("playlist_artist").setIndicator(res.getString(R.string.artist), res.getDrawable(R.drawable.ic_tab_file_browser_white)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this,FileBrowserActivity.class);
        spec = tabHost.newTabSpec("file_browser").setIndicator(res.getString(R.string.act_file_browser), res.getDrawable(R.drawable.ic_tab_file_browser)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);

        bttplaynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nowplayIntent=new Intent(MainActivity.this,NowPlayingActivity.class);
                startActivity(nowplayIntent);

            }
        });
    }
}
