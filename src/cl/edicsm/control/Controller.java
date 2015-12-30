package cl.edicsm.control;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.samples.templateapp.MainActivity;

import java.util.concurrent.ExecutionException;

/**
 * Created by eaguad on 12/23/2015.
 */
public class Controller {
    protected MainActivity mMainActivity;
    protected Context mContext;
    protected ProgressDialog progress;
    protected RestClient client;
    protected RestRequest restRequest;
    private RestResponse response;

    public Controller(Context mContext, MainActivity mMainActivity, ProgressDialog progress, RestClient client) {
        this.mContext = mContext;
        this.mMainActivity = mMainActivity;
        this.progress = progress;
        this.client = client;
    }

    public void setRestRequest(RestRequest restRequest) {
        this.restRequest = restRequest;
    }

    public RestResponse execute() throws ExecutionException, InterruptedException {
        return new SMDroidClient().execute().get();
    }

    public class SMDroidClient extends AsyncTask<Void, Void, RestResponse> {

        public SMDroidClient() {
        }

        public void onPreExecute() {
            android.os.Debug.waitForDebugger();
            progress.show();
        }

        public void onPostExecute(RestResponse unused) {
            android.os.Debug.waitForDebugger();
            progress.dismiss();
        }

        protected RestResponse doInBackground(Void... params) {
            try {
                response = client.sendSync(restRequest);
            } catch (Exception e) {
                Log.e("DOINBACKGROUND", e.toString());
            }
            return response;
        }
    }
}
