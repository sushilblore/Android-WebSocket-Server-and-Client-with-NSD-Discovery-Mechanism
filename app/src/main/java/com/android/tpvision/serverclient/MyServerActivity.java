package com.android.tpvision.serverclient;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import com.android.tpvision.java_websocket.WebSocket;
import com.android.tpvision.java_websocket.WebSocketImpl;
import com.android.tpvision.java_websocket.framing.Framedata;
import com.android.tpvision.java_websocket.handshake.ClientHandshake;
import com.android.tpvision.java_websocket.server.WebSocketServer;


public class MyServerActivity extends ActionBarActivity {

    private static final String TAG = "SAMSERVER";
    Button button;
    private ChatServer mWSServer;
    private NsdHelper mNsdHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sam_server);

        //Initializing Network Service Discovery
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        button = (Button) findViewById(R.id.send_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i(TAG, "..button clicked....");
                mWSServer.sendToAll("I am websocket server");
            }
        });

        StartServerAync asynctask = new StartServerAync();
        asynctask.execute();


    }


    private class StartServerAync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            WebSocketImpl.DEBUG = true;
            int port = 8887; // 843 flash policy port

            try {
                mWSServer = new ChatServer(port);
                mWSServer.start();
                Log.i(TAG, "ChatServer started on port: " + mWSServer.getPort());

                BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

            }catch (UnknownHostException e){

            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Register ws server for discovery
            AdvertiseServer();
        }
    }



    public class ChatServer extends WebSocketServer {

        public ChatServer(int port) throws UnknownHostException {
            super(new InetSocketAddress(port));
        }

        public ChatServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(final WebSocket conn, ClientHandshake handshake) {
            //this.sendToAll("new connection: " + handshake.getResourceDescriptor());
            Log.i(TAG, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected to server");
            MyServerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MyServerActivity.this, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected to server", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            this.sendToAll(conn + " has left the room!");
            Log.i(TAG, conn + " has left the room!");
        }

        @Override
        public void onMessage(WebSocket conn, final String message) {
            //this.sendToAll(message);
            Log.i(TAG, conn + ": " + message);
            MyServerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MyServerActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFragment(WebSocket conn, Framedata fragment) {
            Log.i(TAG, "received fragment: " + fragment);
        }


        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                // some errors like port binding failed may not be assignable to a specific websocket
            }
        }

        /**
         * Sends <var>text</var> to all currently connected WebSocket clients.
         *
         * @param text The String to send across the network.
         * @throws InterruptedException When socket related I/O errors occur.
         */
        public void sendToAll(String text) {
            Collection<WebSocket> con = connections();
            Log.i(TAG, "No of active connections : " + con.size());
            synchronized (con) {
                for (WebSocket c : con) {
                    c.send(text);
                }
            }
        }
    }

    //Discovery Related code - start
    public void AdvertiseServer() {
        Log.i(TAG, "...starting to Advertise Sever...");
        // Register server
        if(mWSServer.getPort() > -1) {
            Log.i(TAG, "..Server port being advertised : " + mWSServer.getPort());
            mNsdHelper.registerService(mWSServer.getPort());
        } else {
            Log.i(TAG, "WebServerSocket isn't bound.");
        }
    }
    //Discovery Related code - end
}
