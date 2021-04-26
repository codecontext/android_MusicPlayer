package com.kd.mBeats;

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

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context context, ArrayList<MusicFiles>files) {
        this.mFiles = files;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if(image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.albumArt);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.ic_music)
                    .into(holder.albumArt);
        }

        holder.songTitle.setText(mFiles.get(position).getTitle());
        holder.albumName.setText(mFiles.get(position).getAlbum());
        holder.songDuration.setText(mFiles.get(position).getDuration());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView albumArt;
        TextView songTitle;
        TextView albumName;
        TextView songDuration;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.albumArt);
            songTitle = itemView.findViewById(R.id.songTitle);
            albumName = itemView.findViewById(R.id.albumName);
            songDuration = itemView.findViewById(R.id.songDuration);
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
