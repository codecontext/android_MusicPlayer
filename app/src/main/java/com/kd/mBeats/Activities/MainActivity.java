package com.kd.mBeats.Activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kd.mBeats.Models.MusicFiles;
import com.kd.mBeats.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String LOG_TAG = "KD";

    public static final int REQUEST_CODE = 123;
    static ArrayList<MusicFiles> musicFiles;

    static boolean shuffleButtonState = false;
    static boolean repeatButtonState = false;

    static ArrayList<MusicFiles> albums = new ArrayList<>();

    private String MY_SORT_PREF = "SortOrder";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            musicFiles = getAllAudios(this);
            initViewPager();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(REQUEST_CODE == requestCode){
            if(PackageManager.PERMISSION_GRANTED == grantResults[0]){
                musicFiles = getAllAudios(this);
                initViewPager();
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }

    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ViewPagerAdapter  viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public ArrayList<MusicFiles> getAllAudios(Context context){

        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sortByName");
        String order = null;
        albums.clear();

        ArrayList<String> duplicate = new ArrayList<>();
        ArrayList<MusicFiles> audioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch (sortOrder){
            case "sortByName":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;

            case "sortByDate":
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;

            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA, //For PATH
                MediaStore.Audio.Media._ID

        };

        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, order);

        if(cursor != null){
            while (cursor.moveToNext()){
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String artist = cursor.getString(3);
                String path = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id);
                Log.v(LOG_TAG, "Path: " + path + "    album: "+album);
                audioList.add(musicFiles);

                /* Avoid adding the duplicate files to the same album list */
                if(!duplicate.contains(album)){
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        return audioList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem menuItem = menu.findItem(R.id.searchOption);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();

        for(MusicFiles song: musicFiles){
            if(song.getTitle().toLowerCase().contains(userInput)){
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateSongList(myFiles);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
        switch (item.getItemId()){
            case R.id.sortByName:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                break;

            case R.id.sortByDate:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                break;

            case R.id.sortBySize:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}