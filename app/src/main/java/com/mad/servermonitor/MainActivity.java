package com.mad.servermonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

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

        // initialize a list of servers
        servers = new ArrayList<>();

        // Restore stored servers
        SharedPreferences sharedPreferences = getSharedPreferences("MonitoredServers", MODE_PRIVATE);
        for (int i = 0; i < sharedPreferences.getAll().size(); i++) {
            String serverDetails = sharedPreferences.getString("server_" + i, "");
            if (!serverDetails.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(serverDetails);
                    String ip = jsonObject.getString("ip");
                    int port = jsonObject.getInt("port");
                    String token = jsonObject.getString("token");
                    MonitoredServer monitoredServer = new MonitoredServer(ip, String.valueOf(port), token);
                    servers.add(monitoredServer);
                } catch (JSONException | MalformedURLException e) {
                    Log.e("MainActivity", "Exception", e);
                }
            }
        }

        // Setup RecyclerView to display servers
        recyclerView = findViewById(R.id.recyclerViewServers);
        adapter = new ServerRecyclerViewAdapter(servers, this::showServerDetailsDialog);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup "Add Server" button click listener
        Button btnAddServer = findViewById(R.id.btnAddServer);
        btnAddServer.setOnClickListener(v -> showServerInputDialog());
    }

    // Method to display dialog for adding a new server
    private void showServerInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.server_input_dialog, null);
        builder.setView(dialogView);

        EditText editTextIP = dialogView.findViewById(R.id.editTextIP);
        EditText editTextPort = dialogView.findViewById(R.id.editTextPort);
        EditText editTextConnectionKey = dialogView.findViewById(R.id.editTextConnectionKey);
        Button btnFetchServerDetails = dialogView.findViewById(R.id.btnFetchServerDetails);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Define behavior when "Fetch Server Details" button is clicked
        btnFetchServerDetails.setOnClickListener(v -> {
            String ip = editTextIP.getText().toString();
            String port = editTextPort.getText().toString();
            String connectionKey = editTextConnectionKey.getText().toString();

            try {
                MonitoredServer monitoredServer = new MonitoredServer(ip, port, connectionKey);

                // Add the new server to the list and notify the adapter
                int position = servers.size(); // Position of the new server
                servers.add(monitoredServer);
                adapter.notifyItemInserted(position); // Notify adapter about the new server

                // Store server details
                SharedPreferences sharedPreferences = getSharedPreferences("MonitoredServers", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("server_" + position, monitoredServer.getServerCredentialsAsJSON().toString());
                editor.apply();

                // Display success message
                Snackbar snackbar = Snackbar.make(recyclerView, R.string.btnFetchSuccess, Snackbar.LENGTH_LONG);
                snackbar.show();
            } catch (MalformedURLException | JSONException e) {
                // Display failure message
                Snackbar snackbar = Snackbar.make(recyclerView, R.string.btnFetchFailure, Snackbar.LENGTH_LONG);
                snackbar.show();
                Log.e("MainActivity", "Exception", e);
            }

            // Dismiss dialog
            dialog.dismiss();
        });
    }
    private Handler handler;
    private Runnable updateServerDataRunnable;

    // Method to display dialog showing details of a monitored server
    private void showServerDetailsDialog(MonitoredServer server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.server_details_dialog, null);
        builder.setView(dialogView);

        TextView tvCPU = dialogView.findViewById(R.id.tvCPU);
        TextView tvCores = dialogView.findViewById(R.id.tvCores);
        TextView tvRAM = dialogView.findViewById(R.id.tvRAM);

        // Define a handler and a runnable to periodically update server data
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
                    // If data is not available, display "N/A" for each field
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
