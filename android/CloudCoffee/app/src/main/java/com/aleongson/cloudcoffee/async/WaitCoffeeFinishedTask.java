package com.aleongson.cloudcoffee.async;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

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
public class WaitCoffeeFinishedTask extends AsyncTask<Integer,Void,Integer> {
    public static final String SERVERNAME = "api.xively.com"; //your computer IP address
    public static final int SERVERPORT = 8081;

    private long reqTime;
    private String feedId;
    private String apiKey;

    PrintWriter out;
    BufferedReader in;

    IWaitCoffeeFinishedListener service;

    public interface IWaitCoffeeFinishedListener {
        public void waitCoffeeFinishedPreExecute();
        public void waitCoffeeFinishedPostExecute(Integer result);
    }


    public WaitCoffeeFinishedTask(IWaitCoffeeFinishedListener service, String feedId, String apiKey, long reqtime) {
        this.service = service;
        this.feedId = feedId;
        this.apiKey = apiKey;
        this.reqTime = reqtime;
    }

    @Override
    protected void onPreExecute() {
        service.waitCoffeeFinishedPreExecute();
    }

    @Override
    protected void onPostExecute(Integer result) {
        service.waitCoffeeFinishedPostExecute(result);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
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
                JSONObject param = new JSONObject();
                JSONObject headers = new JSONObject();

                try {
                    param.put("datastreams", "resp_finished,resp_code");

                    headers.put("X-ApiKey", apiKey);

                    object.put("method", "get");
                    object.put("resource", "/feeds/" + feedId);

                    object.put("params", param);
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

                    int responseCode = -1;
                    if (resultSuccess) {
                        JSONObject body = result.getJSONObject("body");
                        JSONArray datastreams = body.getJSONArray("datastreams");
                        for(int i = 0; i < 2; i++) {
                            JSONObject o = datastreams.getJSONObject(i);
                            switch(o.getString("id")) {
                                case "resp_finished":
                                    if (Long.toString(reqTime).equals(o.getString("current_value"))) {
                                        success = true;
                                    }
                                    break;
                                case "resp_code":
                                    responseCode = o.getInt("current_value");
                                    break;
                            }
                        }
                    }

                    if(success && responseCode >= 0) {
                        return responseCode;
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

        return res;
    }
}
