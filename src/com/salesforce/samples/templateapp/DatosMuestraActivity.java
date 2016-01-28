package com.salesforce.samples.templateapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

/**
 * Created by eaguad on 1/28/2016.
 */
public class DatosMuestraActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private TextView rbd;
    private TextView cuenta;
    private TextView direccion;
    private TextView productcode;
    private TextView productname;
    private TextView cantidad;
    private TextView fecha;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.datos_muestra);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        setValues();
    }

    private void setValues() {
        this.rbd = (TextView) findViewById(R.id.datos_rbd);
        this.cuenta = (TextView) findViewById(R.id.datos_account_name);
        this.direccion = (TextView) findViewById(R.id.datos_account_direccion);
        this.productcode = (TextView) findViewById(R.id.datos_product2_productcode);
        this.productname = (TextView) findViewById(R.id.datos_product2_name);
        this.cantidad = (TextView) findViewById(R.id.datos_product2_cantidad);
        this.fecha = (TextView) findViewById(R.id.datos_muestra_fecha);

        String rbd = getIntent().getExtras().getString("rbd");
        String cuenta = getIntent().getExtras().getString("cuenta");
        String direccion = getIntent().getExtras().getString("direccion");
        String productcode = getIntent().getExtras().getString("productcode");
        String productname = getIntent().getExtras().getString("productname");
        String cantidad = getIntent().getExtras().getString("cantidad");
        String fecha = getIntent().getExtras().getString("fecha");

        this.rbd.setText(rbd);
        this.cuenta.setText(cuenta);
        this.direccion.setText(direccion);
        this.productcode.setText(productcode);
        this.productname.setText(productname);
        this.cantidad.setText(cantidad);
        this.fecha.setText(fecha);
    }
}
