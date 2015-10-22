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
public class SendCoffeeRequestTask extends AsyncTask<Integer,Void,Integer> {
    public static final String SERVERNAME = "api.xively.com"; //your computer IP address
    public static final int SERVERPORT = 8081;

    private long reqTime;
    private String feedId;
    private String apiKey;

    PrintWriter out;
    BufferedReader in;

    ISendCoffeeRequestListener service;

    public interface ISendCoffeeRequestListener {
        public void sendCoffeeRequestPreExecute();
        public void sendCoffeeRequestPostExecute(Integer result);
    }


    public SendCoffeeRequestTask(ISendCoffeeRequestListener service, String feedId, String apiKey, long reqtime) {
        this.service = service;
        this.feedId = feedId;
        this.apiKey = apiKey;
        this.reqTime = reqtime;
    }

    @Override
    protected void onPreExecute() {
        service.sendCoffeeRequestPreExecute();
    }

    @Override
    protected void onPostExecute(Integer result) {
        service.sendCoffeeRequestPostExecute(result);
    }

    private int checkErrorCode() {
        int res = -1;
        try {

            //send message.
            JSONObject object = new JSONObject();
            JSONObject param = new JSONObject();
            JSONObject headers = new JSONObject();

            try {
                param.put("datastreams", "resp_id,resp_code");

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
            while(checkStartTime + 20000L > System.currentTimeMillis()) {
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
                            case "resp_id":
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

                if(success) {
                    return responseCode;
                }
            }

        } catch (Exception e) {

            Log.e("TCP", "S: Error", e);

        }
        return res;
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

                //Iterator<Device> it = mainActivity.deviceList.iterator();

                //send message.
                JSONObject request = new JSONObject();
                JSONObject body = new JSONObject();
                JSONObject headers = new JSONObject();
                JSONArray datastreams = new JSONArray();

                try {
                    headers.put("X-ApiKey", apiKey);

                    request.put("method", "put");
                    request.put("resource", "/feeds/" + feedId);

                    //coffee
                    JSONObject coffee = new JSONObject();
                    coffee.put("id", "req_coffee");
                    coffee.put("current_value", params[0]);

                    //sugar
                    JSONObject sugar = new JSONObject();
                    sugar.put("id", "req_sugar");
                    sugar.put("current_value", params[1]);

                    //creamer
                    JSONObject creamer = new JSONObject();
                    creamer.put("id", "req_creamer");
                    creamer.put("current_value", params[2]);

                    //request_time
                    JSONObject requestTime = new JSONObject();
                    requestTime.put("id", "req_id");
                    requestTime.put("current_value", reqTime);

                    datastreams.put(coffee);
                    datastreams.put(sugar);
                    datastreams.put(creamer);
                    datastreams.put(requestTime);

                    body.put("version", "1.0.0");
                    body.put("datastreams", datastreams);

                    request.put("headers", headers);
                    request.put("body", body);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v("HERE", "Sending: " + request.toString());
                if (out != null && !out.checkError()) {
                    out.println(request.toString());
                    out.flush();
                }

                //retrieve response.
                JSONObject result = null;
                long lStartTime = System.currentTimeMillis();
                while (lStartTime + 5000L > System.currentTimeMillis()) {
                    String serverMessage = in.readLine();

                    if (serverMessage != null) {
                        Log.v("SENDREC", serverMessage);

                        //parse message
                        result = new JSONObject(serverMessage);
                        int status = result.getInt("status");

                        if(status == 200) {
                            //check if device updated
                            res = checkErrorCode();
                        }
                        break;
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
