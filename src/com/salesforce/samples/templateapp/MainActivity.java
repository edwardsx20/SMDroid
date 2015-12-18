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
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {

	private static final String LIST_INSTANCE_STATE = "0x1";
	private RestClient client;
	private ArrayAdapter<String> listAdapter;
	private JSONArray sfResult;

	private ListView mlistView;
	private EditText txtRbd;

	private Parcelable mListInstanceState;
	private String scanResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup view
		setContentView(R.layout.main);
		mlistView = ((ListView) findViewById(R.id.contacts_list));

		// Create list adapter
		listAdapter = new ArrayAdapter<>(this, R.layout.list_black_text, new ArrayList<String>());
		mlistView.setAdapter(listAdapter);

		if (savedInstanceState != null) {
			mListInstanceState = savedInstanceState.getParcelable(LIST_INSTANCE_STATE);
		}

		// Views
		txtRbd = (EditText) findViewById(R.id.txtRbd);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		mListInstanceState = mlistView.onSaveInstanceState();
		savedState.putParcelable(LIST_INSTANCE_STATE, mListInstanceState);
	}

	@Override
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		if (mListInstanceState != null) {
			mlistView.onRestoreInstanceState(mListInstanceState);
		}

		super.onResume();
	}

	@Override
	public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;

		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);
	}

	/**
	 * Called when "Logout" button is clicked.
	 *
	 * @param v
	 */
	public void onLogoutClick(View v) {
		SalesforceSDKManager.getInstance().logout(this);
	}

	/**
	 * Called when "Clear" button is clicked.
	 *
	 * @param v
	 */
	public void onClearClick(View v) {
		listAdapter.clear();
	}

	/**
	 * Called when "Fetch Contacts" button is clicked
	 *
	 * @param v
	 * @throws UnsupportedEncodingException
	 */

	public void onFetchContactsClick(View v) throws UnsupportedEncodingException {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			if (result.getContents() == null) {
				Log.d("MainActivity", "Cancelled scan");
				Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
			} else {
				Log.d("MainActivity", "Scanned");
				Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
				scanResult = result.getContents();

				if (scanResult != null) {
					try {
						sendRequest("SELECT ProductCode, Name FROM Product2 WHERE ISBN__c = " + scanResult + ".0");
						//sendRequest("SELECT Name FROM Contact LIMIT 1");

						Log.d("RequestResponse", String.valueOf((sfResult != null)));

						if (sfResult != null) {
							if (sfResult.length() > 0) {
								String lineStr;
								for (int i = 0; i < sfResult.length(); i++) {
									lineStr = sfResult.getJSONObject(i).getString("ProductCode") + " - " + sfResult.getJSONObject(i).getString("Name");
									listAdapter.add(lineStr);
								}
							} else {
								Toast.makeText(this, "No se han encontrado resultados para " + scanResult, Toast.LENGTH_SHORT).show();
							}
						}
					} catch (Exception e) {
						onError(this, e.toString());
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

	private void onError(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called when "Fetch Accounts" button is clicked
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

	private void sendRequest(String soql) throws UnsupportedEncodingException {
		RestRequest restRequest = RestRequest.getRequestForQuery(getString(R.string.api_version), soql);

		client.sendAsync(restRequest, new AsyncRequestCallback() {
			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				try {
					sfResult = result.asJSONObject().getJSONArray("records");
				} catch (Exception e) {
					onError(e);
				}
			}

			@Override
			public void onError(Exception exception) {
				VolleyError volleyError = (VolleyError) exception;
				NetworkResponse response = volleyError.networkResponse;
				String json = new String(response.data);
				Log.e("RestError", json);
				Toast.makeText(MainActivity.this,
						MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
						Toast.LENGTH_LONG).show();
			}
		});
	}
}
