package com.aleongson.cloudcoffee.http;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 19/10/2015.
 */
public class RequestApiKeyAsyncTask extends AsyncTask <String, Void, String> {
    IRequestApiKeyListener activity;

    public interface IRequestApiKeyListener {
        void requestApiKeyPreExecute();
        void requestApiKeyPostExecute(String result);
    }

    public RequestApiKeyAsyncTask(IRequestApiKeyListener activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        activity.requestApiKeyPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        activity.requestApiKeyPostExecute(result);
        Log.v("ADDRESS", "END");
    }

    @Override
    protected String doInBackground(String... params) {
        String returnMsg = "";
        String writeOut = "";
        String decodedString = "";

        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL("http://cloudcoffee.comli.com/?username=" + params[0] + "&password=" + params[1]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            //connection.setRequestMethod("GET");

            final OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(writeOut);
            osw.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((decodedString = in.readLine()) != null) {
                returnMsg+=decodedString;
            }

            in.close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            returnMsg = "{\"error\":1}";
        }

        return returnMsg;
    }
}
