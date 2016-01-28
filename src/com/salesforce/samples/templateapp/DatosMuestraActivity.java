package com.salesforce.samples.templateapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

import java.util.HashMap;

import cl.edicsm.control.Controller;

/**
 * Created by eaguad on 1/28/2016.
 */
public class DatosMuestraActivity extends SalesforceActivity {
    private Toolbar toolbar;

    private EditText rbd;
    private TextView cuenta;
    private TextView direccion;
    private EditText productcode;
    private TextView productname;
    private EditText cantidad;
    private TextView fecha;

    private String strRbd;
    private String strCuenta;
    private String strDireccion;
    private String strProductcode;
    private String strProductname;
    private String strCantidad;
    private String strFecha;

    private RestClient client;
    private String id;

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.datos_muestra);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        setValues();
    }

    private void setValues() {
        this.rbd = (EditText) findViewById(R.id.datos_rbd);
        this.cuenta = (TextView) findViewById(R.id.datos_account_name);
        this.direccion = (TextView) findViewById(R.id.datos_account_direccion);
        this.productcode = (EditText) findViewById(R.id.datos_product2_productcode);
        this.productname = (TextView) findViewById(R.id.datos_product2_name);
        this.cantidad = (EditText) findViewById(R.id.datos_product2_cantidad);
        this.fecha = (TextView) findViewById(R.id.datos_muestra_fecha);

        this.id = getIntent().getExtras().getString("id");

        strRbd = getIntent().getExtras().getString("rbd");
        strCuenta = getIntent().getExtras().getString("cuenta");
        strDireccion = getIntent().getExtras().getString("direccion");
        strProductcode = getIntent().getExtras().getString("productcode");
        strProductname = getIntent().getExtras().getString("productname");
        strCantidad = getIntent().getExtras().getString("cantidad");
        strFecha = getIntent().getExtras().getString("fecha");

        this.rbd.setText(strRbd);
        this.cuenta.setText(strCuenta);
        this.direccion.setText(strDireccion);
        this.productcode.setText(strProductcode);
        this.productname.setText(strProductname);
        this.cantidad.setText(strCantidad);
        this.fecha.setText(strFecha);
    }

    public void onBtnGuardarDatos(View v) {
        strRbd = rbd.getText().toString();
        strCuenta = cuenta.getText().toString();
        strDireccion = direccion.getText().toString();
        strProductcode = productcode.getText().toString();
        strProductname = productname.getText().toString();
        strCantidad = cantidad.getText().toString();

        ProgressDialog mProgressDialog = new ProgressDialog(this);

        HashMap<String, String> sObject = new HashMap<>();

        sObject.put("id", this.id);
        sObject.put("rbd", strRbd);
        sObject.put("productcode", strProductcode);
        sObject.put("cantidad", strCantidad);

        Controller control = new Controller(this, client, mProgressDialog);

        control.guardarMuestra(sObject);
    }

    private void eliminarMuestra(View v) {

    }
}
