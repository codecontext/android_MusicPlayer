package com.kd.mBeats.Activities;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kd.mBeats.Adapters.AlbumDetailsAdapter;
import com.kd.mBeats.Models.MusicFiles;
import com.kd.mBeats.R;

import java.util.ArrayList;

import static com.kd.mBeats.Activities.MainActivity.musicFiles;

public class AlbumDetailsActivity extends AppCompatActivity {

    RecyclerView fileList;
    ImageView albumPic;
    TextView albumTitleText;
    TextView songDurationTotal;

    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        fileList = findViewById(R.id.fileList);
        albumPic = findViewById(R.id.albumPic);
        albumTitleText = findViewById(R.id.albumTitleText); /// For Album Title headline
        albumName = getIntent().getStringExtra("albumName");

        albumTitleText.setText(albumName);

        int index = 0;

        /* Scan through all the files */
        for(int currentFile = 0 ; currentFile < musicFiles.size() ; currentFile++){

            /* Check if the current file album name matches to the scanned music file */
            if(albumName.equals(musicFiles.get(currentFile).getAlbum())){
                albumSongs.add(index, musicFiles.get(currentFile));
                index++;
            }
        }

        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        if(image != null){
            Glide.with(this)
                    .load(image)
                    .into(albumPic);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_music)
                    .into(albumPic);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setFullScreen();

        if(!(albumSongs.size() < 1)){
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            fileList.setAdapter(albumDetailsAdapter);
            fileList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] art = {0};

        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return art;
    }

    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        /*  Hide both the navigation bar and the status bar.
            SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            a general rule, you should design your app to hide the status bar whenever you
            hide the navigation bar. */
        final int flagsHide = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(flagsHide);
    }
}