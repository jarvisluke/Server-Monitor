package com.mad.servermonitor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServerRecyclerViewAdapter extends RecyclerView.Adapter<ServerRecyclerViewAdapter.ServerViewHolder> {
    private final List<MonitoredServer> servers;
    private final OnServerClickListener listener;

    public ServerRecyclerViewAdapter(List<MonitoredServer> servers, OnServerClickListener listener) {
        this.servers = servers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_item, parent, false);
        return new ServerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        MonitoredServer server = servers.get(position);
        holder.bind(server);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    public interface OnServerClickListener {
        void onServerClick(MonitoredServer server);
    }

    public class ServerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView displayName;
        private MonitoredServer server;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.displayName);
            itemView.setOnClickListener(this);
        }

        public void bind(MonitoredServer server) {
            this.server = server;
            displayName.setText(server.getSafeURL()); // Update to show server URL without connection key
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onServerClick(server);
            }
        }

    }
}