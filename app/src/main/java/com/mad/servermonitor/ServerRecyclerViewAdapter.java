package com.mad.servermonitor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServerRecyclerViewAdapter extends RecyclerView.Adapter<ServerRecyclerViewAdapter.ServerViewHolder> {
    private final List<MonitoredServer> servers; // List of MonitoredServer objects
    private final OnServerClickListener listener; // Click listener interface

    // Constructor to initialize the adapter with a list of MonitoredServer objects and click listeners
    public ServerRecyclerViewAdapter(List<MonitoredServer> servers, OnServerClickListener listener) {
        this.servers = servers;
        this.listener = listener;
    }

    // sets up the initial view for each item in the RecyclerView
    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_item, parent, false);
        return new ServerViewHolder(view);
    }

    // binds data from MonitoredServer object to views within the ServerViewHolder
    // ensures the RecyclerView displays the correct data for each item at the specified position
    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        MonitoredServer server = servers.get(position);
        holder.bind(server);
    }

    // provides total number of items that the RecyclerView will display.
    @Override
    public int getItemCount() {
        return servers != null ? servers.size() : 0;
    }

    // Defines an interface for handling click events on server items in the RecyclerView.
    public interface OnServerClickListener {
        void onServerClick(MonitoredServer server);
    }

    // ViewHolder class to hold the views for each server item.
    public class ServerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView displayName; // TextView to display server name
        private MonitoredServer server; // MonitoredServer object associated with this view holder

        // initializes the displayName TextView and sets up a click listener for each item
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.displayName);
            itemView.setOnClickListener(this);
        }

        public void bind(MonitoredServer server) {
            this.server = server;
            displayName.setText(server.getSafeURL()); // Update to show server URL without connection key
        }

        // Notifies listener when a server item is clicked, passing along the clicked server's information
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onServerClick(server);
            }
        }

    }
}