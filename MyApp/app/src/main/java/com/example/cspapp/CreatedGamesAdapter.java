package com.example.cspapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        Button btnEdit;
        Button btnShare;

        GameViewHolder(View itemView) {
            super(itemView);
            tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
            tvGameType = new TextView(itemView.getContext());
            tvGameSpeed = new TextView(itemView.getContext());

            // Add these TextViews to the layout if they don't exist
            ViewGroup layout = itemView.findViewById(R.id.layoutGameDetails);
            if (layout != null) {
                layout.addView(tvGameType);
                layout.addView(tvGameSpeed);
            }

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}