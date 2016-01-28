package cl.edicsm.control;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.samples.templateapp.ConsultarActivity;
import com.salesforce.samples.templateapp.DatosMuestraActivity;
import com.salesforce.samples.templateapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by eaguad on 12/23/2015.
 */
public class Controller {
    protected RestClient client;
    protected Context context;
    protected ProgressDialog mProgressDialog;

    public Controller(Context context, RestClient client, ProgressDialog mProgressDialog) {
        this.context = context;
        this.client = client;
        this.mProgressDialog = mProgressDialog;
    }

    public void getId(ArrayList<String[]> params) {
        new InsertMuestraTask().execute(params);
    }

    public void consultaMuestra(String rbd) throws ExecutionException, InterruptedException {
        new ConsultaMuestraTask().execute(rbd);
    }

    private void agregarProducto(String id, String msg, String value) {
        // Obtiene el grupo
        Activity activity = (Activity) context;
        LinearLayout parentLayout = (LinearLayout) activity.findViewById(R.id.consultar_root);

        // Crea el inflater
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view;

        // Obtiene el layout a insertar
        view = layoutInflater.inflate(R.layout.lst_productos_consultar, parentLayout, false);

        // Obtiene views
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.consultar_vgroup);
        linearLayout.setClickable(true);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LinearLayout productosLayout = (LinearLayout) v;
                    TextView tv = (TextView) productosLayout.getChildAt(0);

                    Log.d("Clicked", tv.getText().toString());

                    String idMuestra = tv.getText().toString();

