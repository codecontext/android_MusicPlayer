package com.kd.mBeats.Activities;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
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
}