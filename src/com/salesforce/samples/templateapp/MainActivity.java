/*
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.samples.templateapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cl.edicsm.control.ViewDetail;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {
    private RestClient client;
    private JSONArray sfResult;
    ArrayList<ViewDetail> myViews = new ArrayList<>();

    private String scanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.i("EVENT", "onCreate");

        // Setup view
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            myViews = savedInstanceState.getParcelableArrayList("myViews");

            if (myViews != null) {

                for (ViewDetail vd : myViews) {
                    Log.i("SAVEDSTATE", "Se agrega producto.");
                    agregarProducto(vd.getValue(), vd.getCantidad());
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        myViews = new ArrayList<>();

        Log.i("EVENT", "onSaveInstanceState");

        try {
            for (int i = 0; i < ((ViewGroup) findViewById(R.id.root)).getChildCount(); i++) {
                ViewGroup parentVg = (ViewGroup) ((ViewGroup) findViewById(R.id.root)).getChildAt(i);

                if (parentVg.getChildCount() == 2 && parentVg.getId() == R.id.vgroup) {
                    TextView tView = (TextView) parentVg.getChildAt(0);
                    EditText eText = (EditText) parentVg.getChildAt(1);

                    myViews.add(new ViewDetail(tView.getText().toString(), eText.getText().toString()));
                }
            }

            savedState.putParcelableArrayList("myViews", myViews);

        } catch (NullPointerException e) {
            Log.e("Error", e.toString());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        //get the views back...
        myViews = savedState.getParcelableArrayList("myViews");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("EVENT", "onPause");
    }

    @Override
    public void onResume() {
        // Hide everything until we are logged in
        findViewById(R.id.root).setVisibility(View.INVISIBLE);

        Log.i("EVENT", "onResume normal");
        super.onResume();
    }

    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;

        Log.i("EVENT", "onResume RestClient");
        // Show everything
        findViewById(R.id.root).setVisibility(View.VISIBLE);
    }

    /**
     * Cierra sesion.
     *
     * @param v
     */
    public void onLogoutClick(View v) {
        SalesforceSDKManager.getInstance().logout(this);
    }

    /**
     * Limpia el layout de registros.
     *
     * @param v
     */
    public void onClearClick(View v) {
        ViewGroup root = (ViewGroup) findViewById(R.id.root);
        for (int i = 0; i < root.getChildCount(); i++) {
            View vw = findViewById(R.id.vgroup);
            if (vw != null) {
                root.removeView(vw);
            }
        }
    }

    // Obtiene resultado de codigo de barra
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                makeToast(this, "Cancelado");
            } else {
                Log.d("MainActivity", "Scanned");
                makeToast(this, "ISBN: " + result.getContents());
                scanResult = result.getContents();

                if (scanResult != null) {
                    try {
                        getProducto(scanResult);
                    } catch (Exception e) {
                        Log.e("RequestError", e.toString());
                        makeToast(this, e.toString());
                    } finally {
                        sfResult = null;
                    }
                }
            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Crea toast
    private void makeToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Inicia lector de codigos de barra
     *
     * @param v
     * @throws UnsupportedEncodingException
     */
    public void onGetProduct2Click(View v) throws UnsupportedEncodingException, JSONException {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Escanea un codigo");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    // Agrega producto al layout
    private void agregarProducto(String msg, String value) {
        // Obtiene el grupo
        LinearLayout parentLayout = (LinearLayout) findViewById(R.id.root);

        // Crea el inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        // Obtiene el layout a insertar
        view = layoutInflater.inflate(R.layout.text_layout, parentLayout, false);

        // Obtiene views
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.vgroup);
        EditText et = (EditText) linearLayout.findViewById(R.id.editText);
        TextView tv = (TextView) linearLayout.findViewById(R.id.textView);

        tv.setText(msg);
        if (value != "0") {
            et.setText(value);
        }

        parentLayout.addView(linearLayout);
    }

    // Crea registro
    public void createProducto(View v) throws IOException {
        AsyncRequestCallback asyncCallBack = new AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, RestResponse response) {
                    Log.i("APITest", "Success");
                }

                @Override
                public void onError(Exception exception) {
                    VolleyError volleyError = (VolleyError) exception;
                    NetworkResponse response = volleyError.networkResponse;
                    String json = new String(response.data);
                    Log.e("RestError", exception.toString());
                    Log.e("RestError", json);
                    Toast.makeText(MainActivity.this,
                            MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                            Toast.LENGTH_LONG).show();
                }
        };

        try {
            Map sObject;
            ArrayList<Map> objetos = new ArrayList<>();

            for (int i = 0; i < ((ViewGroup) findViewById(R.id.root)).getChildCount(); i++) {

                ViewGroup parentVg = (ViewGroup) ((ViewGroup) findViewById(R.id.root)).getChildAt(i);
                sObject = new HashMap<>();

                if (parentVg.getChildCount() == 2 && parentVg.getId() == R.id.vgroup) {
                    TextView tView = (TextView) parentVg.getChildAt(0);
                    EditText eText = (EditText) parentVg.getChildAt(1);

                    String productCode = tView.getText().toString().split(" ")[0];
                    String name = tView.getText().toString().split("-")[1];
                    String Id = getId("Product2", "SELECT Id FROM Product2 WHERE ProductCode = '" + productCode + "'");
                    if (Id != null) {
                        sObject.put("Producto__c", Id);
                        sObject.put("Name", name);
                        sObject.put("Cuenta__c", "001J000001gmdjp");
                        sObject.put("Presupuesto_Muestras__c", "a0JJ000000A9j1cMAB");
                        sObject.put("Cantidad_entregada__c", eText.getText().toString());

                        objetos.add(sObject);
                    } else {
                        Log.d("QueryId", "Failed");
                    }
                }
            }


            for (Map sobj : objetos) {
                // Objeto request
                RestRequest restRequest = RestRequest.getRequestForCreate(getString(R.string.api_version), "MuestrasLineItem__c", sobj);

                // Ejecuta Async
                client.sendAsync(restRequest, asyncCallBack);
            }

        } catch (Exception e) {
            Log.e("APITest", e.toString());
        }
    }

    private String getId(String sObject, String soql) {
        String result = null;
        try {
            RestRequest restRequest = RestRequest.getRequestForQuery(getString(R.string.api_version), soql);
            RestResponse response = client.sendSync(restRequest);

            sfResult = response.asJSONObject().getJSONArray("records");
            result = sfResult.getJSONObject(0).getString("Id");
        } catch (Exception e) {
            Log.e("GETID", e.toString());
        }
        return result;
    }

    // Obtiene producto y agrega a la lista
    private void getProducto(String isbn) throws UnsupportedEncodingException {
        String soql = "SELECT ProductCode, Name FROM Product2 WHERE ISBN__c = " + isbn + ".0";
        RestRequest restRequest = RestRequest.getRequestForQuery(getString(R.string.api_version), soql);

        client.sendAsync(restRequest, new AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, RestResponse result) {
                try {
                    sfResult = result.asJSONObject().getJSONArray("records");

                    Log.d("RequestResponse", String.valueOf((sfResult != null)));
                    if (sfResult != null) {
                        if (sfResult.length() > 0) {
                            String lineStr;
                            for (int i = 0; i < sfResult.length(); i++) {
                                lineStr = sfResult.getJSONObject(i).getString("ProductCode") + " - " + sfResult.getJSONObject(i).getString("Name");
                                agregarProducto(lineStr, "0");
                            }
                        } else {
                            makeToast(getApplicationContext(), "No se han encontrado resultados para " + scanResult);
                        }
                    }
                } catch (Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Exception exception) {
                VolleyError volleyError = (VolleyError) exception;
                NetworkResponse response = volleyError.networkResponse;
                String json = new String(response.data);
                Log.e("RestError", exception.toString());
                Log.e("RestError", json);
                Toast.makeText(MainActivity.this,
                        MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
