package com.andygopu.androidantariksa.blackplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andygopu.androidantariksa.blackplay.model.Album;
import com.andygopu.androidantariksa.blackplay.model.Artist;
import com.andygopu.androidantariksa.blackplay.model.Track;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TrackPlaylist extends AppCompatActivity {
    final static public int ARTISTS = 0, ALBUMS = 1, TRACKS = 2;
    private int currentLevel;
    private ListView libraryListView;
    private TextView libraryTitle;
    private int currentArtist, currentAlbum;
    private String currentArtistName, currentAlbumName;
    private ArrayAdapter<Track> trackAdapter;
    private HashMap<Integer, Bitmap> albumArts;
    private String artistsTitle, yearLabel;
    private Object mutex;
    private MusicService playerService;

    private ServiceConnection playerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            MusicService.PlayerBinder playerBinder = (MusicService.PlayerBinder)service;
            playerService = playerBinder.getService();
            show();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_track);

       albumArts = new HashMap<Integer, Bitmap>();
        yearLabel = getResources().getString(R.string.library_year_label);
        mutex = new Object();

        SharedPreferences settings = getSharedPreferences("settings", 0);
        currentLevel = settings.getInt("currentLevel", 0);

        libraryListView = (ListView)findViewById(R.id.track_list);
        libraryTitle = (TextView)findViewById(R.id.track_title);
        libraryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                        playerService.addTrack(playerService.new Track(trackAdapter.getItem(pos).getId()));


            }
        });
        trackAdapter = new ArrayAdapter<Track>(this, R.layout.library_track_item, 0) {

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                TrackPlaylist.TrackViewHolder holder = null;
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.library_track_item, null);
                    holder = new TrackPlaylist.TrackViewHolder();
                    holder.name = (TextView)v.findViewById(R.id.library_track_name);
                    v.setTag(holder);
                } else {
                    holder = (TrackPlaylist.TrackViewHolder)v.getTag();
                }
                holder.name.setText(getItem(pos).getNumber()+". "+getItem(pos).getName());
                return v;
            }

        };
       // libraryListView.setAdapter(trackAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent playerServiceIntent = new Intent(this, MusicService.class);
        getApplicationContext().bindService(playerServiceIntent, playerServiceConnection, 0);
    }

    @Override
    protected void onStop() {
        getApplicationContext().unbindService(playerServiceConnection);
        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("currentLevel", currentLevel);
        editor.putInt("currentArtist", currentArtist);
        editor.putInt("currentAlbum", currentAlbum);
        editor.commit();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && currentLevel > 0) {
            currentLevel--;
            show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void show() {
                libraryTitle.setText(artistsTitle+" > "+currentArtistName+" > "+currentAlbumName);
                showTracks();

    }
    private void showTracks() {
        Cursor trackCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.TRACK}, null, null, MediaStore.Audio.AudioColumns.TRACK);
        trackAdapter.clear();
        for (int n = 1; trackCursor.moveToNext(); n++) {
            trackAdapter.add(new Track(Integer.parseInt(trackCursor.getString(0)), trackCursor.getString(1), n));
        }
        libraryListView.setAdapter(trackAdapter);
    }

    static private class TrackViewHolder {
        TextView name;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.track_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.track_menu_remove:
                playerService.addTrack(info.position);
                break;
            case R.id.track_menu_info:
                MusicService.Track currentTrack = playerService.getTrack(info.position);
                String message = "Artist: "+currentTrack.getArtist()+"\nAlbum: "+currentTrack.getAlbum()+"\nYear: "+currentTrack.getYear()+"\nGenre: "+currentTrack.getGenre();


        }
        return super.onContextItemSelected(item);
    }

}
