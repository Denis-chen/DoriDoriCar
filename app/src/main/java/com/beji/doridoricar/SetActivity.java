package com.beji.doridoricar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SetActivity extends Activity {

    private String Piip = "";
    private int Piport = 0;
    private EditText connectip;
    private EditText connectport;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox autoConnect, remember;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        //ip, port 입력
        connectip = (EditText) findViewById(R.id.connectip);
        connectport = (EditText) findViewById(R.id.connectport);
        Button button = (Button) findViewById(R.id.connectbtn);
        autoConnect = (CheckBox) findViewById(R.id.autoConnect);
        remember = (CheckBox) findViewById(R.id.remember);

        pref = getSharedPreferences("settingInfo", 0);
        editor = pref.edit();

        if (pref.getBoolean("remember", false)) {
            connectip.setText(pref.getString("ip", ""));
            connectport.setText(Integer.toString(pref.getInt("port", 0)));
            remember.setChecked(true);
        }

        if (pref.getBoolean("autoConnect", false)) {
            Piip = pref.getString("ip", "");
            Piport = pref.getInt("port", 0);
            Intent pre = getIntent();
            pre.putExtra("ip", Piip);
            pre.putExtra("Piport", Piport);
            //RESULT_OK==-1, RESULT_FIRST_USEr==1, RESULT_CANCELED==0
            setResult(RESULT_OK, pre);
            //연결 확인 thread 시작
            ConnectThread my = new ConnectThread(Piip, Piport);
            my.execute();

        }


        //연결 확인 button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //입력 받은 ip, port번호를 부모 intent에 넘겨줌.
                Piip = connectip.getText().toString();
                Piport = Integer.parseInt(connectport.getText().toString());

                Intent pre = getIntent();
                pre.putExtra("ip", Piip);
                pre.putExtra("Piport", Piport);
                //RESULT_OK==-1, RESULT_FIRST_USEr==1, RESULT_CANCELED==0
                setResult(RESULT_OK, pre);

                //연결 확인 thread 시작
                ConnectThread my = new ConnectThread(Piip, Piport);
                my.execute();
            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String IP = connectip.getText().toString();
                    int PORT = Integer.parseInt(connectport.getText().toString());

                    editor.putString("ip", IP);
                    editor.putInt("port", PORT);
                    editor.putBoolean("remember", true);
                    editor.commit();

                } else if (pref.getBoolean("autoConnect", false)) {
                    editor.remove("ip");
                    editor.remove("port");
                    editor.remove("remember");
                    editor.commit();
                } else {
                    editor.remove("remember");
                    editor.commit();
                }
            }
        });

        autoConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String IP = connectip.getText().toString();
                    int PORT = Integer.parseInt(connectport.getText().toString());

                    editor.putString("ip", IP);
                    editor.putInt("port", PORT);
                    editor.putBoolean("autoConnect", true);
                    editor.commit();
                } else if (pref.getBoolean("remember", false)) {
                    editor.remove("ip");
                    editor.remove("port");
                    editor.remove("autoConnect");
                    editor.commit();
                } else {
                    editor.remove("autoConnect");
                    editor.commit();
                }
            }
        });

    }

    private void failed_connection() {
        editor.remove("autoConnect");
        editor.commit();
    }


    public void onBackPressed() {
        //finish();
        Intent pre = getIntent();
        //RESULT_OK==-1, RESULT_FIRST_USEr==1, RESULT_CANCELED==0
        setResult(RESULT_CANCELED, pre);
        super.onBackPressed();
    }

    public class ConnectThread extends AsyncTask<Void, Void, String> {
        String host;
        int port;
        StringBuffer strbuf = new StringBuffer(5);

        ConnectThread(String i, int r) {
            host = i;
            port = r;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            Socket socket = null;
            String result = "";
            try {
                socket = new Socket(host, port);

                byte[] str = new byte[5];
                int read = 0;
                BufferedOutputStream outstr = new BufferedOutputStream(
                        socket.getOutputStream());
                outstr.write("c".getBytes());
                outstr.flush();

                BufferedInputStream instr = new BufferedInputStream(socket.getInputStream());

                read = instr.read(str);
                result = new String(str, 0, read);

                instr.close();
                outstr.close();
                socket.close();


            } catch (UnknownHostException e) {
                Log.e("TCP", "unknown host");
                failed_connection();

            } catch (IOException e) {
                Log.e("TCP", "IO error");
                failed_connection();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            if (result.contains("good")) {
                Toast.makeText(SetActivity.this, "Connection Sucess!", Toast.LENGTH_LONG).show();
                autoConnect.setChecked(true);
            } else {
                Toast.makeText(SetActivity.this, "Connection failed!", Toast.LENGTH_LONG).show();
                failed_connection();
            }
        }
    }


}