package com.wickapps.android.smartmenuedit;

/*
 * Copyright (C) 2019 Mark Wickham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {
	
	int i;	
	Locale lc;
	
	Boolean downloadFail = false;
	private CheckBox cb1;
	private Button b1;
	
	private ConnectionLog mLog;
	
	private SharedPreferences prefs;
	
	Boolean uploadFail = false;
	
    private EditText etPassword1, etPassword2, etPassword3, etPassword4;
    GenericTextWatcher watcher1, watcher2, watcher3, watcher4;

	private static String[] filesText = new String[] {"", "", "", "", "", ""};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);     
        
    	getWindow().setFlags(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	
        // Setup the ActionBar
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setSubtitle("Edit");
        getActionBar().setTitle("SmartMenu");

		try {
			mLog = new ConnectionLog(this);
		} catch (Exception e) {
		}
    	
    	prefs=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	// Stage 1: Check if we have the required Boot settings, if not prompt for first time setup
        Global.ServerIP = (new String(prefs.getString("serverip", "")));
        Global.SMID = (new String(prefs.getString("smid", "")));
		Global.ProtocolPrefix = (new String(prefs.getString("protocolprefix", "http://")));
		Global.CheckAvailability = (new Boolean(prefs.getBoolean("checkavailability", false)));
        Global.AdminPin = (new String(prefs.getString("adminpin", "")));
		
        // If any of these are blank, prompt for initial setting
        if ((Global.ServerIP.length() == 0)) {
        	getBootSettings();
        } else {
        	stage2();
        }
	}
    	
	private void stage2() {        
	    // Now we are ready to load after asking them for the password and menu choice         
        this.setContentView(R.layout.enterpassword);
	    	    
		etPassword1 = (EditText) findViewById(R.id.etPassword1);
		etPassword1.setRawInputType(Configuration.KEYBOARD_12KEY);
		etPassword2 = (EditText) findViewById(R.id.etPassword2);
		etPassword2.setRawInputType(Configuration.KEYBOARD_12KEY);
		etPassword3 = (EditText) findViewById(R.id.etPassword3);
		etPassword3.setRawInputType(Configuration.KEYBOARD_12KEY);
		etPassword4 = (EditText) findViewById(R.id.etPassword4);
		etPassword4.setRawInputType(Configuration.KEYBOARD_12KEY);
		
		// set the starting selected et
		etPassword1.requestFocus();
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		// setup the text watchers
	    watcher1 = new GenericTextWatcher(etPassword1);
	    etPassword1.addTextChangedListener(watcher1);
	    watcher2 = new GenericTextWatcher(etPassword2);
	    etPassword2.addTextChangedListener(watcher2);
	    watcher3 = new GenericTextWatcher(etPassword3);
	    etPassword3.addTextChangedListener(watcher3);
	    watcher4 = new GenericTextWatcher(etPassword4);
	    etPassword4.addTextChangedListener(watcher4);         
	}

	// Dialog for Initial Boot Settings
	private void getBootSettings() {
		LayoutInflater factory = LayoutInflater.from(this);            
        final View textEntryView = factory.inflate(R.layout.boot_settings, null);
        
        final CustomDialog customDialog = new CustomDialog(this);
        customDialog.setContentView(textEntryView);
        customDialog.show();
        customDialog.setCancelable(false);
        customDialog.setCanceledOnTouchOutside(false);
        
		TextView tx1 = (TextView) customDialog.findViewById(R.id.textBootTitle);
		tx1.setText(getString(R.string.boot_title));
		tx1 = (TextView) customDialog.findViewById(R.id.textBootIntro);
		tx1.setText(getString(R.string.boot_intro));

        EditText et1 = (EditText) customDialog.findViewById(R.id.serverIP);
        if (Global.ServerIP.length() == 0) {
            et1.setText(Global.ServerIPHint);
        } else {
            et1.setText(Global.ServerIP);
        }

		Button but1 = (Button) customDialog.findViewById(R.id.butContinue);
		but1.setText(getString(R.string.boot_continue));
        but1.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		// Grab the inputs and see how they look
        		Editor prefEdit = prefs.edit();

    			EditText et1 = (EditText) customDialog.findViewById(R.id.serverIP);
    			Global.ServerIP = et1.getText().toString();

                // Grab the SMID
                EditText et2 = (EditText) customDialog.findViewById(R.id.SMID);
                Global.SMID = et2.getText().toString();

				// Check for SSL Protocol
				CheckBox cbSSL = (CheckBox) customDialog.findViewById(R.id.SSLprotocol);
				if (cbSSL.isChecked()) Global.ProtocolPrefix = "https://";
				else Global.ProtocolPrefix = "http://";

				// Check for Server Availability 204 Check
				CheckBox cb204 = (CheckBox) customDialog.findViewById(R.id.Check204);
				if (cb204.isChecked()) Global.CheckAvailability = true;
				else Global.CheckAvailability = false;

				// Save settings if requested
                CheckBox cbSave = (CheckBox) customDialog.findViewById(R.id.checkSave);
                if (cbSave.isChecked()) {
                    prefEdit.putBoolean("checkavailability", Global.CheckAvailability);
                    prefEdit.putString("protocolprefix", Global.ProtocolPrefix);
                    prefEdit.putString("serverip", Global.ServerIP);
                    prefEdit.putString("smid", Global.SMID);
                    prefEdit.commit();
                }
        		
        		if ((Global.ServerIP.length() > 0)) {
        			// Carry on ...
        			customDialog.dismiss();
        			stage2();
        		}
        	}        	
        });
	}
	
	private class HttpsDownload extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			// need to download all the files from the server
			try {
				// Download config files
                Boolean connected = false;
                if (Global.CheckAvailability) connected = pingIP();
                else connected = checkInternetConnection();
				if (connected) {
					// Get the list of pictures from the server first
					String fileList = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + Global.PICBASE200 + Global.LISTER;
					Global.PICLISTTXT = Utils.DownloadText(fileList);

					// Get the settings file first
                    String settingsfile = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + "settings.txt";
                    Global.SETTINGSTXT = Utils.DownloadText(settingsfile);
                    Global.SETTINGSTXT = Utils.removeBOMchar(Global.SETTINGSTXT);
                    Global.Settings = new JSONArray(Global.SETTINGSTXT);
                    // Grab the menu version
                    Global.MenuVersion = jsonGetter(Global.Settings,"menuversion").toString();
                    // Grab the Admin PIN so we can ask for it before allowing menu edit
                    Global.AdminPin = jsonGetter(Global.Settings,"adminpin").toString();
                    Editor prefEdit = prefs.edit();
                    prefEdit.putString("adminpin", Global.AdminPin);
                    prefEdit.putString("menuversion", Global.MenuVersion);
                    prefEdit.commit();

                    // Check password
                    if (pwMatch()) {
                        publishProgress("Password OK");

                        String menufile = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + "menufile.txt";
                        String catfile = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + "category.txt";
                        String kitchfile = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + "kitchen.txt";
                        String optfile = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + "options.txt";
                        String extfile = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + "extras.txt";

                        publishProgress("Loading");

                        Global.MENUTXT = Utils.DownloadText(menufile);
                        Global.MENUTXT = Utils.removeBOMchar(Global.MENUTXT);
                        Global.MENUTXT = Utils.removeCommentLines(Global.MENUTXT);

                        Global.CATEGORYTXT = Utils.DownloadText(catfile);
                        Global.CATEGORYTXT = Utils.removeBOMchar(Global.CATEGORYTXT);
                        processMenu();

                        Global.KITCHENTXT = Utils.DownloadText(kitchfile);
                        Global.KITCHENTXT = Utils.removeBOMchar(Global.KITCHENTXT);

                        Global.OPTIONSTXT = Utils.DownloadText(optfile);
                        Global.OPTIONSTXT = Utils.removeBOMchar(Global.OPTIONSTXT);

                        Global.EXTRASTXT = Utils.DownloadText(extfile);
                        Global.EXTRASTXT = Utils.removeBOMchar(Global.EXTRASTXT);

                        publishProgress("Complete");
                        log("MenuEdit: download success, menuversion=" + Global.MenuVersion);
                    } else {
                        publishProgress("Password Fail");
                        downloadFail = true;
                    }
				} else {
                    log("MenuEdit Not Connected");
                    publishProgress("Not Connected");
                    downloadFail = true;
                }
			} catch (Exception e) {
				log("MenuEdit Download ex=" + e);
				downloadFail = true;
			}
			return null; 
		}
		@Override
		protected void onPostExecute(Void unused) {
			setProgressBarVisibility(false);
            if (downloadFail) {            	
                failedAuth2();
			} else {
                finish();
                Intent kintent = new Intent(getApplicationContext(), MenuEditor.class);
                kintent.setFlags((Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                startActivity(kintent);
			}
		}	
		@Override
		protected void onPreExecute() {
		}
		protected void onProgressUpdate(String... item) {
		    TextView txt = (TextView) findViewById(R.id.text);
		    txt.setText(item[0]); 
		}
	}
	
	// check for ping connectivity
	private boolean pingIP() {
    	String ip1 = Global.ProtocolPrefix + Global.ServerIP + Global.ServerReturn204;
    	int status = -1;
    	Boolean downloadSuccess = false;
    	try {
            URL url = new URL(ip1);
            OkHttpClient.Builder b = new OkHttpClient.Builder();
            b.readTimeout(Global.ReadTimeout, TimeUnit.MILLISECONDS);
            b.writeTimeout(Global.ReadTimeout, TimeUnit.MILLISECONDS);
            b.connectTimeout(Global.ConnectTimeout, TimeUnit.MILLISECONDS);
            final OkHttpClient client = b.build();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            status = response.code();
            if (status == 204) {
                downloadSuccess = true; 
    		}
    	} catch (Exception e) {
    		log("MenuEdit: pingIP e=" + e);
    		downloadSuccess = false;
    	}	
		return downloadSuccess; 
	} 
	
	private void processMenu() {
		// We have read in the Menu file, so now set up the image fetchURL200 arraylist for pics downloads
		String[] menuItem=Global.MENUTXT.split("\\n");
		String[] categoryItem=Global.CATEGORYTXT.split("\\n");
		Global.MenuMaxItems = menuItem.length;    
		Global.NumCategory = categoryItem.length;
		Global.NumSpecials = 0;
		Global.fetchURL200.clear();
		// Loop through each line and populate the the URL strings for image loading
		for(int i=0;i<menuItem.length;i++) {
			// parse each line into columns using the divider character "|"
			String[] menuColumns=menuItem[i].split("\\|");
	  		// if it is a special, then bump the counter
	  		if (menuColumns[1].equals("specials")) Global.NumSpecials++;
	  		// build the picture array lists
	  		String menuPic  = menuColumns[3];
	  		String menuPic200 = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + Global.PICBASE200 + menuPic;
			Global.fetchURL200.add(i,menuPic200);
		}
	}
    
    public boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
	  
    public void failedAuth0() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
    	alertDialog.setTitle("Connection");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("No connection. Please restart.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton("Exit", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			finish();
    		} });
    	alertDialog.show();
    }
    
    public void failedAuth1() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
    	alertDialog.setTitle("Connection");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("Upload error. Please restart.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton("Exit", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			finish();
    		} });
    	alertDialog.show();
    }
    
    public void failedAuth2() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
    	alertDialog.setTitle("Connection");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("Connection error. Please Restart.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton("Exit", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			finish();
    		} });
    	alertDialog.show();
    }
    
	private class GenericTextWatcher implements TextWatcher{
	    private View view;
	    private GenericTextWatcher(View view) {
	        this.view = view;
	    }
	    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
	    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
	    public void afterTextChanged(Editable editable) {
	    	//String text = editable.toString();
	        switch(view.getId()){
	            case R.id.etPassword1:
	            	etPassword2.requestFocus();
	                break;
	            case R.id.etPassword2:
	            	etPassword3.requestFocus();
	                break;
	            case R.id.etPassword3:
	            	etPassword4.requestFocus();
	                break;
	            case R.id.etPassword4:
                    // turn off keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                    setContentView(R.layout.splash);
                    // always read from server
                    if (checkInternetConnection()) {
                        new HttpsDownload().execute();
                    } else {
                        failedAuth0();
                    }
                break;
	        }
	    }
	}
	
	private boolean pwMatch() {
		Boolean result = false;
		String pw1 = etPassword1.getText().toString();
		String pw2 = etPassword2.getText().toString();
		String pw3 = etPassword3.getText().toString();
		String pw4 = etPassword4.getText().toString();
		String fullpw = pw1 + pw2 + pw3 + pw4;
        //log("MenuEdit: pwfull=" + fullpw);
        //log("MenuEdit: AdminPin=" + Global.AdminPin);
		if (fullpw.equals(Global.AdminPin)) result = true;
		return (result);
	}
    
	private Object jsonGetter(JSONArray json, String key) {
		Object value = null;
		for (int i=0; i<json.length(); i++) {
			try {
				JSONObject obj = json.getJSONObject(i);
				String name = obj.getString("name");
				if (name.equalsIgnoreCase(key)) {
					value = obj.get("value");
				}
			} catch (JSONException e) {
			}
		}
		return value;
	}
	
	// Log helper function
	private void log(String message) {
		log(message, null);
	}
	private void log(String message, Throwable e) {		
		if (mLog != null) {
			try {
				mLog.println(message);
			} catch (IOException ex) {}
		}		
	}
	  
}
