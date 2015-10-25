package com.aleongson.cloudcoffeeadmin.async;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.aleongson.cloudcoffeeadmin.CoffeeMakerStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 12/10/2015.
 */
public class RetrieveCoffeeMakerStatusTask extends AsyncTask<Integer,Void,CoffeeMakerStatus> {
    public static final String SERVERNAME = "api.xively.com"; //your computer IP address
    public static final int SERVERPORT = 8081;

    private String feedId;
    private String apiKey;

    PrintWriter out;
    BufferedReader in;

    IRetrieveCoffeeMakerStatusListener service;

    public interface IRetrieveCoffeeMakerStatusListener {
        public void retrieveCoffeeMakerStatusPreExecute();
        public void retrieveCoffeeMakerStatusPostExecute(CoffeeMakerStatus result);
    }

    public RetrieveCoffeeMakerStatusTask(IRetrieveCoffeeMakerStatusListener service, String feedId, String apiKey) {
        this.service = service;
        this.feedId = feedId;
        this.apiKey = apiKey;
    }

    @Override
    protected void onPreExecute() {
        service.retrieveCoffeeMakerStatusPreExecute();
    }

    @Override
    protected void onPostExecute(CoffeeMakerStatus result) {
        service.retrieveCoffeeMakerStatusPostExecute(result);
    }

    @Override
    protected CoffeeMakerStatus doInBackground(Integer... params) {
        int res = -1;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERNAME);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);

            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //send message.
                JSONObject object = new JSONObject();
                //JSONObject param = new JSONObject();
                JSONObject headers = new JSONObject();

                try {
                    //param.put("datastreams", "coffee_tsp,creamer_tsp,sugar_tsp,water_cups");

                    headers.put("X-ApiKey", apiKey);

                    object.put("method", "get");
                    object.put("resource", "/feeds/" + feedId);

                    //object.put("params", param);
                    object.put("headers", headers);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                long checkStartTime = System.currentTimeMillis();

                boolean success = false;
                while(checkStartTime + 300000L > System.currentTimeMillis()) { // 5mins
                    SystemClock.sleep(2500);
                    Log.v("HERE", "Sending: " + object.toString());
                    if (out != null && !out.checkError()) {
                        out.println(object.toString());
                        out.flush();
                    }

                    //retrieve response.
                    JSONObject result = null;
                    boolean resultSuccess = false;
                    long lStartTime = System.currentTimeMillis();
                    while (lStartTime + 5000L > System.currentTimeMillis()) {
                        String serverMessage = in.readLine();

                        if (serverMessage != null) {
                            Log.v("HERE", serverMessage);

                            //parse message
                            result = new JSONObject(serverMessage);
                            int status = result.getInt("status");

                            if (status == 200)
                                resultSuccess = true;
                            break;
                        }
                    }

                    if (resultSuccess) {
                        JSONObject body = result.getJSONObject("body");
                        JSONArray datastreams = body.getJSONArray("datastreams");
                        CoffeeMakerStatus c = new CoffeeMakerStatus();

                        for(int i = 0; i < datastreams.length(); i++) {
                            JSONObject o = datastreams.getJSONObject(i);
                            switch(o.getString("id")) {
                                case "coffee_tsp":
                                    c.setCoffeeTsp(o.getInt("current_value"));
                                    break;
                                case "sugar_tsp":
                                    c.setSugarTsp(o.getInt("current_value"));
                                    break;
                                case "creamer_tsp":
                                    c.setCreamerTsp(o.getInt("current_value"));
                                    break;
                                case "water_cups":
                                    c.setWaterCup(o.getInt("current_value"));
                                    break;
                                case "error_code":
                                    c.setErrorCode(o.getInt("current_value"));
                                    break;
                                case "tray_0_owner":
                                    c.setTrayOwner(0, o.getString("current_value"));
                                    break;
                                case "tray_0_status":
                                    c.setTrayStatus(0, o.getInt("current_value"));
                                    break;
                                case "tray_1_owner":
                                    c.setTrayOwner(1, o.getString("current_value"));
                                    break;
                                case "tray_1_status":
                                    c.setTrayStatus(1, o.getInt("current_value"));
                                    break;
                                case "tray_2_owner":
                                    c.setTrayOwner(2, o.getString("current_value"));
                                    break;
                                case "tray_2_status":
                                    c.setTrayStatus(2, o.getInt("current_value"));
                                    break;
                            }
                        }

                        //Log.v("HERE", datastreams.toString());
                        return c;
                    }
                }

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

        return null;
    }
}
