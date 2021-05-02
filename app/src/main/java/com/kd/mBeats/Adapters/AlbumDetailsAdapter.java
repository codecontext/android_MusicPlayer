package com.kd.mBeats.Adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kd.mBeats.Activities.PlayerActivity;
import com.kd.mBeats.Models.MusicFiles;
import com.kd.mBeats.R;

import java.util.ArrayList;

import static com.kd.mBeats.Activities.PlayerActivity.milliSecondsToTimer;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.AlbumViewHolder> {

    private Context mContext;
    public static ArrayList<MusicFiles> albumFiles;
    View view;

    public AlbumDetailsAdapter(Context context, ArrayList<MusicFiles> albumFiles) {
        this.mContext = context;
        this.albumFiles = albumFiles;
    }


    @NonNull
    @Override
    public AlbumDetailsAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailsAdapter.AlbumViewHolder holder, int position) {
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.albumImage);
        } else {
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.ic_music)
                    .into(holder.albumImage);
        }

        holder.songTitle.setText(albumFiles.get(position).getTitle());

        int totalSongDuration = Integer.parseInt(albumFiles.get(position).getDuration()) / 1000;
        holder.songDurationTotal.setText(milliSecondsToTimer(totalSongDuration));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder{
        ImageView albumImage;
        TextView songTitle;
        TextView songDurationTotal;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            albumImage = itemView.findViewById(R.id.albumArt);
            songTitle = itemView.findViewById(R.id.songTitle);
            songDurationTotal = itemView.findViewById(R.id.songDuration);
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
