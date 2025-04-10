package com.example.cspapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        Button btnDownload;

        SharedGameViewHolder(View itemView) {
            super(itemView);
            tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
            tvCreator = itemView.findViewById(R.id.tvCreator);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}