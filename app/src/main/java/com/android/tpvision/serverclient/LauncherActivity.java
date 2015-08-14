package com.android.tpvision.serverclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class LauncherActivity extends Activity {

    private Button mServerButton;
    private Button mClientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mServerButton = (Button) findViewById(R.id.server_button);
        mServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(LauncherActivity.this, MyServerActivity.class);
                startActivity(intent);
            }
        });

        mClientButton = (Button) findViewById(R.id.client_button);
        mClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(LauncherActivity.this, MyClientActivity.class);
                startActivity(intent);
            }
        });
    }


}
