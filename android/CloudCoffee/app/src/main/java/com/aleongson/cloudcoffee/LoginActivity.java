package com.aleongson.cloudcoffee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aleongson.cloudcoffee.http.RequestApiKeyAsyncTask;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
        implements RequestApiKeyAsyncTask.IRequestApiKeyListener{

    ProgressDialog progress;
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", 0);
        String feedid = mPrefs.getString("feedid", "");
        String apiKey = mPrefs.getString("apikey", "");

        if(!feedid.equals("")) {
            //means may laman
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }

        setContentView(R.layout.activity_login);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        final EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RequestApiKeyAsyncTask(LoginActivity.this).execute(txtUsername.getText().toString(),
                        txtPassword.getText().toString());
            }
        });
    }

    @Override
    public void requestApiKeyPreExecute() {
        progress = ProgressDialog.show(LoginActivity.this, "Please Wait...",
                "Logging in.");
    }

    @Override
    public void requestApiKeyPostExecute(String result) {
        progress.dismiss();
        //
        try {
            JSONObject root = new JSONObject(result);
            int error = root.getInt("error");

            if(error == 0) {
                //success
                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putString("feedid", root.getString("feedid"));
                mEditor.putString("apikey", root.getString("apikey"));
                mEditor.putString("username", root.getString("username"));
                mEditor.commit();

                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            } else {
                //toast.
                Toast.makeText(getApplicationContext(), "Invalid username/password.",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }
}
