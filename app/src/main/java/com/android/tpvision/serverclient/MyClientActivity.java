package com.android.tpvision.serverclient;

import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.android.tpvision.java_websocket.client.WebSocketClient;
import com.android.tpvision.java_websocket.drafts.Draft;
import com.android.tpvision.java_websocket.drafts.Draft_10;
import com.android.tpvision.java_websocket.framing.Framedata;
import com.android.tpvision.java_websocket.handshake.ServerHandshake;

public class MyClientActivity extends ActionBarActivity {

    private static final String TAG = "SAMCLIENT";
    private ExampleClient mClient;
    private ListView mListView ;
    private Button mDiscoverButton;
    private Button mConnectButton;
    private NsdHelper mNsdHelper;
    private ArrayList<NsdServiceInfo> mNsdServers = new ArrayList<NsdServiceInfo>();
    private ArrayList<String> mNsdServiceNameList = new ArrayList<String>();
    private InetAddress mServiceAddress;
    private int mPort;
    private String mServiceName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sam_client);
        //mAddress = (EditText) findViewById(R.id.address);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
        mNsdHelper.discoverServices();


        //List to show the services advertising as NSD servers - start
        mListView = (ListView) findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                mServiceName = (String) mListView.getItemAtPosition(position);
                if(mServiceName.equals("SamWSServer")){
                    mServiceAddress = mNsdServers.get(position).getHost();
                    mPort = mNsdServers.get(position).getPort();
                    StartClientAync asynctask = new StartClientAync();
                    asynctask.execute();
                }
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mNsdServiceNameList);
        mListView.setAdapter(adapter);
        //List to show the services advertising as NSD servers - end


        //Discover button which on clicked should list the nsd services in listview -start
        mDiscoverButton = (Button) findViewById(R.id.discover_button);
        mDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mNsdServers = mNsdHelper.getChosenServiceList();
                for(NsdServiceInfo ns : mNsdServers) {
                    Log.i(TAG, "..service name : " + ns.getServiceName());
                    mNsdServiceNameList.add(ns.getServiceName());
                }
                adapter.notifyDataSetChanged();
            }
        });
        //Discover button which on clicked should list the nsd services in listview -end

//        mConnectButton = (Button) findViewById(R.id.connect_button);
//        mConnectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                //mNsdServers = mNsdHelper.getChosenServiceList();
//                /*Log.i(TAG, "..button connect button clicked....");
//                if (mAddress.getText() != null) {
//                    StartClientAync asynctask = new StartClientAync();
//                    asynctask.execute();
//                }
//                else {
//                    Toast.makeText(getApplicationContext(), "Enter server address", Toast.LENGTH_SHORT).show();
//                }*/
//
//                StartClientAync asynctask = new StartClientAync();
//                asynctask.execute();
//
//            }
//        });

    }


    private class StartClientAync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //mClient = new ExampleClient(new URI(/*"ws://192.168.20.52:8887"*/mAddress.getText().toString()), new Draft_10()); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
                //mClient.connect();

                //NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
                //mNsdServers = mNsdHelper.getChosenServiceList();
                //if (service != null) {
                  if (null != mServiceAddress && mPort >=0) {
                    Log.i(TAG, "Connecting. host : " + "ws:/"+mServiceAddress+":"+mPort);
                    mClient = new ExampleClient(new URI("ws:/"+mServiceAddress+":"+mPort), new Draft_10());
                    mClient.connect();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyClientActivity.this, "Connected to " + mServiceName, Toast.LENGTH_SHORT).show();
                        }
                    });
                  } else {
                        Log.i(TAG, "No service to connect to!");
                    }

            }catch (URISyntaxException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }



    public class ExampleClient extends WebSocketClient {

        public ExampleClient( URI serverUri , Draft draft ) {
            super( serverUri, draft );
        }

        public ExampleClient( URI serverURI ) {
            super( serverURI );
        }

        @Override
        public void onOpen( ServerHandshake handshakedata ) {
            Log.i(TAG, "opened connection");
            // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
        }

        @Override
        public void onMessage( final String message ) {
            Log.i(TAG, "received: " + message);
            MyClientActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MyClientActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFragment( Framedata fragment ) {
            Log.i(TAG, "received fragment: " + new String( fragment.getPayloadData().array() ) );
        }

        @Override
        public void onClose( int code, String reason, boolean remote ) {
            // The codecodes are documented in class org.java_websocket.framing.CloseFrame
            Log.i(TAG, "Connection closed by " + ( remote ? "remote peer" : "us" ) );
            Log.i(TAG, "Reason : " + reason + "    Code : " + code);
        }

        @Override
        public void onError( Exception ex ) {
            Log.i(TAG, "onError is called");
            ex.printStackTrace();
            // if the error is fatal then onClose will be called additionally
        }

    }
}
