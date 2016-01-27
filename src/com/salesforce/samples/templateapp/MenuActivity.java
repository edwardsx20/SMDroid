package com.salesforce.samples.templateapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

/**
 * Created by eaguad on 1/4/2016.
 */
public class MenuActivity extends SalesforceActivity {
    private RestClient client;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu_layout);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
    }

    public void onBtnInsertar(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onBtnConsultar(View v) {
        Intent intent = new Intent(this, ConsultarActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }
}
