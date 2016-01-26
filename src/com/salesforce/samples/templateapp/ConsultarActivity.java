package com.salesforce.samples.templateapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import cl.edicsm.control.Controller;

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

    public void consultarMuestra(View v) {
        EditText rbd = (EditText) findViewById(R.id.consultar_rbd);
        String strRbd = rbd.getText().toString();
        HashMap<String, String> result = null;

        if (strRbd != "") {
            Controller control = new Controller(this, client, new ProgressDialog(this));
            try {
                result = control.consultaMuestra(strRbd);
                Log.d("Consulta", "Success");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