                    new DatosMuestraTask().execute(idMuestra);

                } catch(Exception e) {
                    Log.e("ErrorClicked", v.toString());
                }
            }
        });
        TextView tvid = (TextView) linearLayout.findViewById(R.id.consultar_id);
        TextView et = (TextView) linearLayout.findViewById(R.id.consultar_txtproducto);
        TextView tv = (TextView) linearLayout.findViewById(R.id.consultar_txtcantidad);

        tvid.setText(id);
        tv.setText(msg);
        et.setText(value);

        parentLayout.addView(linearLayout);
    }

    private class DatosMuestraTask extends AsyncTask<String, Void, Void> {
        private Intent mIntent;

        @Override
        protected void onPostExecute(Void unused) {

            mProgressDialog.dismiss();

            context.startActivity(mIntent);
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle("Contactando con Salesforce...");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];

            RestRequest restRequest;

            String query = "SELECT Cantidad_respaldada__c, Fecha__c, Name, Producto__r.ProductCode, Producto__r.Name, Cuenta__r.RBD__c, Cuenta__r.Name, Cuenta__r.Calle_y_numero__c FROM MuestrasLineItem__c WHERE Id = '" + id + "'";
            try {
                restRequest = RestRequest.getRequestForQuery(context.getString(R.string.api_version), query);

                RestResponse response = client.sendSync(restRequest);
                JSONArray resultado = response.asJSONObject().getJSONArray("records");

                JSONObject cuenta = resultado.getJSONObject(0).getJSONObject("Cuenta__r");
                JSONObject producto = resultado.getJSONObject(0).getJSONObject("Producto__r");


                String strCantidad = resultado.getJSONObject(0).getString("Cantidad_respaldada__c");
                String strCuenta = cuenta.getString("Name");
                String strRbd = cuenta.getString("RBD__c");
                String strDireccion = cuenta.getString("Calle_y_Numero__c");

                String strProductCode = producto.getString("ProductCode");
                String strProductName = producto.getString("Name");
                String strFecha = resultado.getJSONObject(0).getString("Fecha__c");

                mIntent = new Intent(context, DatosMuestraActivity.class);
                Bundle mBundle = new Bundle();

                mBundle.putString("rbd", strRbd.substring(0, strRbd.indexOf(".")));
                mBundle.putString("cuenta", strCuenta);
                mBundle.putString("direccion", strDireccion);
                mBundle.putString("productcode", strProductCode);
                mBundle.putString("productname", strProductName);
                mBundle.putString("cantidad", strCantidad.substring(0, strCantidad.indexOf(".")));
                mBundle.putString("fecha", strFecha);

                mIntent.putExtras(mBundle);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private class ConsultaMuestraTask extends AsyncTask<String, Void, ArrayList<String[]>> {

        @Override
        protected void onPostExecute(ArrayList<String[]> unused) {
            if(unused != null && unused.size() > 0) {
                for(String[] vw : unused) {
                    agregarProducto(vw[0], vw[1], vw[2]);
                }
            } else {
                Toast.makeText(context, "No se han encontrado datos.", Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.dismiss();
        }

        @Override
        protected ArrayList<String[]> doInBackground(String... param) {
            //android.os.Debug.waitForDebugger();
            RestRequest restRequest = null;
            final ArrayList<String[]> result = new ArrayList<String[]>();

            try {
                String query = "SELECT Id, Producto__r.ProductCode, Name, Cantidad_respaldada__c FROM MuestrasLineItem__c WHERE Cuenta__r.RBD__c = " + param[0];
                restRequest = RestRequest.getRequestForQuery(context.getString(R.string.api_version), query);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                RestResponse response = client.sendSync(restRequest);
                JSONArray resultado = response.asJSONObject().getJSONArray("records");
                
                for(int i = 0; i < resultado.length(); i++) {
                    String[] values = null;

                    String cantidad = resultado.getJSONObject(i).getString("Cantidad_respaldada__c");
                    String name = resultado.getJSONObject(i).getString("Name");
                    String productcode = resultado.getJSONObject(i).getJSONObject("Producto__r").getString("ProductCode");
                    String id = resultado.getJSONObject(i).getString("Id");

                    if (cantidad != null && cantidad.indexOf('.') > 0) {
                        cantidad = cantidad.substring(0, cantidad.indexOf('.'));
                    }
                    name = productcode + " - " + name;

                    values = new String[] { id, cantidad, name };
                    result.add(values);
                }

                Log.d("ConsultaMuestraTask", "Success");
            } catch (IOException e) {
                e.printStackTrace();
                mProgressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                mProgressDialog.dismiss();
            }

            /*
            // Ejecuta Async
            client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, RestResponse response) throws IOException, JSONException {
                    //android.os.Debug.waitForDebugger();
                    JSONArray resultado = response.asJSONObject().getJSONArray("records");

                    for(int i = 0; i < resultado.length(); i++) {

                        String cantidad = resultado.getJSONObject(i).getString("Cantidad_respaldada__c");
                        String name = resultado.getJSONObject(i).getString("Name");

                        result.put(name, cantidad);
                    }

                    Log.d("ConsultaMuestraTask", "Success");
                }

                @Override
                public void onError(Exception exception) {
                    android.os.Debug.waitForDebugger();
                    VolleyError v = (VolleyError) exception;
                    Log.d("ConsultaMuestraTask", "Failed");
                }
            });
            */

            return result;
        }
    }

    private class InsertMuestraTask extends AsyncTask<ArrayList<String[]>, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle("Conectando con Salesforce...");
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();


            String mTitle = (result ? "Finalizado con éxito" : "Se insertaron con errores");

            Toast.makeText(context, mTitle, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(ArrayList<String[]>... params) {
            Map sObject;
            String productoId;
            String name;
            String cantidad;
            String cuentaId;
            String muestraId;
            ArrayList<Map> objetos = new ArrayList<>();
            Boolean result = true;

            try {
                for (String[] arr : params[0]) {
                    sObject = new HashMap<>();

                    cuentaId = getId("SELECT Id FROM Account WHERE RBD__c = " + arr[0]);
                    productoId = getId("SELECT Id FROM Product2 WHERE ProductCode = '" + arr[1] + "'");

                    //HARDCORE
                    muestraId = "a0MJ0000004LGaM";

                    cantidad = arr[2];
                    name = arr[3];

                    sObject.put("Producto__c", productoId);
                    sObject.put("Name", name);
                    sObject.put("Cuenta__c", cuentaId);
                    //sObject.put("Presupuesto_Muestras__c", "a0JJ000000A9j1cMAB");
                    sObject.put("Cantidad_respaldada__c", cantidad);
                    sObject.put("Muestra__c", muestraId);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String currentDateandTime = sdf.format(new Date());
                    //HARDCORE
                    sObject.put("Solicitante__c", "005G0000001CNHZ");
                    sObject.put("Producto__c", productoId);
                    sObject.put("Fecha__c", currentDateandTime);

                    objetos.add(sObject);
                }

                for (Map sobj : objetos) {
                    // Objeto request
                    if (sobj.get("Cuenta__c") != null && sobj.get("Producto__c") != null) {
                        RestRequest restRequest = RestRequest.getRequestForCreate(context.getString(R.string.api_version), "MuestrasLineItem__c", sobj);
                        // Ejecuta Async
                        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                            @Override
                            public void onSuccess(RestRequest request, RestResponse response) {
                                Log.d("GetIdTask", "Success");
                            }

                            @Override
                            public void onError(Exception exception) {
                                try {
                                    Log.e("InsertaMuestraTask", exception.getMessage());
                                } catch(Exception e) {
                                    Log.e("InsertaMuestraTask", e.getMessage());
                                }
                            }
                        });
                    }
                }

            } catch (Exception e) {
                Log.e("CreateProducto", e.toString());
                result = false;
            }

            return result;
        }

        private String getId(String soql) {
            String result = null;
            try {

                RestRequest restRequest = RestRequest.getRequestForQuery(context.getString(R.string.api_version), soql);

                RestResponse response = client.sendSync(restRequest);

                JSONArray resultado = response.asJSONObject().getJSONArray("records");
                result = resultado.getJSONObject(0).getString("Id");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("GETID", e.toString());
            }
            return result;
        }
    }
}
