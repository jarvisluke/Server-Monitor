package com.mad.servermonitor;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<MonitoredServer> servers;
    private RecyclerView recyclerView;
    private ServerRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        servers = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewServers);
        adapter = new ServerRecyclerViewAdapter(servers, this::showServerDetailsDialog);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddServer = findViewById(R.id.btnAddServer);
        btnAddServer.setOnClickListener(v -> showServerInputDialog());
    }

    private void showServerInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.server_input_dialog, null);
        builder.setView(dialogView);

        EditText editTextIP = dialogView.findViewById(R.id.editTextIP);
        EditText editTextPort = dialogView.findViewById(R.id.editTextPort);
        EditText editTextConnectionKey = dialogView.findViewById(R.id.editTextConnectionKey);
        Button btnFetchServerDetails = dialogView.findViewById(R.id.btnFetchServerDetails);

        btnFetchServerDetails.setOnClickListener(v -> {
            String ip = editTextIP.getText().toString();
            String port = editTextPort.getText().toString();
            String connectionKey = editTextConnectionKey.getText().toString();

            try {
                MonitoredServer monitoredServer = new MonitoredServer(ip, port, connectionKey);
                int position = servers.size(); // Position of the new item
                servers.add(monitoredServer);
                adapter.notifyItemInserted(position); // Notify adapter about the new item
            } catch (MalformedURLException e) {
                Log.e("MainActivity", "MalformedURLException", e);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private Handler handler;
    private Runnable updateServerDataRunnable;

    private void showServerDetailsDialog(MonitoredServer server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.server_details_dialog, null);
        builder.setView(dialogView);

        TextView tvCPU = dialogView.findViewById(R.id.tvCPU);
        TextView tvCores = dialogView.findViewById(R.id.tvCores);
        TextView tvRAM = dialogView.findViewById(R.id.tvRAM);

        handler = new Handler();
        updateServerDataRunnable = new Runnable() {
            @Override
            public void run() {
                // Fetch server details
                MonitoredServer.ServerData data = server.getData();

                // Update dialog content with fetched data
                if (data != null) {
                    tvCPU.setText(getString(R.string.cpu_label, data.cpu));
                    tvCores.setText(getString(R.string.cores_label, data.cores.length));
                    tvRAM.setText(getString(R.string.memory_label, data.ram));
                } else {
                    tvCPU.setText(getString(R.string.cpu_na));
                    tvCores.setText(getString(R.string.cores_na));
                    tvRAM.setText(getString(R.string.memory_na));
                }

                // Schedule the next update after a delay
                handler.postDelayed(this, 1000); // Update every 1 seconds
            }
        };

        // Start periodic updates
        handler.post(updateServerDataRunnable);

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Stop periodic updates when dialog is dismissed
            handler.removeCallbacks(updateServerDataRunnable);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
