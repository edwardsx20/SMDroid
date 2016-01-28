package com.salesforce.samples.templateapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cl.edicsm.control.Controller;

/**
 * Created by eaguad on 1/4/2016.
 */
public class ConsultarActivity extends SalesforceActivity {
    private RestClient client;
    private ProgressDialog mProgressDialog;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.consultar_layout);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }

    public void clearConsultaMuestras(View v) {
        ViewGroup root = (ViewGroup) findViewById(R.id.consultar_root);
        Integer childcount = root.getChildCount();
        for (int i = 0; i < childcount; i++) {
            View vw = findViewById(R.id.consultar_vgroup);
            if (vw != null) {
                root.removeView(vw);
            }
        }
    }

    public void consultarMuestra(View v) throws ExecutionException, InterruptedException {
        EditText rbd = (EditText) findViewById(R.id.consultar_rbd);
        String strRbd = rbd.getText().toString();

        if (strRbd == "") {
            Toast.makeText(this, "Por favor, ingrese RBD.", Toast.LENGTH_SHORT).show();

            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Conectando con Salesforce...");
        mProgressDialog.show();


        clearConsultaMuestras(null);

        if (strRbd != "") {
            Controller control = new Controller(this, client, mProgressDialog);

            control.consultaMuestra(strRbd);
        }
    }
}

