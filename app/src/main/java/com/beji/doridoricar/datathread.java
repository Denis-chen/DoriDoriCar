package com.beji.doridoricar;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class datathread extends AsyncTask<Void, Void, Integer> {

    String host;
    int port;
    String data = null;
    int angleX = 90, angleY = 90;
    byte[] out = new byte[5];

    datathread(String host, int port, String data) {
        this.host = host;
        this.port = port;
        this.data = data;
    }

    int getAngleX() {
        return angleX;
    }

    int getAngleY() {
        return angleY;
    }

    @Override
    protected Integer doInBackground(Void... arg0) {
        Socket socket = null;
        while (isCancelled() != true) {
            try {
                socket = new Socket(host, port);
                System.out.println("client:" + host + ", " + port);

                switch (data.charAt(0)) {
                    case 'y':
                        angleY = Integer.parseInt(data.substring(2));
                        if (data.charAt(1) == '+') {
                            if (angleY >= 180)
                                angleY = 179;
                            angleY++;
                            data = data.substring(0, 2) + angleY;
                            out = (data.substring(0, 1) + angleY).getBytes();
                        } else if (data.charAt(1) == '-') {
                            if (angleY <= 0)
                                angleY = 1;
                            angleY--;
                            data = data.substring(0, 2) + angleY;
                            out = (data.substring(0, 1) + angleY).getBytes();
                        }
                        break;
                    case 'x':
                        angleX = Integer.parseInt(data.substring(2));
                        if (data.charAt(1) == '+') {
                            if (angleX >= 180)
                                angleX = 179;
                            angleX++;
                            data = data.substring(0, 2) + angleX;
                            out = (data.substring(0, 1) + angleX).getBytes();
                        } else if (data.charAt(1) == '-') {
                            if (angleX <= 0)
                                angleX = 1;
                            angleX--;
                            data = data.substring(0, 2) + angleX;
                            out = (data.substring(0, 1) + angleX).getBytes();
                        }
                        break;
                    default:
                        out = data.getBytes();
                        break;
                }

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
        } // while
        return null;
    }
}


