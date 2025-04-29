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

public class CreatedGamesAdapter extends RecyclerView.Adapter<CreatedGamesAdapter.GameViewHolder> {

    private List<GameItem> gamesList;
    private OnGameClickListener listener;

    public interface OnGameClickListener {
        void onGameClick(GameItem game);
        void onShareClick(GameItem game);
    }

    public CreatedGamesAdapter(List<GameItem> gamesList, OnGameClickListener listener) {
        this.gamesList = gamesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_created_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem game = gamesList.get(position);
        holder.tvGameTitle.setText(game.getName());

        // Set game details
        holder.tvGameType.setText("Type: " + game.getType());
        holder.tvGameSpeed.setText("Speed: " + game.getSpeed());

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

        // Show music availability if music TextView exists
        if (holder.tvGameMusic != null) {
            if (game.hasMusic()) {
                holder.tvGameMusic.setVisibility(View.VISIBLE);
                holder.tvGameMusic.setText("Music: Available");
            } else {
                holder.tvGameMusic.setVisibility(View.GONE);
            }
        }

        // Set click listener for the play button
        holder.btnEdit.setText("Play"); // Repurpose the edit button
        holder.btnEdit.setOnClickListener(v -> listener.onGameClick(game));

        // Set click listener for the share button
        holder.btnShare.setOnClickListener(v -> listener.onShareClick(game));
    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvGameTitle;
        TextView tvGameType;
        TextView tvGameSpeed;
        TextView tvGameMusic; // Add reference to music TextView
        Button btnEdit;
        Button btnShare;
        ImageView ivGameImage;

        GameViewHolder(View itemView) {
            super(itemView);
            tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
            tvGameType = itemView.findViewById(R.id.tvGameType);
            tvGameSpeed = itemView.findViewById(R.id.tvGameSpeed);
            tvGameMusic = itemView.findViewById(R.id.tvGameMusic);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnShare = itemView.findViewById(R.id.btnShare);
            ivGameImage = itemView.findViewById(R.id.ivGameImage);
        }
    }
}