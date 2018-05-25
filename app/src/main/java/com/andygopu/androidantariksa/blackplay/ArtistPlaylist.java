package com.andygopu.androidantariksa.blackplay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

public class ArtistPlaylist extends AppCompatActivity {
    final static public int ARTISTS = 0, ALBUMS = 1, TRACKS = 2;
    private int currentLevel;
    private ListView libraryListView;
    private TextView libraryTitle;
    private int currentArtist, currentAlbum;
    private String currentArtistName, currentAlbumName;
    private ArrayAdapter<Artist> artistAdapter;
    private ArrayAdapter<Album> albumAdapter;
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
        setContentView(R.layout.playlist_artist);

        albumArts = new HashMap<Integer, Bitmap>();
        artistsTitle = getResources().getString(R.string.library_artists_title);
        yearLabel = getResources().getString(R.string.library_year_label);
        mutex = new Object();

        SharedPreferences settings = getSharedPreferences("settings", 0);
        currentLevel = settings.getInt("currentLevel", 0);
        currentArtist = settings.getInt("currentArtist", 0);
        currentAlbum = settings.getInt("currentAlbum", 0);

        libraryListView = (ListView)findViewById(R.id.artist_list);
        libraryTitle = (TextView)findViewById(R.id.artist_title);
        libraryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                switch (currentLevel) {
                    case ARTISTS:
                        currentLevel++;
                        currentArtist = artistAdapter.getItem(pos).getId();
                        show();
                        break;
                    case ALBUMS:
                        currentLevel++;
                        currentAlbum = albumAdapter.getItem(pos).getId();
                        show();
                        break;
                    case TRACKS:
                        playerService.addTrack(playerService.new Track(trackAdapter.getItem(pos).getId()));
                        break;
                }
            }
        });
        artistAdapter = new ArrayAdapter<Artist>(this, R.layout.library_artist_item, 0) {

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                ArtistPlaylist.ArtistViewHolder holder = null;
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.library_artist_item, null);
                    holder = new ArtistPlaylist.ArtistViewHolder();
                    holder.name = (TextView)v.findViewById(R.id.library_artist_name);
                    v.setTag(holder);
                } else {
                    holder = (ArtistPlaylist.ArtistViewHolder)v.getTag();
                }
                holder.name.setText(getItem(pos).getName());
                return v;
            }
        };
        albumAdapter = new ArrayAdapter<Album>(this, R.layout.library_album_item, 0) {

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                ArtistPlaylist.AlbumViewHolder holder = null;
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.library_album_item, null);
                    holder = new ArtistPlaylist.AlbumViewHolder();
                    holder.year = (TextView)v.findViewById(R.id.library_album_year);
                    holder.name = (TextView)v.findViewById(R.id.library_album_name);
                    holder.art = (ImageView)v.findViewById(R.id.library_album_art);
                    v.setTag(holder);
                } else {
                    holder = (ArtistPlaylist.AlbumViewHolder)v.getTag();
                }
                String year = getItem(pos).getYear();
                if (year != null) {
                    holder.year.setVisibility(View.VISIBLE);
                    holder.year.setText(yearLabel+getItem(pos).getYear());
                } else {
                    holder.year.setVisibility(View.GONE);
                }
                holder.name.setText(getItem(pos).getName());
                new Thread(new ArtistPlaylist.ArtLoader(pos, getItem(pos).getArt(), holder.art)).start();
                return v;
            }
        };
        trackAdapter = new ArrayAdapter<Track>(this, R.layout.library_track_item, 0) {

            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                View v = convertView;
                ArtistPlaylist.TrackViewHolder holder = null;
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    v = inflater.inflate(R.layout.library_track_item, null);
                    holder = new ArtistPlaylist.TrackViewHolder();
                    holder.name = (TextView)v.findViewById(R.id.library_track_name);
                    v.setTag(holder);
                } else {
                    holder = (ArtistPlaylist.TrackViewHolder)v.getTag();
                }
                holder.name.setText(getItem(pos).getNumber()+". "+getItem(pos).getName());
                return v;
            }
        };
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
        if (currentLevel > 0) {
            Cursor artistNameCursor = managedQuery(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ARTIST}, MediaStore.Audio.Media._ID+" == "+currentArtist+" ", null, null);
            if (artistNameCursor.moveToFirst()) {
                currentArtistName = artistNameCursor.getString(0);
            }
            if (currentLevel > 1) {
                Cursor albumCursor = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Albums.FIRST_YEAR}, MediaStore.Audio.Media._ID+" == "+currentAlbum+" ", null, null);
                if (albumCursor.moveToFirst()) {
                    currentAlbumName = albumCursor.getString(0);
                    String albumYear = albumCursor.getString(1);
                    if (albumYear != null) {
                        currentAlbumName = albumYear+" - "+currentAlbumName;
                    }
                }
            }
        }
        switch (currentLevel) {
            case ARTISTS:
                libraryTitle.setText(artistsTitle);
                showArtists();
                break;
            case ALBUMS:
                libraryTitle.setText(artistsTitle+" > "+currentArtistName);
                showAlbums();
                break;
            case TRACKS:
                libraryTitle.setText(artistsTitle+" > "+currentArtistName+" > "+currentAlbumName);
                showTracks();
                break;
        }
    }

    private void showArtists() {
        Cursor artistCursor = managedQuery(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST}, null, null, MediaStore.Audio.Media.ARTIST);
        artistAdapter.clear();
        while (artistCursor.moveToNext()) {
            artistAdapter.add(new Artist(Integer.parseInt(artistCursor.getString(0)), artistCursor.getString(1)));
        }
        libraryListView.setAdapter(artistAdapter);
    }

    private void showAlbums() {
        Cursor albumCursor = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.AlbumColumns.FIRST_YEAR, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_KEY}, MediaStore.Audio.Media.ARTIST_ID+" == "+currentArtist+" ", null, MediaStore.Audio.AlbumColumns.LAST_YEAR);
        albumArts.clear();
        albumAdapter.clear();
        while (albumCursor.moveToNext()) {
            albumAdapter.add(new Album(Integer.parseInt(albumCursor.getString(0)), albumCursor.getString(1), albumCursor.getString(2), albumCursor.getString(3)));
        }
        libraryListView.setAdapter(albumAdapter);
    }

    private void showTracks() {
        Cursor trackCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.TRACK}, MediaStore.Audio.AudioColumns.ALBUM_ID+" == "
                        +currentAlbum+" ", null, MediaStore.Audio.AudioColumns.TRACK);
        trackAdapter.clear();
        for (int n = 1; trackCursor.moveToNext(); n++) {
            trackAdapter.add(new Track(Integer.parseInt(trackCursor.getString(0)), trackCursor.getString(1), n));
        }
        libraryListView.setAdapter(trackAdapter);
    }

    private class ArtLoader implements Runnable {

        private int pos;
        private final String path;
        private ImageView artView;

        public ArtLoader(int pos, String path, ImageView artView) {
            this.pos = pos;
            this.path = path;
            this.artView = artView;
        }

        @Override
        public void run() {
            synchronized (mutex) {
                if (!albumArts.containsKey(pos)) {
                    if (path != null) {
                        BitmapScaler scaler = null;
                        try {
                            scaler = new BitmapScaler(new File(path), 80);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        BitmapScaler s = scaler;
                        if (s != null) {
                            albumArts.put(pos, s.getScaled());
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (albumArts.containsKey(pos)) {
                            artView.setImageBitmap(albumArts.get(pos));
                        } else {
                            artView.setImageResource(R.drawable.noart);
                        }
                    }
                });
                mutex.notify();
            }
        }
    }

    static private class ArtistViewHolder {
        TextView name;
    }

    static private class AlbumViewHolder {
        TextView year;
        TextView name;
        ImageView art;
    }

    static private class TrackViewHolder {
        TextView name;
    }



}
