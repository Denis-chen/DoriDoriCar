package com.beji.doridoricar;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class datathread2 extends AsyncTask<Void, Void, Void> {

    private String host;
    private int port;
    private String data = null;


    datathread2(String host, int port, String data) {
        this.host = host;
        this.port = port;
        this.data = data;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            System.out.println("client:" + host + ", " + port);
            byte[] out = new byte[5];
            out = data.getBytes();

            BufferedOutputStream outstr = new BufferedOutputStream(socket.getOutputStream());
            outstr.write(out);

            outstr.flush();

            socket.close();
            outstr.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


}


