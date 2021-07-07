package com.example.senddata;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    EditText e1, e2;
    TextView myIp , disMsg;

    public static String MY_IP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        e1 = findViewById(R.id.txtIp);
        e2 = findViewById(R.id.txtMsg);
        myIp = findViewById(R.id.myIp);
        disMsg = findViewById(R.id.disMsg);

        try {
            MY_IP = getLocalIpAddress();
            myIp.setText("My IP: " +MY_IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Thread myThread = new Thread(new MyServer());
        myThread.start();
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    class MyServer implements Runnable{

        ServerSocket ss;
        Socket mysocket;
        DataInputStream dis;
        String message;
        Handler handler = new Handler();

        @Override
        public void run() {

            try {
                ss = new ServerSocket(9700);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Waiting for client", Toast.LENGTH_SHORT).show();
                    }
                });
                while (true)
                {
                    mysocket = ss.accept();
                    dis = new DataInputStream(mysocket.getInputStream());

                    message = dis.readUTF();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            disMsg.setText(message);
                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void button_click(View v){

        BackgroundTask b = new BackgroundTask();
        b.execute(e1.getText().toString(), e2.getText().toString());

    }

    class BackgroundTask extends AsyncTask<String, Void, String> {
        Socket s;
        DataOutputStream dos;
        String ip, message;
        @Override
        protected String doInBackground(String... params) {
            ip = params[0];
            message = params[1];
            try {
                s =new Socket(ip, 9700);
                dos = new DataOutputStream(s.getOutputStream());
                dos.writeUTF(message);

                dos.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}