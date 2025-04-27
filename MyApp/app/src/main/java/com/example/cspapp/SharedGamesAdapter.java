package com.example.cspapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class SharedGamesAdapter extends RecyclerView.Adapter<SharedGamesAdapter.SharedGameViewHolder> {

    private List<SharedGameItem> gamesList;
    private OnSharedGameClickListener listener;

    public interface OnSharedGameClickListener {
        void onPlayClick(SharedGameItem game);
    }

    public SharedGamesAdapter(List<SharedGameItem> gamesList, OnSharedGameClickListener listener) {
        this.gamesList = gamesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SharedGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shared_game, parent, false);
        return new SharedGameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedGameViewHolder holder, int position) {
        SharedGameItem game = gamesList.get(position);
        holder.tvGameTitle.setText(game.getName());
        holder.tvCreator.setText("By " + game.getCreatorName());

        // Display game details
        if (holder.tvGameType != null) {
            holder.tvGameType.setText("Type: " + game.getType());
        }
        if (holder.tvGameSpeed != null) {
            holder.tvGameSpeed.setText("Speed: " + game.getSpeed());
        }

        // Load and display the game image if available
        if (game.hasImage() && holder.ivGameImage != null) {
            holder.ivGameImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(game.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image))
                    .into(holder.ivGameImage);
        } else if (holder.ivGameImage != null) {
            holder.ivGameImage.setVisibility(View.GONE);
        }

        holder.btnDownload.setText("Play Game");
        holder.btnDownload.setOnClickListener(v -> listener.onPlayClick(game));
    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }

    static class SharedGameViewHolder extends RecyclerView.ViewHolder {
        TextView tvGameTitle;
        TextView tvCreator;
        TextView tvGameType;
        TextView tvGameSpeed;
        ImageView ivGameImage;
        Button btnDownload;

        SharedGameViewHolder(View itemView) {
            super(itemView);
            tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
            tvCreator = itemView.findViewById(R.id.tvCreator);
            tvGameType = itemView.findViewById(R.id.tvGameType);
            tvGameSpeed = itemView.findViewById(R.id.tvGameSpeed);
            ivGameImage = itemView.findViewById(R.id.ivGameImage);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}