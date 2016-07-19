package com.beji.doridoricar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SetActivity extends Activity {

    private String Piip = null;
    private int Piport = 0;
    private EditText connectip;
    private EditText connectport;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set);

        //ip, port 입력
        connectip = (EditText) findViewById(R.id.connectip);
        connectport = (EditText) findViewById(R.id.connectport);
        Button button = (Button) findViewById(R.id.leftbtn);

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
                setResult(1, pre);

                //연결 확인 thread 시작
                ConnectThread my = new ConnectThread(Piip, Piport);
                my.execute();
            }
        });


    }

    public void onBackPressed() {
        finish();
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
            String result = null;
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

            } catch (IOException e) {
                Log.e("TCP", "IO error");
            }
            return result;
        }

        protected void onPostExecute(String result) {
            if (result.contains("good"))
                Toast.makeText(SetActivity.this, "Connection Sucess!", Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(SetActivity.this, "Connection failed!", Toast.LENGTH_LONG).show();
            }
        }
    }


}