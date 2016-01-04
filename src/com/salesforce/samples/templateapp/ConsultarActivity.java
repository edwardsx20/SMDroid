package com.salesforce.samples.templateapp;

import android.os.Bundle;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

/**
 * Created by eaguad on 1/4/2016.
 */
public class ConsultarActivity extends SalesforceActivity {
    private RestClient client;


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.consultar_layout);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }
}
