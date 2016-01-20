package cl.edicsm.control;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.samples.templateapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    public HashMap<String, String> consultaMuestra(String rbd) throws ExecutionException, InterruptedException {
        return new ConsultaMuestraTask().execute(rbd).get();
    }

    private class ConsultaMuestraTask extends AsyncTask<String, Void, HashMap<String, String>> {

        @Override
        protected HashMap<String, String> doInBackground(String... param) {
            RestRequest restRequest = null;
            final HashMap<String, String> result = null;
            try {
                String query = "SELECT Name, Cantidad_respaldada__c FROM MuestrasLineItem__c WHERE Tipo_transaccion__c = 'RESPALDO' Cuenta__c.RBD__c = " + param;
                restRequest = RestRequest.getRequestForQuery(context.getString(R.string.api_version), query);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Ejecuta Async
            client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, RestResponse response) throws IOException, JSONException {
                    JSONArray resultado = response.asJSONObject().getJSONArray("records");

                    for(int i = 0; i < resultado.length(); i++) {
                        result.put(resultado.getJSONObject(i).getString("Id"), resultado.getJSONObject(i).getString("Name"));
                    }

                    Log.d("ConsultaMuestraTask", "Success");
                }

                @Override
                public void onError(Exception exception) {
                    Log.d("ConsultaMuestraTask", "Failed");
                }
            });

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
            ArrayList<Map> objetos = new ArrayList<>();
            Boolean result = true;

            try {
                for (String[] arr : params[0]) {
                    sObject = new HashMap<>();

                    cuentaId = getId("SELECT Id FROM Account WHERE RBD__c = " + arr[0]);
                    productoId = getId("SELECT Id FROM Product2 WHERE ProductCode = '" + arr[1] + "'");
                    cantidad = arr[2];
                    name = arr[3];

                    sObject.put("Producto__c", productoId);
                    sObject.put("Name", name);
                    sObject.put("Cuenta__c", cuentaId);
                    sObject.put("Presupuesto_Muestras__c", "a0JJ000000A9j1cMAB");
                    sObject.put("Cantidad_entregada__c", cantidad);

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
                                Log.d("GetIdTask", "Failed");
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
