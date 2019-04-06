package com.example.chatclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    TextView feed;
    TextView chatHistory;

    EditText msgText;
    Button msgSendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        feed = (TextView) findViewById(R.id.feed);
        chatHistory = (TextView) findViewById(R.id.chatHistory);

        msgText = findViewById(R.id.msgText);
        msgSendBtn = findViewById(R.id.msgSendBtn);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) //if wifi is not connected
        {
            feed.setText("Mobile Wi-Fi is OFF. Please start it, connect to Host Hotspot and restart the App");
        }
        else // if wifi is connected
        {
            final String hotspotIP = "192.168.43.1";
            final int port = 3399;

        //on clicking on send msg button
            msgSendBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String tMsg = msgText.getText().toString();

                    if(tMsg.equals(""))
                    {
                        tMsg = null;
                    }

                    MyClientTask myClientTask = new MyClientTask(hotspotIP, port, tMsg);
                    myClientTask.execute();
                }
            });
        }
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void>
    {
        String dstAddress;
        int dstPort;
        String msgToServer;

        String response = "";

        MyClientTask(String addr, int port, String msgTo)
        {
            dstAddress = addr;
            dstPort = port;
            msgToServer = msgTo;
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try
            {
                socket = new Socket(dstAddress, dstPort);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                if(msgToServer != null){
                    dataOutputStream.writeUTF(msgToServer);
                }

                response = dataInputStream.readUTF();

            } catch (UnknownHostException e)
            {
                e.printStackTrace();
                response = "Failed to connect to the client. You are connected to wrong WI-Fi or Host may not be active at the moment";
            } catch (IOException e)
            {
                e.printStackTrace();
                response = "IOException: " +  "Failed to connect to the client. You are connected to wrong WI-Fi or Host may not be active at the moment";;
            } finally
            {
                if (socket != null)
                {
                    try {
                        socket.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null)
                {
                    try
                    {
                        dataOutputStream.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            chatHistory.setText(response);
            super.onPostExecute(result);
        }
    }
}