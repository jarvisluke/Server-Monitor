package com.mad.servermonitor;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

public class MonitoredServer extends Thread{
    public class ServerData {

        public double cpu;
        public double ram;
        public double[] cores;

        public ServerData(double cpu, double ram, double[] cores) {
            this.cpu = cpu;
            this.ram = ram;
            this.cores = cores;
        }

    }

    private static final String TAG = "MonitoredServer";
    private URL url;
    private URL safeUrl;
    private String JSONstring;
    private boolean looping;
    private boolean connectionSuccessful = true;

    public MonitoredServer(String ip, String port, String key) throws MalformedURLException {
        if (ip.isEmpty() || port.isEmpty() || key.isEmpty()) {
            throw new MalformedURLException();
        }
        this.url = new URL("http://" + ip + ":" + port + "/?key=" + key);
        this.safeUrl = new URL("http://" + ip + ":" + port);
        System.out.println("http://" + ip + ":" + port + "/?key=" + key);
        looping = true;
        Log.d(TAG, "about to start thread...");
        this.start();
    }

    public String getSafeURL() {
        return safeUrl.toString();
    }

    private String queryData() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        //StringBuffer content = new StringBuffer();
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    @Override
    public void run() {
        while (looping) {
            try {
                JSONstring = queryData();
                Thread.sleep(3000);
                Log.d(TAG, "looping");
            } catch (IOException | InterruptedException ignored) {}
        }
    }

    public ServerData getData() {
        if (JSONstring != null) {
            try {
                JSONObject jsonObject = new JSONObject(JSONstring);
                JSONArray cpucoresJSONarray = jsonObject.getJSONArray("cpucores");
                double[] coresarray = new double[cpucoresJSONarray.length()];
                for (int i = 0; i < cpucoresJSONarray.length(); i++) {coresarray[i] = cpucoresJSONarray.getDouble(i);}
                return new ServerData(
                        jsonObject.getDouble("cpu"),
                        jsonObject.getDouble("memory"),
                        coresarray
                );
            }
            catch (JSONException e) { return new ServerData(-1, -1, new double[] {-1}); }
        }
        return new ServerData(-1, -1, new double[] {-1});

    }

    public JSONObject getServerCredentialsAsJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip", url.getHost());
        jsonObject.put("ip", url.getHost());
        jsonObject.put("port", url.getPort());
        jsonObject.put("token", url.getQuery().replace("key=", ""));
        return jsonObject;
    }

    public boolean getConnectionSuccessful() {
        return this.connectionSuccessful;
    }

    public void stopThread() {
        looping = false;
    }

    public void startThread() {
        if (!looping) {
            looping = true;
            start();
        }
    }

}