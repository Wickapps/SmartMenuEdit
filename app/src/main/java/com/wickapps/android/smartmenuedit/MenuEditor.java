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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.squareup.picasso.Picasso.get;

public class MenuEditor extends Activity implements OnNavigationListener {
	
    ListView listLines;
    SelectedAdapter adapterLines;
    private int curFile, curItem, totItems;
    TextView tt1, ttCI;
    LinearLayout ll1, ll2, ll3, ll4, ll5, ll6, ll7, ll8;
    Button optButton, extButton, catButton, picButton; 
    Button butNew, butSaveNew, butUpload, butDel, butMoveUp, butMoveDown;
    TextView tvM;
	
	String infoWifi;
	
	private static ConnectionLog mLog;
    
	private ViewFlipper vf;
    
    private EditText et11, et12, et13, et14, et15, et16, et17;
    GenericTextWatcher watcher11, watcher12, watcher13, watcher14, watcher15, watcher16, watcher17;
    private String s1,s2,s3,s4,s5,s6,s7;
    private EditText et21, et22, et23;
    GenericTextWatcher watcher21, watcher22, watcher23;
    private EditText et31, et32;
    GenericTextWatcher watcher31, watcher32;
    private EditText et51;
    GenericTextWatcher watcher51;
    private EditText et61, et62, et63;
    GenericTextWatcher watcher61, watcher62, watcher63;
    private EditText et71, et72, et73;
    GenericTextWatcher watcher71, watcher72, watcher73;
    private CheckBox cb1, cb2, cb3, cb4, cb5, cb6;

    // list below to be used to populate the drop down spinners
	private ArrayList<String> linesPjpg = new ArrayList<String>();
	private ArrayList<String> linesCcol0 = new ArrayList<String>();
	private ArrayList<String> linesOcol0 = new ArrayList<String>();
	private ArrayList<String> linesEcol0 = new ArrayList<String>();
	
	protected ArrayList<CharSequence> selectedOpts = new ArrayList<CharSequence>();
	protected ArrayList<CharSequence> selectedExts = new ArrayList<CharSequence>();
	protected int checkedCat, checkedPic;
	
	String[] linesM = Global.MENUTXT.split("\\n"); 
	String[] linesC = Global.CATEGORYTXT.split("\\n"); 
	String[] linesK = Global.KITCHENTXT.split("\\n"); 
	String[] linesS = Global.SETTINGSTXT.split("never gonna match this- keep 1 line edit");
	String[] linesO = Global.OPTIONSTXT.split("\\n");
	String[] linesE = Global.EXTRASTXT.split("\\n");
	String[] linesPic = Global.PICLISTTXT.split("\\r\\n");
	
	private static String[] filesLocalName 	= new String[] {"menufile.txt", "category.txt", "kitchen.txt", "settings.txt", "options.txt", "extras.txt"};
	private Boolean[] filesModified = new Boolean[] {false, false, false, false, false, false};

	private File tmpDir;
	
	String path1, path2;
	
	protected boolean pictureTaken;
	protected boolean uploadNewPic = false;
	protected String  uploadNewPicName;
	protected String  uploadNewPicNameLarge;
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	private static final ArrayList<Integer> MenuPosition 	= new ArrayList<Integer>();
	
//	Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();
    
//	Create runnables for posting
    final Runnable updateResults = new Runnable() {
        public void run() {
			butUpload.setVisibility(View.INVISIBLE);
			butNew.setVisibility(View.INVISIBLE);
			butDel.setVisibility(View.GONE);
			uploadNewPic = false;
			clearDetailArea();
			setWhiteTitles();
			removeChangeListeners();
			adapterLines.notifyDataSetChanged();
        }
    };
    final Runnable noConnection = new Runnable() {
        public void run() {
        	failedAuth0();
        }
    };
    final Runnable exceptionConnection = new Runnable() {
        public void run() {
        	failedAuth2();
        }
    };
    final Runnable exceptionPhoto = new Runnable() {
        public void run() {
        	Toast.makeText(getApplicationContext(), "Could Not Take Photo", Toast.LENGTH_LONG).show();
        }
    };
	
	@Override
    protected void onResume() {
       super.onResume();
       IntentFilter filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
       this.registerReceiver(wifiStatusReceiver, filter);
    }
    
	@Override
	public void onPause() {
		this.unregisterReceiver(wifiStatusReceiver);    
		super.onPause();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_editor);
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tmpDir = new File(getFilesDir(),"SmartMenuTmp");
		if(!tmpDir.exists())
			tmpDir.mkdirs();

        path1 = getFilesDir() + "/SmartMenuTmp/takepicOrig.jpg";
        path2 = getFilesDir() + "/SmartMenuTmp";
		
		try {
			mLog = new ConnectionLog(this);
		} catch (IOException e) {
		}
		
		// get a picture list for the spinner
		linesPjpg.clear();
		for(int i=0; i<linesPic.length; i++) {
			linesPjpg.add(i, linesPic[i]);
	    }
				
		// set up the category ids for the item separator bars
		setCatIdList();
		
		// get the option list for the button
		newOptionsList();
		
		// get the extra list for the button
		newExtrasList();
		
		// Need these buttons for later
		butNew = (Button) findViewById(R.id.butNew);
		butSaveNew = (Button) findViewById(R.id.butSaveNew);
		butDel = (Button) findViewById(R.id.butDelete);
	    butUpload = (Button) findViewById(R.id.butUpload);
	    butMoveUp = (Button) findViewById(R.id.butMoveUp);
	    butMoveDown = (Button) findViewById(R.id.butMoveDown);
	    
	    // Setup the Spinner in the ActionBar
	    // Setup the ActionBar
	    getActionBar().setDisplayShowTitleEnabled(true);
	    getActionBar().setSubtitle("Edit");
	    getActionBar().setTitle("SmartMenu");
	    
	    Context context = getActionBar().getThemedContext();
	    ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.MenuFiles, android.R.layout.simple_spinner_item);
	    list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    getActionBar().setListNavigationCallbacks(list, this);
		
		vf = (ViewFlipper) findViewById(R.id.details);
		
		clearDetailArea();
	    
	    butUpload.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
	    		// Upload is enable and clicked, send marked files to the server
	    		filesUpload();
	    	}
	    });
	    
	    butNew.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	   			// do the add
	   			displayNewItem(curFile, curItem);
				butNew.setVisibility(View.INVISIBLE);
				butSaveNew.setVisibility(View.VISIBLE);
	    	}
	    });
	    
	    butMoveDown.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	   			// Move the selected item Down 1 slot	    		
	    		int pos = curItem - 1;
	    		int newLoc = pos + 1;
	    	    switch (curFile) {
		    	    case 0:
			    		String tmp = linesM[pos];
			    		linesM[pos] = linesM[newLoc];
			    		linesM[newLoc] = tmp;
			    		Global.MENUTXT = saveArray2File(linesM);
			    		writeOutFile(Global.MENUTXT, filesLocalName[0]);
			    		filesModified[0] = true;
			    		linesM = Global.MENUTXT.split("\\n");
			    		clearDetailArea();
			    		totItems = linesM.length;
			    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesM, curFile);
			    		break;
		    	    case 1:
			    		tmp = linesC[pos];
			    		linesC[pos] = linesC[newLoc];
			    		linesC[newLoc] = tmp;
			    		Global.CATEGORYTXT = saveArray2File(linesC);
			    		writeOutFile(Global.CATEGORYTXT, filesLocalName[1]);
			    		filesModified[1] = true;
			    		linesC = Global.CATEGORYTXT.split("\\n");
			    		clearDetailArea();
			    		totItems = linesC.length;
			    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesC, curFile);
			    		// Also rebuild the Category list
			    		setCatIdList();
			    		// Also nned to rebuild the Menu List since category order has changed
			    		rebuildMenuDishes();
	    	    }
	        	listLines.setAdapter(adapterLines);
	        	adapterLines.setSelectedPosition(newLoc);
	        	listLines.smoothScrollToPosition(newLoc);
	        	adapterLines.unSelect();
	        	
	        	butUpload.setVisibility(View.VISIBLE);
	        	butNew.setVisibility(View.VISIBLE);
	        	butDel.setVisibility(View.GONE);
    			butSaveNew.setVisibility(View.INVISIBLE);
	    	}
	    });
	    
	    butMoveUp.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	   			// Move the selected item Up 1 slot	    		
	    		int pos = curItem - 1;
	    		int newLoc = pos - 1;
	    	    switch (curFile) {
	    	    case 0:
		    		String tmp = linesM[pos];
		    		linesM[pos] = linesM[newLoc];
		    		linesM[newLoc] = tmp;
		    		Global.MENUTXT = saveArray2File(linesM);
		    		writeOutFile(Global.MENUTXT, filesLocalName[0]);
		    		filesModified[0] = true;
		    		linesM = Global.MENUTXT.split("\\n");
		    		clearDetailArea();
		    		totItems = linesM.length;
		    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesM, curFile);
		    		break;
	    	    case 1:
		    		tmp = linesC[pos];
		    		linesC[pos] = linesC[newLoc];
		    		linesC[newLoc] = tmp;
		    		Global.CATEGORYTXT = saveArray2File(linesC);
		    		writeOutFile(Global.CATEGORYTXT, filesLocalName[1]);
		    		filesModified[1] = true;
		    		linesC = Global.CATEGORYTXT.split("\\n");
		    		clearDetailArea();
		    		totItems = linesC.length;
		    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesC, curFile);
		    		// Also rebuild the Category list
		    		setCatIdList();
		    		// Also need to rebuild the Menu List since category order has changed
		    		rebuildMenuDishes();
		    		break;
	    	    }
	        	listLines.setAdapter(adapterLines);
	        	adapterLines.setSelectedPosition(newLoc);
	        	listLines.smoothScrollToPosition(newLoc);
	        	adapterLines.unSelect();
	        	
	        	butUpload.setVisibility(View.VISIBLE);
	        	butNew.setVisibility(View.VISIBLE);
	        	butDel.setVisibility(View.GONE);
    			butSaveNew.setVisibility(View.INVISIBLE);
	    	}
	    });
	    
	    butSaveNew.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	   			// when a new item is saved, this will validate all fields and then add the item to the file list
	    	    switch (curFile) {
	    	    case 0:
	    	    	String s1 = et11.getText().toString();
	    	    	String s2 = et12.getText().toString();
	    	    	String s3 = et13.getText().toString();
	    	    	String s4 = et14.getText().toString();
	    	    	String s5 = et15.getText().toString();
	    	    	String s6 = et16.getText().toString();
	    	    	String s7 = et17.getText().toString();
	    	    	if ((s1.length() > 0 ) && (s2.length() > 0) && (s3.length() > 0) && (s4.length() > 0) && (s5.length() > 0) && (s6.length() > 0) && (s7.length() > 0)) {
	    	    		// add the new item after the current last item in its category
	    	    		String newEntry = decode1();
	    	    		String cat = catButton.getText().toString();
	    	    		int newLoc = lastInCat(cat);
	    	    		// if newLoc came back not found (-1), we need to find prior CAT that has a dish and insert after it
	    	    		// set up for the while loop...
	    	    		int priorCatIndx = categoryGetIndex(cat) - 1;
	    	    		if (priorCatIndx < 0) priorCatIndx = 0;
	    	    		String priorCat = linesCcol0.get(priorCatIndx);
	    	    		while (newLoc == -1) {    	    			
	    	    			newLoc = lastInCat(priorCat);
	    	    			priorCatIndx = categoryGetIndex(priorCat) - 1;
	    	    			if (priorCatIndx < 0) priorCatIndx = 0;
	    	    			priorCat = linesCcol0.get(priorCatIndx);
	    	    		}
	    	    		
	    	    		// increment -- as we want to add to the slot after the last item
	    	    		newLoc = newLoc +1;
	    	    		
	        	    	String[] newLinesM = new String[linesM.length + 1];
	        	    	
	        	    	System.arraycopy(linesM, 0, newLinesM, 0, newLoc);
	    	    		newLinesM[newLoc] = newEntry;
	    	    		System.arraycopy(linesM, newLoc, newLinesM, newLoc+1, linesM.length-newLoc);
	    	    		
	    	    		Global.MENUTXT = saveArray2File(newLinesM);
	    	    		writeOutFile(Global.MENUTXT, filesLocalName[0]);
	    	    		filesModified[0] = true;
	    	    		linesM = Global.MENUTXT.split("\\n");
	    	    		clearDetailArea();
	    	    		totItems = newLinesM.length;
	    	    		
	    	    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, newLinesM, curFile);
	    	        	listLines.setAdapter(adapterLines);
	    	        	adapterLines.setSelectedPosition(newLoc);
	    	        	listLines.smoothScrollToPosition(newLoc);
	    	        	adapterLines.unSelect();
	    	        	
	    	        	butUpload.setVisibility(View.VISIBLE);
	        			butSaveNew.setVisibility(View.INVISIBLE);
	    	    	} else {
	    	    		validationEmpty();
	    	    	}
	    	    	break;
	    	    case 1:
	    	    	String[] newLinesC = new String[linesC.length + 1];
	    	    	// always add new C items to the bottom
	    	    	System.arraycopy(linesC, 0, newLinesC, 0, linesC.length);
	    	    	s1 = et21.getText().toString();
	    	    	s2 = et22.getText().toString();
	    	    	s3 = et23.getText().toString();
	    	    	if ((s1.length() > 0 ) && (s2.length() > 0) && (s3.length() > 0)) {
	    	    		newLinesC[linesC.length] = s1 + "|" + s2 + "\\" + s3;
	    	    		Global.CATEGORYTXT = saveArray2File(newLinesC);
	    	    		writeOutFile(Global.CATEGORYTXT, filesLocalName[1]);
	    	    		filesModified[1] = true;
	    	    		linesC = Global.CATEGORYTXT.split("\\n");
	    	    		clearDetailArea();
	    	    		totItems = newLinesC.length;
	    	    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, newLinesC, curFile);
	    	    		butUpload.setVisibility(View.VISIBLE);
	        			butSaveNew.setVisibility(View.INVISIBLE);
	    	    	} else {
	    	    		validationEmpty();
	    	    	}
	    	    	break;
	    	    case 2:
	    	    	String[] newLinesK = new String[linesK.length + 1];
	    	    	// always add new K items to the bottom
	    	    	System.arraycopy(linesK, 0, newLinesK, 0, linesK.length);
	    	    	s1 = et31.getText().toString();
	    	    	s2 = et32.getText().toString();
	    	    	if ((s1.length() > 0 ) && (s2.length() > 0)) {
	    	    		newLinesK[linesK.length] = s1 + "|" + s2;
	    	    		Global.KITCHENTXT = saveArray2File(newLinesK);
	    	    		writeOutFile(Global.KITCHENTXT, filesLocalName[2]);
	    	    		filesModified[2] = true;
	    	    		linesK = Global.KITCHENTXT.split("\\n");
	    	    		clearDetailArea();
	    	    		totItems = newLinesK.length;
	    	    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, newLinesK, curFile);
	    	    		butUpload.setVisibility(View.VISIBLE);
	        			butSaveNew.setVisibility(View.INVISIBLE);
	    	    	} else {
	    	    		validationEmpty();
	    	    	}
	    	    	break;	
	    	    case 3:
	    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesS, curFile);
	    	    	break;	
	    	    case 4:
	    	    	String[] newLinesO = new String[linesO.length + 1];
	    	    	// always add new O items to the bottom
	    	    	System.arraycopy(linesO, 0, newLinesO, 0, linesO.length);
	    	    	s1 = et61.getText().toString();
	    	    	s2 = et62.getText().toString();
	    	    	s3 = et63.getText().toString();
	    	    	if ((s1.length() > 0 ) && (s2.length() > 0) && (s3.length() > 0)) {
	    	    		newLinesO[linesO.length] = s1 + "|" + s2 + "\\" + s3;
	    	    		Global.OPTIONSTXT = saveArray2File(newLinesO);
	    	    		writeOutFile(Global.OPTIONSTXT, filesLocalName[4]);
	    	    		filesModified[4] = true;
	    	    		linesO = Global.OPTIONSTXT.split("\\n");
	    	    		clearDetailArea();
	    	    		totItems = newLinesO.length;
	    	    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, newLinesO, curFile);
	    	    		butUpload.setVisibility(View.VISIBLE);
	        			butSaveNew.setVisibility(View.INVISIBLE);
	        			// refresh the options list
	        			newOptionsList();
	    	    	} else {
	    	    		validationEmpty();
	    	    	}
	    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesO, curFile);
	    	    	break;	
	    	    case 5:
	    	    	String[] newLinesE = new String[linesE.length + 1];
	    	    	// always add new E items to the bottom
	    	    	System.arraycopy(linesE, 0, newLinesE, 0, linesE.length);
	    	    	s1 = et71.getText().toString();
	    	    	s2 = et72.getText().toString();
	    	    	s3 = et73.getText().toString();
	    	    	if ((s1.length() > 0 ) && (s2.length() > 0) && (s3.length() > 0)) {
	    	    		newLinesE[linesE.length] = s1 + "|" + s2 + "\\" + s3;
	    	    		Global.EXTRASTXT = saveArray2File(newLinesE);
	    	    		writeOutFile(Global.EXTRASTXT, filesLocalName[5]);
	    	    		filesModified[5] = true;
	    	    		linesE = Global.EXTRASTXT.split("\\n");
	    	    		clearDetailArea();
	    	    		totItems = newLinesE.length;
	    	    		adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, newLinesE, curFile);
	    	    		butUpload.setVisibility(View.VISIBLE);
	        			butSaveNew.setVisibility(View.INVISIBLE);
	        			// refresh the extras list
	        			newExtrasList();
	    	    	} else {
	    	    		validationEmpty();
	    	    	}
	    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesE, curFile);
	    	    	break;	
	    	    }
	        	listLines.setAdapter(adapterLines);
	        	////adapterFiles.notifyDataSetChanged();
	    	}
	    });
	    
	    butDel.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    	   	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
	    	   	alertDialog.setTitle("Delete Item");
	    	   	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
	    	   	alertDialog.setMessage("The current selected Item will be permanently deleted.");
	    	   	alertDialog.setCancelable(false); 
	    	   	alertDialog.setButton2("Back", new DialogInterface.OnClickListener() {
	    	   		public void onClick(DialogInterface dialog, int which) {
	    	   		} });
	    	   	alertDialog.setButton("Delete", new DialogInterface.OnClickListener() {
	    	   		public void onClick(DialogInterface dialog, int which) {
	    	   			// do the delete
	    	   			if (deleteItem(curFile, curItem)) {
		    	   			// notifyDataSetChanged does not work here cause we use String Arrays to control the Adapter, not ArrayLists
		    	   			// So do it with a case
		    	    	    switch (curFile) {
		    	    	    case 0:
		    	    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesM, curFile);
		    	    	    	break;
		    	    	    case 1:
		            	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesC, curFile);
		            	    	break;
		    	    	    case 2:
		            	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesK, curFile);
		            	    	break;	
		    	    	    case 3:
		            	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesS, curFile);
		            	    	break;	
		    	    	    case 4:
		            	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesO, curFile);
		            	    	break;	
		    	    	    case 5:
		            	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesE, curFile);
		            	    	break;	
		    	    	    }
		    	        	listLines.setAdapter(adapterLines);
		    	        	// move back to the general area where they made the deletion
		    	        	adapterLines.setSelectedPosition(curItem-1);
		    	        	listLines.smoothScrollToPosition(curItem-1);
		    	        	// Dont show the selection highlight
		    	        	adapterLines.unSelect();
	    	   			} else {
	    	   				// Delete failed
	    	   				Toast.makeText(MenuEditor.this, "Delete Failed", Toast.LENGTH_SHORT).show();
	    	   			}
	    	        	butDel.setVisibility(View.GONE);
	    	   		} });
	    	   	alertDialog.show();
	    	}
	    });
	}
	
	private Uri outputFileUri;
	private Uri photoUri;
	private Bitmap bitmapOrig;
	protected static Uri preDefinedCameraUri = null;
	protected static int rotateXDegrees = 0;
	protected static Uri photoUriIn3rdLocation = null;
	protected static Date dateCameraIntentStarted = null;
	
	/**
	 * Receives all activity results and triggers onCameraIntentResult if 
	 * the requestCode matches.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			super.onActivityResult(requestCode, resultCode, intent);
			switch (requestCode) {
				case 1717: {
					log("1717: reqCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent.toString());
					onCameraIntentResult(requestCode, resultCode, intent);
					break;
				}
			}
		} catch (Exception e) {
			log("Failed to take photo e=" + e);
			mHandler.post(exceptionPhoto);
		}
	}

	/**
	 * On camera activity result, we try to locate the photo.
	 * 
	 * <b>Mediastore:</b>
	 * First, we try to read the photo being captured from the MediaStore. Using a ContentResolver on the MediaStore content, 
	 * we retrieve the latest image being taken, as well as its orientation property and its timestamp. 
	 * If we find an image and it was not taken before the camera intent was called, it is the image we were looking for. 
	 * Otherwise, we dismiss the result and try one of the following approaches.
	 * <b>Intent extra:</b>
	 * Second, we try to get an image Uri from intent.getData() of the returning intent. If this is not successful either, we continue with step 3.
	 * <b>Default photo Uri:</b>
	 * If all of the above mentioned steps did not work, we use the image Uri we passed to the camera activity.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	protected void onCameraIntentResult(int requestCode, int resultCode, Intent intent) {
		try {
			log("MenuEdit: onCameraIntentResult resultCode=" + resultCode);
			log("MenuEdit: onCameraIntentResult requestCode=" + requestCode);
		    if (resultCode == RESULT_OK) {
	            final boolean isCamera;
	            if (intent == null) {
	                isCamera = true;
	            } else {
	                final String action = intent.getAction();
	                log("MenuEdit: Intent=" + intent.toString());
	                log("MenuEdit: onActivityResult action=" + action);
	                if(action == null) {
	                    isCamera = false;
	                } else {
	                    isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
	                }
	            }
	            Uri selectedImageUri;
	            if(isCamera) {
	                //selectedImageUri = outputFileUri;
	            	log("MenuEdit: onActivityResult isCamera");
	            	log("MenuEdit: path1=" + path1);            	
	            	bitmapOrig = BitmapFactory.decodeFile(path1);
	                onPhotoTaken();
	            } else {
	                selectedImageUri = intent == null ? null : intent.getData();
	                log("MenuEdit: onActivityResult is Not Camera");
	                //path1 = getRealPathFromURI(selectedImageUri);
	                //path1 = selectedImageUri.getPath();
	                log("MenuEdit: uri=" + selectedImageUri);
	                //log("MenuEdit: path1=" + path1);
	                InputStream is;
					try {
						is = getContentResolver().openInputStream(selectedImageUri);
		                bitmapOrig = BitmapFactory.decodeStream(is);
		                log("bitmapOrig=bmf.dsi");
		                is.close();
					} catch (Exception e) {
						log("OnCamIntRes e=" + e);
					}
	                onPhotoTaken();
	            }
			} else if (resultCode == Activity.RESULT_CANCELED) {
				onCanceled();
			} else {
				onCanceled();
			}
		} catch (Exception e) {
			log("Failed to take photo e=" + e);
			mHandler.post(exceptionPhoto);
		}
	}
	
	/**
	 * Being called if the photo could be located. The photo's Uri 
	 * and its orientation could be retrieved.
	 */
	protected void onPhotoUriFound() {
		log("Your photo is stored under: " + photoUri.toString());
	}
	
	/**
	 * Being called if the photo could not be located (Uri == null). 
	 */
	protected void onPhotoUriNotFound() {
		log("Could not find a photoUri that is != null");
	}
	
	/**
	 * Being called if the camera intent could not be started or something else went wrong.
	 */
	protected void onCouldNotTakePhoto() {
		Toast.makeText(getApplicationContext(), "Could Not Take Photo", Toast.LENGTH_LONG).show();		
	}

	/**
	 * Being called if the SD card (or the internal mass storage respectively) is not mounted.
	 */
	protected void onSdCardNotMounted() {
		Toast.makeText(getApplicationContext(), "SD Card Not Mounted", Toast.LENGTH_LONG).show();		
	}

	/**
	 * Being called if the camera intent was canceled.
	 */
	protected void onCanceled() {
		log("Camera Intent was canceled");
	}
	
	/**
	 * Given an Uri that is a content Uri (e.g.
	 * content://media/external/images/media/1884) this function returns the
	 * respective file Uri, that is e.g. file://media/external/DCIM/abc.jpg
	 * 
	 * @param cameraPicUri
	 * @return Uri
	 */
	private Uri getFileUriFromContentUri(Uri cameraPicUri) {
		try {
			if (cameraPicUri != null && cameraPicUri.toString().startsWith("content")) {
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(cameraPicUri, proj, null, null, null);
				cursor.moveToFirst();
				// This will actually give you the file path location of the image.
				String largeImagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
				return Uri.fromFile(new File(largeImagePath));
			}
			return cameraPicUri;
		} catch (Exception e) {
			return cameraPicUri;
		}
	}	
	
	// picture taking handler
    public class CameraClickHandler implements OnClickListener {
    	public void onClick( View view ) {
    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    			try {
    				File file = new File(path1);
    				outputFileUri = Uri.fromFile(file);

    				// Camera.
    				final List<Intent> cameraIntents = new ArrayList<Intent>();
    				
    				final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    				//captureIntent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
    				final PackageManager packageManager = getPackageManager();
    				final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
    				for(ResolveInfo res : listCam) {
    					final String packageName = res.activityInfo.packageName;
    					final Intent intent = new Intent(captureIntent);
    					intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
    					intent.setPackage(packageName);
    					intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    					cameraIntents.add(intent);
    				}    				

    				// Filesystem.
    				final Intent galleryIntent = new Intent();
    				galleryIntent.setType("image/*");
    				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

    			    // Chooser of filesystem options.
    			    final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

    			    // Add the camera options.
    			    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

    				startActivityForResult(chooserIntent, 1717);
    	    
    			} catch (Exception e) {
    				log("CouldNotTakePhoto Exception e=" + e);
    				onCouldNotTakePhoto();
    			}
    		} else {
    			log("SDCardNotMounted");
    			onSdCardNotMounted();
    		}
    	}
    }
    
    protected void onPhotoTaken() {
    	try {
	    	pictureTaken = true;
	    			
			// pop up a dialog so we can get a name for the new pic (NP) and upload it
			final Dialog dialogName = new Dialog(MenuEditor.this);
	        dialogName.setContentView(R.layout.newpic_name);
	        dialogName.setCancelable(true);
	        dialogName.setCanceledOnTouchOutside(true);
	          
	        // lets scale the title on the popup box
	        String tit = getString(R.string.new_pic_title);                 
	        SpannableStringBuilder ssBuilser = new SpannableStringBuilder(tit);
	        StyleSpan span = new StyleSpan(Typeface.BOLD);
	        ScaleXSpan span1 = new ScaleXSpan(2);
	        ssBuilser.setSpan(span, 0, tit.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
	        ssBuilser.setSpan(span1, 0, tit.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
	        dialogName.setTitle(ssBuilser);
	          
	        TextView NPtext=(TextView) dialogName.findViewById(R.id.NPtext);
	
	        NPtext.setText(getString(R.string.new_pic_text1)); 
	        NPtext.setTextSize(Utils.getFontSize(MenuEditor.this));
	        // edit text box is next
	        Button NPcancel = (Button) dialogName.findViewById(R.id.NPcancel);
	        NPcancel.setTextSize(Utils.getFontSize(MenuEditor.this));
	        NPcancel.setText(getString(R.string.cancel));
	        NPcancel.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	dialogName.dismiss();
	            }
	        });	
	        Button NPsave = (Button) dialogName.findViewById(R.id.NPadd);
	        NPsave.setTextSize(Utils.getFontSize(MenuEditor.this));
	        NPsave.setText(getString(R.string.save));
	        NPsave.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
                try {
                    EditText nameET = (EditText) dialogName.findViewById(R.id.NPedit);
                    String name = nameET.getText().toString();
                    name = name.replaceAll("[^\\p{L}\\p{N}]", "");
                    if (name.equalsIgnoreCase("")) name = "newpic";
                    String nameLarge = name.toLowerCase() + "-large.jpg";
                    name = name.toLowerCase() + ".jpg";

                    int width, height, newWidth, newHeight, newWidthLarge, newHeightLarge;

                    //adjust for camera orientation
                    width = bitmapOrig.getWidth();
                    height = bitmapOrig.getHeight();

                    // create a matrix for the manipulation
                    Matrix matrix = new Matrix();
                    Matrix matrixLarge = new Matrix();

                    String manufacturer = android.os.Build.MANUFACTURER.toLowerCase(Locale.ENGLISH);
                    if ((manufacturer.contains("samsung")) || (manufacturer.contains("sony"))) {
                        log("Fix broken rotation");
                        newWidth = 225;
                        newHeight = 150;
                        newWidthLarge = 735;
                        newHeightLarge = 490;
                        // calculate the scale
                        float scaleWidth = ((float) newWidth) / width;
                        float scaleHeight = ((float) newHeight) / height;
                        float scaleWidthLarge = ((float) newWidthLarge) / width;
                        float scaleHeightLarge = ((float) newHeightLarge) / height;
                        // resize the bit map
                        matrix.postScale(scaleWidth, scaleHeight);
                        matrixLarge.postScale(scaleWidthLarge, scaleHeightLarge);
                        // fixed the broken sammy rotate
                        matrix.postRotate(90);
                        matrixLarge.postRotate(90);
                    } else {
                        log("Normal");
                        newWidth = 150;
                        newHeight = 225;
                        newWidthLarge = 490;
                        newHeightLarge = 735;
                        // calculate the scale
                        float scaleWidth = ((float) newWidth) / width;
                        float scaleHeight = ((float) newHeight) / height;
                        float scaleWidthLarge = ((float) newWidthLarge) / width;
                        float scaleHeightLarge = ((float) newHeightLarge) / height;
                        // resize the bit map
                        matrix.postScale(scaleWidth, scaleHeight);
                        matrixLarge.postScale(scaleWidthLarge, scaleHeightLarge);
                    }

                    // save a scaled down Bitmap
                    Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrig, 0, 0, width, height, matrix, true);
                    Bitmap resizedLargeBitmap = Bitmap.createBitmap(bitmapOrig, 0, 0, width, height, matrixLarge, true);

                    path2 = getFilesDir() + "/SmartMenuTmp";
                    File file2 = new File (path2, name);
                    File file3 = new File (path2, nameLarge);

                    try {
                        FileOutputStream out = new FileOutputStream(file2);
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out = new FileOutputStream(file3);
                        resizedLargeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // update the picture
                    ImageView img = (ImageView) findViewById(R.id.ll31img);
                    img.setImageBitmap(resizedBitmap);

                    // save new name on spinner, add it to the array, marked as changed, set flag for upload
                    picButton.setText(name);
                    linesPjpg.add(name);
                    if (!butSaveNew.isShown()) {
                        tvM = (TextView) findViewById(R.id.txtPic);
                        tvM.setTextColor(Color.parseColor("#fe7f3d"));
                    }
                    uploadOrange();
                    uploadNewPic = true;
                    uploadNewPicName = name;
                    uploadNewPicNameLarge = nameLarge;
                    dialogName.dismiss();
                } catch (Exception e) {
                    log("NPSave e=" + e);
                    mHandler.post(exceptionPhoto);
                }
	            }
	        });	
	        dialogName.show();
	} catch (Exception e) {
		log("onPhotoTaken e=" + e);
		mHandler.post(exceptionPhoto);
	}
    }
    
    @Override 
    protected void onRestoreInstanceState( Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        //outputFileUri = Uri.parse(savedInstanceState.getString("outputFileUri"));
    	if( savedInstanceState.getBoolean( PHOTO_TAKEN ) ) {
    		onPhotoTaken();
    	}
    }
    
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        //outState.putString("outputFileUri",outputFileUri.toString());
    	outState.putBoolean( PHOTO_TAKEN, pictureTaken );
        super.onSaveInstanceState(outState);
    }   
		
    public void filesUpload() {
        // download the file list for the server spinner
		final ProgressDialog pd = ProgressDialog.show(MenuEditor.this,"Uploading","Uploading files to the server...",true, false);
		new Thread(new Runnable(){
			public void run(){
				try {
				// see if we can ping the server first
					if (pingIP()) {
						for (int i=0; i<filesLocalName.length; i++) {    
		    				String fpath = Global.SMID;
		    				String fname = filesLocalName[i];
		    				File fbody = new File(tmpDir, fname);
		    				if (filesModified[i]) {
		    					log("MenuEdit: Upload: fname=" + fname + " fpath=" + fpath + " fbody=" + fbody + " lenfbody=" + fbody.length());
		    					Utils.Uploader(fbody, fname, fpath);
		    				}
		    				filesModified[i] = false;
		    			}
		    			// if there is a new picture, upload it
		    			if (uploadNewPic) {
		    				String fpath = Global.SMID + "/" + Global.PICBASE200 ;
		    				String fname = uploadNewPicName;
		    				File fbody = new File(tmpDir, fname);
		    				Utils.Uploader(fbody, fname, fpath);
		    				// Uload the large picture to another directory for large pics, but with the same filename
		    				fpath = Global.SMID + "/" + Global.PICBASE800 ;
		    				String fnameLarge = uploadNewPicNameLarge;
		    				fbody = new File(tmpDir, fnameLarge);
		    				Utils.Uploader(fbody, fname, fpath);
		    			}
		    			// files were uploaded, so now we need to increment the version and update the settings.txt file on the server
					    Integer mv = Integer.parseInt(Global.MenuVersion);
					    Integer newmv = mv + 1;
					    String formatmv = String.format("%04d", newmv);
					    Global.MenuVersion = formatmv;
					    // update the json
					    jsonSetter(Global.Settings, "menuversion", Global.MenuVersion);
					    // write out settings.txt to the tmp directory
					    writeOutFile(Global.Settings.toString(4), filesLocalName[3]);
					    // upload the settings.txt file which has the menuversion
						String fpath = Global.SMID;
						String fname = filesLocalName[3];
						File fbody = new File(tmpDir, fname);
						Utils.Uploader(fbody, fname, fpath);
						log("MenuEdit: Upload: success");
						invalidateOptionsMenu();
		    			mHandler.post(updateResults);  
				    } else {
				    	log("MenuEdit: Upload fail No Ping");
				    	mHandler.post(noConnection);
				    }
				} catch (Exception e) {
					log("MenuEdit: Upload fail Exception e=" + e);
					mHandler.post(exceptionConnection);
				}
				pd.dismiss();
			}
		}).start(); 
    }
	
	private boolean anyFilesModified() {
		boolean yesMod = false;
		for (int i=0; i<filesLocalName.length; i++) { 		
    		if (filesModified[i]) {
            	yesMod = true;
            	break;
    		}
        }
        return yesMod;
	}
	
	private void saveChanges() {
    		// find out which file/cat was modified and save the input variables
    		int whichFile = vf.getDisplayedChild();

    	    switch (whichFile) {
    	    case 0:
    	    	//Toast.makeText(MenuEditor.this, "file=menu", Toast.LENGTH_SHORT).show();
    	    	filesModified[0] = true;
    	    	linesM[curItem -1] = decode1();		//call the decode to build a new string from the UI widgets
    	    	Global.MENUTXT = saveArray2File(linesM);
            	writeOutFile(Global.MENUTXT, filesLocalName[0]);
            	// update items on the left, such as color markers, etc...
            	adapterLines.notifyDataSetChanged();
    	    	break;
    	    case 1:
    	    	filesModified[1] = true;
            	et21 = (EditText) findViewById(R.id.etCatName);
            	s1 = et21.getText().toString();
            	et22 = (EditText) findViewById(R.id.etEngName);
            	s2 = et22.getText().toString();
            	et23 = (EditText) findViewById(R.id.etChName);
            	s3 = et23.getText().toString();
            	linesC[curItem - 1] = s1 + "|" + s2 + "\\" + s3;
            	Global.CATEGORYTXT = saveArray2File(linesC);
            	writeOutFile(Global.CATEGORYTXT, filesLocalName[1]);
    	    	break;
    	    case 2:
    	    	filesModified[2] = true;
            	et31 = (EditText) findViewById(R.id.etKitchenName);
            	s1 = et31.getText().toString();
            	et32 = (EditText) findViewById(R.id.etKitchenReplace);
            	s2 = et32.getText().toString();
            	linesK[curItem - 1] = s1 + "|" + s2;
            	Global.KITCHENTXT = saveArray2File(linesK);
            	writeOutFile(Global.KITCHENTXT, filesLocalName[2]);
    	    	break;
    	    case 3:
    	    	filesModified[3] = true;
            	et51 = (EditText) findViewById(R.id.etSettings);
            	s1 = et51.getText().toString();
            	linesS[curItem - 1] = s1;
            	Global.SETTINGSTXT = saveArray2File(linesS);
            	writeOutFile(Global.SETTINGSTXT, filesLocalName[3]);
            	// Update the JSON version
            	try {
					Global.Settings = new JSONArray(Global.SETTINGSTXT);
				} catch (JSONException e) {
					log("Case4 JSON e=" + e);
				}
    	    	break;
    	    case 4:
    	    	filesModified[4] = true;
            	et61 = (EditText) findViewById(R.id.etOptName);
            	s1 = et61.getText().toString();
            	et62 = (EditText) findViewById(R.id.etOptEng);
            	s2 = et62.getText().toString();
            	s2 = s2.replaceAll("\\r", "");
            	s2 = s2.replaceAll("\\n", "%");
            	et63 = (EditText) findViewById(R.id.etOptCh);
            	s3 = et63.getText().toString();
            	s3 = s3.replaceAll("\\r", "");
            	s3 = s3.replaceAll("\\n", "%");
            	linesO[curItem - 1] = s1 + "|" + s2 + "\\" + s3;
            	Global.OPTIONSTXT = saveArray2File(linesO);
            	writeOutFile(Global.OPTIONSTXT, filesLocalName[4]);
    	    	break;
    	    case 5:
    	    	filesModified[5] = true;
            	et71 = (EditText) findViewById(R.id.etExtName);
            	s1 = et71.getText().toString();
            	et72 = (EditText) findViewById(R.id.etExtEng);
            	s2 = et72.getText().toString();
            	s2 = s2.replaceAll("\\r", "");
            	s2 = s2.replaceAll("\\n", "%");
            	et73 = (EditText) findViewById(R.id.etExtCh);
            	s3 = et73.getText().toString();
            	s3 = s3.replaceAll("\\r", "");
            	s3 = s3.replaceAll("\\n", "%");
            	linesE[curItem - 1] = s1 + "|" + s2 + "\\" + s3;
            	Global.EXTRASTXT = saveArray2File(linesE);
            	writeOutFile(Global.EXTRASTXT, filesLocalName[5]);
    	    	break;  	    	
    	    }
    	    adapterLines.notifyDataSetChanged();
        	listLines.smoothScrollToPosition(adapterLines.getSelectedPosition());
	}

	private void clearDetailArea() {
		ttCI = (TextView) findViewById(R.id.col3header);
		ttCI.setText("Choose Item");
		ttCI.setTextColor(Color.parseColor("#FFFFFF"));
		ttCI.setVisibility(View.VISIBLE);
	
		ll1 = (LinearLayout) findViewById(R.id.LinearLayout31);
		ll1.setVisibility(View.GONE);
		ll2 = (LinearLayout) findViewById(R.id.LinearLayout32);
		ll2.setVisibility(View.GONE);
		ll3 = (LinearLayout) findViewById(R.id.LinearLayout33);
		ll3.setVisibility(View.GONE);
		ll5 = (LinearLayout) findViewById(R.id.LinearLayout35);
		ll5.setVisibility(View.GONE);
		ll6 = (LinearLayout) findViewById(R.id.LinearLayout36);
		ll6.setVisibility(View.GONE);
		ll7 = (LinearLayout) findViewById(R.id.LinearLayout37);
		ll7.setVisibility(View.GONE);
		
		// could have been delete or add, so rebuild the catIdList
		setCatIdList();
		
		// clear the MoveUp/MoveDown buttons
	    butMoveDown.setVisibility(View.INVISIBLE);
	    butMoveUp.setVisibility(View.INVISIBLE);
	}
	
	private void removeChangeListeners() {
    	if ((watcher11 != null)) et11.removeTextChangedListener(watcher11);
    	if ((watcher12 != null)) et12.removeTextChangedListener(watcher12);
    	if ((watcher13 != null)) et13.removeTextChangedListener(watcher13);
    	if ((watcher14 != null)) et14.removeTextChangedListener(watcher14);
    	if ((watcher15 != null)) et15.removeTextChangedListener(watcher15);
    	if ((watcher16 != null)) et16.removeTextChangedListener(watcher16);
    	if ((watcher17 != null)) et17.removeTextChangedListener(watcher17);
    	if ((watcher21 != null)) et21.removeTextChangedListener(watcher21);
    	if ((watcher22 != null)) et22.removeTextChangedListener(watcher22);
    	if ((watcher23 != null)) et23.removeTextChangedListener(watcher23);
    	if ((watcher31 != null)) et31.removeTextChangedListener(watcher31);
    	if ((watcher32 != null)) et32.removeTextChangedListener(watcher32);
    	if ((watcher51 != null)) et51.removeTextChangedListener(watcher51);
    	if ((watcher61 != null)) et61.removeTextChangedListener(watcher61);
    	if ((watcher62 != null)) et62.removeTextChangedListener(watcher62);
    	if ((watcher63 != null)) et63.removeTextChangedListener(watcher63);
    	if ((watcher71 != null)) et71.removeTextChangedListener(watcher71);
    	if ((watcher72 != null)) et72.removeTextChangedListener(watcher72);
    	if ((watcher73 != null)) et73.removeTextChangedListener(watcher73);
	}
    		
	private void setWhiteTitles() {
        tvM = (TextView) findViewById(R.id.txtDishEng);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtDishCh);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtDescEng);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtDescCh);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtPriceEng);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtPriceCh);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtPrice);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtCatName);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtCatEng);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtCatCh);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtKitchenName);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtKitchenReplace);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtSettings);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtOptName);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtOptEng);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtOptCh);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtExtName);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtExtEng);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtExtCh);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        
        tvM = (TextView) findViewById(R.id.txtCat);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtPic);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        
        tvM = (TextView) findViewById(R.id.txtOptions);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        
        tvM = (TextView) findViewById(R.id.txtExtras);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        
        tvM = (TextView) findViewById(R.id.txtUnavail);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtFavorite);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtNewDish);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtHealthy);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtDrink);
        tvM.setTextColor(Color.parseColor("#ffffff"));
        tvM = (TextView) findViewById(R.id.txtCounter);
        tvM.setTextColor(Color.parseColor("#ffffff"));
	}
	
	private void uploadOrange() {
		// if the SAVE button is off, then we are in Edit Mode, not Add New mode, so continue
		if (!butSaveNew.isShown()) {
			butUpload.setVisibility(View.VISIBLE);
			saveChanges();
		}
	}
	
	private String decode1() {
		et11 = (EditText) findViewById(R.id.etDishEng);
		s1 = et11.getText().toString();
		s1 = s1.replaceAll("[^\\p{L}\\p{N}\\s]","");
		//et11.setText(s1);
		et12 = (EditText) findViewById(R.id.etDishCh);
		s2 = et12.getText().toString();
		s2 = s2.replaceAll(",","");
		//et12.setText(s2);
		et13 = (EditText) findViewById(R.id.etDescEng);
		s3 = et13.getText().toString();
		et14 = (EditText) findViewById(R.id.etDescCh);
		s4 = et14.getText().toString();
	
		et15 = (EditText) findViewById(R.id.etPriceEng);
		s5 = et15.getText().toString();
		s5 = s5.replaceAll("\\n", "%");
	
		et16 = (EditText) findViewById(R.id.etPriceCh);
		s6 = et16.getText().toString();
		s6 = s6.replaceAll("\\n", "%");
	
		et17 = (EditText) findViewById(R.id.etPrice);
		s7 = et17.getText().toString();
	
		String pic = picButton.getText().toString();
		String cat = catButton.getText().toString();

		String opt = optButton.getText().toString().trim();
		opt = opt.replaceAll("  ", "%");
		if (opt.length() == 0) opt = "none";
	
		String ext = extButton.getText().toString().trim();
		ext = ext.replaceAll("  ", "%");
		if (ext.length() == 0) ext = "none";
	
		String type1 ="0";
		if (cb1.isChecked()) type1 = "1"; // Popular, Healthy, New
		String type2 ="0";
		if (cb2.isChecked()) type2 = "1";
		String type3 ="0";
		if (cb3.isChecked()) type3 = "1";
		String type4 ="0";
		if (cb4.isChecked()) type4 = "1"; // Inactive
		String type5 ="0";
		if (cb5.isChecked()) type5 = "1"; // Drink
		String type6 ="0";
		if (cb6.isChecked()) type6 = "1"; // Counter Only
	
		return type1 + type2 + type3 + type4 + type5 + type6 + "|" +
						 cat + "|"  + 
					     s1  + "\\" + s2 + "|" + 
					     pic + "|"  + 
					     s3  + "\\" + s4 + "|" +
					     s5  + "\\" + s6 + "|" +
					     s7  + "|"  +
					     opt + "|"  +
					     ext;
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
	        uploadOrange();
	        switch(view.getId()){
	            case R.id.etDishEng:
	            	tvM = (TextView) findViewById(R.id.txtDishEng);
	            	tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etDishCh:
	            	tvM = (TextView) findViewById(R.id.txtDishCh);
	            	tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etDescEng:
	                tvM = (TextView) findViewById(R.id.txtDescEng);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etDescCh:    
	                tvM = (TextView) findViewById(R.id.txtDescCh);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etPriceEng:   
	                tvM = (TextView) findViewById(R.id.txtPriceEng);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etPriceCh:
	                tvM = (TextView) findViewById(R.id.txtPriceCh);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etPrice:
	                tvM = (TextView) findViewById(R.id.txtPrice);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etCatName:
	                tvM = (TextView) findViewById(R.id.txtCatName);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etEngName:
	                tvM = (TextView) findViewById(R.id.txtCatEng);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etChName:
	                tvM = (TextView) findViewById(R.id.txtCatCh);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etKitchenName:
	                tvM = (TextView) findViewById(R.id.txtKitchenName);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etKitchenReplace:
	                tvM = (TextView) findViewById(R.id.txtKitchenReplace);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etSettings:
	                tvM = (TextView) findViewById(R.id.txtSettings);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etOptName:
	                tvM = (TextView) findViewById(R.id.txtOptName);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etOptEng:
	                tvM = (TextView) findViewById(R.id.txtOptEng);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etOptCh:
	                tvM = (TextView) findViewById(R.id.txtOptCh);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etExtName:
	                tvM = (TextView) findViewById(R.id.txtExtName);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etExtEng:
	                tvM = (TextView) findViewById(R.id.txtExtEng);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	            case R.id.etExtCh:
	                tvM = (TextView) findViewById(R.id.txtExtCh);
	                tvM.setTextColor(Color.parseColor("#fe7f3d"));
	                break;
	        }
	    }
	}

	private class SelectedAdapter extends BaseAdapter{
		private int selectedPos = -1;	// init value for not-selected
		private String[] items;
		private int type;
		private String[] categoryAll;

		public SelectedAdapter(MenuEditor menuEditor, int textViewResourceId, String[] d, int t) {
			super();
			this.items = d;
			items = d;
			type = t;
			if (type==0) categoryAll = Global.CATEGORYTXT.split("\\n");
			
		}
		public void setSelectedPosition(int pos){
			selectedPos = pos;
			notifyDataSetChanged();
		}
		public int getSelectedPosition(){
			return selectedPos;
		}
		public void unSelect() {
			selectedPos = -1;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    View v = convertView;
		    if (v == null) {
		        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        v = vi.inflate(R.layout.files_item, null);
		    }
		    String backColor = "#00000000";	// normal clear background for the list
            String o = items[position];            

            if (type ==  0) {
		    	String line = items[position];
		    	String[] menuColumns = line.split("\\|");
		    	
		    	String cat;
		    	String catColumns = menuColumns[1];
		    	// look up the category and set the language
		    	int qq = categoryGetIndex(catColumns);
				String line2 = categoryAll[qq];
				String[] lin2Columns = line2.split("\\|");
				String[] lin2Lang = lin2Columns[1].split("\\\\");    	
		    	cat = lin2Lang[0];
		    	
		  	  	// set Top divider if start of Category
		    	TextView tt = (TextView)v.findViewById(R.id.lineTop);
		  	  	if (MenuPosition.contains(position)) {
		  	  		tt.setText(cat);
		  	  		tt.setVisibility(View.VISIBLE);
		  		} else {
		  			tt.setVisibility(View.GONE);
		  		}
		    	
		    	String typeFlags = menuColumns[0];
            	// we want the text after the second divider
            	o = o.substring(o.indexOf("|") +1, o.indexOf("\\"));
            	o = o.substring(o.indexOf("|") +1, o.length());
          	  	if (typeFlags.substring(0,1).equals("1")) backColor = "#b83e3e"; // dish unavailable (RED)
          	  	
          	  	// set up the color bullet indicators
          	  	// \u221A checkmark, \u2022 bullet, \u258B left 5/8 block, \u25FC middle block
          	  	
          	  	TextView b1 = (TextView)v.findViewById(R.id.markFavorite);
            	b1.setTextColor(getResources().getColor(R.color.favorite));
            	b1.setText("\u25FC");
          	  	TextView b2 = (TextView)v.findViewById(R.id.markNewDish);
          	  	b2.setTextColor(getResources().getColor(R.color.newdish));
          	  	b2.setText("\u25FC");
          	  	TextView b3 = (TextView)v.findViewById(R.id.markHealthy);
            	b3.setTextColor(getResources().getColor(R.color.healthy));
            	b3.setText("\u25FC");
          	  	TextView b4 = (TextView)v.findViewById(R.id.markDrink);
            	b4.setTextColor(getResources().getColor(R.color.drink));
            	b4.setText("\u25FC");
          	  	TextView b5 = (TextView)v.findViewById(R.id.markCounter);
            	b5.setTextColor(getResources().getColor(R.color.counter));
            	b5.setText("\u25FC");
          	  	// Favorite
          	  	if (typeFlags.substring(1,2).equals("1")) b1.setVisibility(View.VISIBLE);
          	  	else b1.setVisibility(View.INVISIBLE);
          	  	// New Dish
          	  	if (typeFlags.substring(2,3).equals("1")) b2.setVisibility(View.VISIBLE);
          	  	else b2.setVisibility(View.INVISIBLE);
          	  	// Healthy
          	  	if (typeFlags.substring(3,4).equals("1")) b3.setVisibility(View.VISIBLE);
          	  	else b3.setVisibility(View.INVISIBLE);
          	  	// Drink
          	  	if (typeFlags.substring(4,5).equals("1")) b4.setVisibility(View.VISIBLE);
          	  	else b4.setVisibility(View.INVISIBLE);
          	  	// Counter Only
          	  	if (typeFlags.substring(5,6).equals("1")) b5.setVisibility(View.VISIBLE);
          	  	else b5.setVisibility(View.INVISIBLE);
            }
            
            if (type ==  1) o = o.substring(0, o.indexOf("|"));
            if (type ==  2) o = o.substring(0, o.indexOf("|"));
            if (type ==  4) o = o.substring(0, o.indexOf("|"));
            if (type ==  5) o = o.substring(0, o.indexOf("|"));

            TextView tt = (TextView)v.findViewById(R.id.files_item_title);
            if (tt != null) {
            	tt.setText(o);
            	tt.setTextSize(Utils.getFontSize(MenuEditor.this) - 2);
            	tt.setPadding(2,2,2,2);
            	tt.setSingleLine();
            }
            
	        // change the highlight based on selected state
            LinearLayout ll = (LinearLayout)v.findViewById(R.id.lineLL);
	        if(selectedPos == position){
	        	ll.setBackgroundResource(R.drawable.popupyellowtrans);
	        }else{
	        	ll.setBackgroundResource(R.drawable.popupcleartrans);
	        }
	        
	        // update the background color for inactive dishes
	        tt.setBackgroundColor(Color.parseColor(backColor));
	        
	        return(v);
		}
    	public int getCount() {
            return items.length;
        }

    	public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
	}
	
    // Scan though the optionsItem array, find the str, return the location index
    private int optionsGetIndex(String str) {
    	int found = 0;
    	for(int i=0; i<linesOcol0.size(); i++) {
    		if (str.equalsIgnoreCase(linesOcol0.get(i))) {
    			found = i;
    			break;
    		}
    	}
        return found;
    }
    
    // Scan though the extrasItem array, find the str, return the location index
    private int extrasGetIndex(String str) {
    	int found = 0;
    	for(int i=0; i<linesEcol0.size(); i++) {
    		if (str.equalsIgnoreCase(linesEcol0.get(i))) {
    			found = i;
    			break;
    		}
    	}
        return found;
    }
    
    // Scan though the Category array, find the str, return the location index
    private int categoryGetIndex(String str) {
    	int found = 0;
    	for(int i=0;i<linesC.length;i++) {
    		if (str.equalsIgnoreCase(linesC[i].substring(0, str.length()))) {
    			found = i;
    			break;
    		}
    	}
        return found;
    }
    
    // Scan though the Pic array, find the str, return the location index
    private int picGetIndex(String str) {
    	int found = 0;
    	for(int i=0; i<linesPjpg.size(); i++) {
    		if (str.equalsIgnoreCase(linesPjpg.get(i))) {
    			found = i;
    			break;
    		}
    	}
        return found;
    }
    
    // Scan though the Menu array of dishes from BOTTOM, find the last Category str, return the location index
    private int lastInCat(String str) {
    	int found = -1;
    	for(int i=linesM.length-1; i>0; i--) {
        	String line = linesM[i].trim();
        	String[] menuColumns = line.split("\\|");
        	String cat = menuColumns[1];
        	if (cat.equalsIgnoreCase(str)) {
    			found = i;
    			break;
    		}
    	}
        return found;
    }
    
    // Scan though the Menu array of dishes from TOP, find the first Category str, return the location index
    private int firstInCat(String str) {
    	int found = -1;
    	for(int i=0; i<linesM.length; i++) {
        	String line = linesM[i].trim();
        	String[] menuColumns = line.split("\\|");
        	String cat = menuColumns[1];
        	if (cat.equalsIgnoreCase(str)) {
    			found = i;
    			break;
    		}
    	}
        return found;
    }
    
	protected void showOptDialog() {
		boolean[] checkedOpt = new boolean[linesOcol0.size()];
		int count = linesOcol0.size();
		for(int i = 0; i < count; i++)
			checkedOpt[i] = selectedOpts.contains(linesOcol0.get(i));
			DialogInterface.OnMultiChoiceClickListener optsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(isChecked)
					selectedOpts.add(linesOcol0.get(which));
				else
					selectedOpts.remove(linesOcol0.get(which));
				//if (!butSaveNew.isShown()) {
					tvM = (TextView) findViewById(R.id.txtOptions);
					tvM.setTextColor(Color.parseColor("#fe7f3d"));
				//}
				StringBuilder stringBuilder = new StringBuilder();
				for(CharSequence opt : selectedOpts)
					stringBuilder.append(opt + "  ");
				optButton.setText(stringBuilder.toString());
                uploadOrange();
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Options");
		// need the opts in a string [] to pass in multi
		String[] tmpArr = new String[linesOcol0.size()];
		for (int i = 0; i < linesOcol0.size(); i++) {
		    tmpArr[i] = linesOcol0.get(i);  
		}
		builder.setMultiChoiceItems(tmpArr, checkedOpt, optsDialogListener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	protected void showExtDialog() {
		boolean[] checkedExt = new boolean[linesEcol0.size()];
		int count = linesEcol0.size();
		for(int i = 0; i < count; i++)
			checkedExt[i] = selectedExts.contains(linesEcol0.get(i));
			DialogInterface.OnMultiChoiceClickListener extsDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(isChecked)
					selectedExts.add(linesEcol0.get(which));
				else
					selectedExts.remove(linesEcol0.get(which));
				
				tvM = (TextView) findViewById(R.id.txtExtras);
				tvM.setTextColor(Color.parseColor("#fe7f3d"));
				
				StringBuilder stringBuilder = new StringBuilder();
				for(CharSequence ext : selectedExts)
					stringBuilder.append(ext + "  ");
				extButton.setText(stringBuilder.toString());
                uploadOrange();
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Extras");
		// need the opts in a string [] to pass in multi
		String[] tmpArr = new String[linesEcol0.size()];
		for (int i = 0; i < linesEcol0.size(); i++) {
		    tmpArr[i] = linesEcol0.get(i);  
		}
		builder.setMultiChoiceItems(tmpArr, checkedExt, extsDialogListener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	protected void showCatDialog() {
		DialogInterface.OnClickListener catDialogListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!butSaveNew.isShown()) {
					// not a new entry so we need to move the item to a new category
					dialog.dismiss();
					String str = linesCcol0.get(which);
					catButton.setText(str);	
				
					// this is the place to act on a item CAT change
					// 2 step process, delete and add
					//now do the delete
					String[] newLinesM = new String[linesM.length - 1];
					System.arraycopy(linesM, 0, newLinesM, 0, curItem-1);
					System.arraycopy(linesM, curItem, newLinesM, curItem-1, linesM.length-curItem);
					Global.MENUTXT = saveArray2File(newLinesM);
					linesM = Global.MENUTXT.split("\\n");
					setCatIdList();

					//now do the add
					String newEntry = decode1();
					//get the new CAT and new location for this dish edit
					String cat = catButton.getText().toString();
					int newLoc = lastInCat(cat);
					// if newLoc came back not found (-1), we need to find prior CAT that has a dish and insert after it
					int priorCatIndx = categoryGetIndex(cat) - 1;
					if (priorCatIndx < 0) priorCatIndx = 0;
					String priorCat = linesCcol0.get(priorCatIndx);
					while (newLoc == -1) {    	    			
						newLoc = lastInCat(priorCat);
						priorCatIndx = categoryGetIndex(priorCat) - 1;
						if (priorCatIndx < 0) priorCatIndx = 0;
						priorCat = linesCcol0.get(priorCatIndx);
					}
					newLoc = newLoc +1;		// this is the new location
	    		
					String[] newLinesM2 = new String[linesM.length + 1];
					System.arraycopy(linesM, 0, newLinesM2, 0, newLoc);
					newLinesM2[newLoc] = newEntry;
					System.arraycopy(linesM, newLoc, newLinesM2, newLoc+1, linesM.length-newLoc);
					Global.MENUTXT = saveArray2File(newLinesM2);
					linesM = Global.MENUTXT.split("\\n");
	    		
					writeOutFile(Global.MENUTXT, filesLocalName[0]);
					clearDetailArea();
					totItems = linesM.length;
					filesModified[0] = true;
	    		
					butUpload.setVisibility(View.VISIBLE);
				
					adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesM, curFile);					
		        	listLines.setAdapter(adapterLines);
		        	adapterLines.setSelectedPosition(newLoc);
		        	listLines.smoothScrollToPosition(newLoc);
		        	adapterLines.unSelect();
				} else {
					// this is a new entry so just save the cat on the button
					dialog.dismiss();
					String str = linesCcol0.get(which);
					catButton.setText(str);	
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Category");
		// need the opts in a string [] to pass in multi
		String[] tmpArr = new String[linesCcol0.size()];
		for (int i = 0; i < linesCcol0.size(); i++) {
		    tmpArr[i] = linesCcol0.get(i);  
		}
		builder.setSingleChoiceItems(tmpArr, checkedCat, catDialogListener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	protected void showPicDialog() {
		DialogInterface.OnClickListener picDialogListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//if (!butSaveNew.isShown()) {
					tvM = (TextView) findViewById(R.id.txtPic);
					tvM.setTextColor(Color.parseColor("#fe7f3d"));
				//}
		    	String str = linesPjpg.get(which);
				picButton.setText(str);	
		    	uploadOrange();
				// Load a thumb of the selected picture
 				ImageView img = (ImageView) findViewById(R.id.ll31img);
				String fetchURL = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + Global.PICBASE200 + str;

				// Lazy load the image with Picasso
				get()
						.load(fetchURL)
						.placeholder(R.drawable.nopic)
						.error(R.drawable.nopic)
						.into(img);
				img.setTag(fetchURL); 
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Picture");
		// need the pics in a string [] to pass in multi
		String[] tmpArr = new String[linesPjpg.size()];
		for (int i = 0; i < linesPjpg.size(); i++) {
		    tmpArr[i] = linesPjpg.get(i);  
		}
		builder.setSingleChoiceItems(tmpArr, checkedPic, picDialogListener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
    private String saveArray2File(String ary[]) {
    	String str = "";
		StringBuilder sb = new StringBuilder();
		for(CharSequence line : ary) {
			line = line.toString().trim();
			sb.append(line);
			sb.append("\r\n");
		}
		str = sb.toString();
        return str;
    }
    
    private void writeOutFile(String fcontent, String fname) {
        File tmpDir = new File(getFilesDir(),"SmartMenuTmp");
        if(!tmpDir.exists()) tmpDir.mkdirs();

		File writeFile = new File(tmpDir, fname);
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeFile, false), "UTF-8"));
			writer.write(fcontent);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			log("writeOutFile e=" +e);
		}
    }
    
    private boolean deleteItem(int ary, int pos) {
    	int len = 0;
    	boolean success = false;
    	String[] newLinesM = new String[linesM.length-1];
    	String[] newLinesC = new String[linesC.length-1];
    	String[] newLinesK = new String[linesK.length-1];
    	String[] newLinesO = new String[linesO.length-1];
    	String[] newLinesE = new String[linesE.length-1];

    	try {
		    switch (ary) {
		    case 0:
		    	len = linesM.length;
		    	System.arraycopy(linesM, 0, newLinesM, 0, pos-1);
		    	System.arraycopy(linesM, pos, newLinesM, pos-1, linesM.length-pos);
	        	Global.MENUTXT = saveArray2File(newLinesM);
	        	writeOutFile(Global.MENUTXT, filesLocalName[0]);
	        	linesM = Global.MENUTXT.split("\\n");
	        	clearDetailArea();
	        	butUpload.setVisibility(View.VISIBLE);
	    		totItems = linesM.length;
	    		filesModified[0] = true;
	    		success = true;
	        	break;
		    case 1:
		    	len = linesC.length;
		    	// see if this Cat is in use, if so, it cant be deleted
		    	if (catIsActive(linesC[pos-1])) {
		        	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
		        	alertDialog.setTitle("Cannot Delete Category");
		        	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
		        	alertDialog.setMessage("This Category is not empty. Please delete its Dishes first.");
		        	alertDialog.setCancelable(false); 
		        	alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
		        		public void onClick(DialogInterface dialog, int which) {
		        		} });
		        	alertDialog.show();
		    	} else {
			    	System.arraycopy(linesC, 0, newLinesC, 0, pos-1);
			    	System.arraycopy(linesC, pos, newLinesC, pos-1, linesC.length-pos);
		        	Global.CATEGORYTXT = saveArray2File(newLinesC);
		        	writeOutFile(Global.CATEGORYTXT, filesLocalName[1]);
		        	linesC = Global.CATEGORYTXT.split("\\n");
		        	clearDetailArea();
		        	
		        	butUpload.setVisibility(View.VISIBLE);
	
		    		totItems = linesC.length;
		    		filesModified[1] = true;
		    		success = true;
		    		
		    		// rebuild the Cat spinner list
		    		setCatIdList();

		    	}
		    	break;
		    case 2:
		    	len = linesK.length;
		    	System.arraycopy(linesK, 0, newLinesK, 0, pos-1);
		    	System.arraycopy(linesK, pos, newLinesK, pos-1, linesK.length-pos);
	        	Global.KITCHENTXT = saveArray2File(newLinesK);
	        	writeOutFile(Global.KITCHENTXT, filesLocalName[2]);
	        	linesK = Global.KITCHENTXT.split("\\n");
	        	clearDetailArea();
	        	butUpload.setVisibility(View.VISIBLE);
	
	    		totItems = linesK.length;
	    		filesModified[2] = true;
	    		success = true;
		    	break;
		    case 3:
		    	break;
		    case 4:
		    	len = linesO.length;
		    	// see if this Opt is in use, if so, it cant be deleted
		    	if (optInUse(linesO[pos-1])) {
		        	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
		        	alertDialog.setTitle("Cannot Delete Option");
		        	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
		        	alertDialog.setMessage("This Option is used in the menu. Please update its Dishes first.");
		        	alertDialog.setCancelable(false); 
		        	alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
		        		public void onClick(DialogInterface dialog, int which) {
		        		} });
		        	alertDialog.show();
		    	} else {
			    	System.arraycopy(linesO, 0, newLinesO, 0, pos-1);
			    	System.arraycopy(linesO, pos, newLinesO, pos-1, linesO.length-pos);
		        	Global.OPTIONSTXT = saveArray2File(newLinesO);
		        	writeOutFile(Global.OPTIONSTXT, filesLocalName[4]);
		        	linesO = Global.OPTIONSTXT.split("\\n");
		        	clearDetailArea();
		        	
		        	butUpload.setVisibility(View.VISIBLE);
	
		    		totItems = linesO.length;
		    		filesModified[4] = true;
		    		success = true;
		    		
		    		// rebuild the Opt list
		    		newOptionsList();
		    	}
		    	break;
		    case 5:
		    	len = linesE.length;
		    	// see if this Ext is in use, if so, it cant be deleted
		    	if (extInUse(linesE[pos-1])) {
		        	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
		        	alertDialog.setTitle("Cannot Delete Extra");
		        	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
		        	alertDialog.setMessage("This Extra is used in the menu. Please update its Dishes first.");
		        	alertDialog.setCancelable(false); 
		        	alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
		        		public void onClick(DialogInterface dialog, int which) {
		        		} });
		        	alertDialog.show();
		    	} else {
			    	System.arraycopy(linesE, 0, newLinesE, 0, pos-1);
			    	System.arraycopy(linesE, pos, newLinesE, pos-1, linesE.length-pos);
		        	Global.EXTRASTXT = saveArray2File(newLinesE);
		        	writeOutFile(Global.EXTRASTXT, filesLocalName[5]);
		        	linesE = Global.EXTRASTXT.split("\\n");
		        	clearDetailArea();
		        	
		        	butUpload.setVisibility(View.VISIBLE);
	
		    		totItems = linesE.length;
		    		filesModified[5] = true;
		    		success = true;
		    		
		    		// rebuild the Ext list
		    		newExtrasList();
		    	}
		    	break;
		    }
		} catch (Exception e) {
			//	log("Delete Item Ex=" +e);
			success = false;
		}    
		return success;
    }
    
    private void displayNewItem(int ary, int pos) {
    	int len = 0;
	    vf.setDisplayedChild(ary);
	    
	    switch (ary) {
	    
	    case 0:
	    	len = linesM.length;
	    	//Toast.makeText(MenuEditor.this, "add=0, pos=" + pos + " len=" + len, Toast.LENGTH_SHORT).show();
	    	
        	et11 = (EditText) findViewById(R.id.etDishEng);
        	et11.setText("");
        	et12 = (EditText) findViewById(R.id.etDishCh);
        	et12.setText("");
        	
        	et13 = (EditText) findViewById(R.id.etDescEng);
        	et13.setText("");
        	et14 = (EditText) findViewById(R.id.etDescCh);
        	et14.setText("");
        	
        	et15 = (EditText) findViewById(R.id.etPriceEng);
        	et15.setText("");            	
        	et16 = (EditText) findViewById(R.id.etPriceCh);
        	et16.setText("");            	
        	et17 = (EditText) findViewById(R.id.etPrice);
        	et17.setText("");  
        	
        	cb1 = (CheckBox) findViewById(R.id.checkUnavail);
        	cb1.setChecked(false);
        	cb2 = (CheckBox) findViewById(R.id.checkFavorite);
        	cb2.setChecked(false);
        	cb3 = (CheckBox) findViewById(R.id.checkNewDish);
        	cb3.setChecked(false);
        	cb4 = (CheckBox) findViewById(R.id.checkHealthy);
        	cb4.setChecked(false);
        	cb5 = (CheckBox) findViewById(R.id.checkDrink);
        	cb5.setChecked(false);
        	cb6 = (CheckBox) findViewById(R.id.checkCounter);
        	cb6.setChecked(false);
        	
        	// populate the buttons and spinners
    		optButton = (Button) findViewById(R.id.butOptions);
    	    optButton.setOnClickListener(new OnClickListener() {
    	    	  public void onClick(View v) {
    	    		  showOptDialog();
    	    	  }
    	    });
        	
    		extButton = (Button) findViewById(R.id.butExtras);
    	    extButton.setOnClickListener(new OnClickListener() {
    	    	  public void onClick(View v) {
    	    		  showExtDialog();
    	    	  }
    	    });
    	    
    		catButton = (Button) findViewById(R.id.spinnerCat);
    	    catButton.setOnClickListener(new OnClickListener() {
    	    	  public void onClick(View v) {
    	    		  showCatDialog();
    	    	  }
    	    });
    	    
    		picButton = (Button) findViewById(R.id.spinnerPic);
    	    picButton.setOnClickListener(new OnClickListener() {
    	    	  public void onClick(View v) {
    	    		  showPicDialog();
    	    	  }
    	    });
	    	String str = linesCcol0.get(0);
			catButton.setText(str);	
        	checkedCat = 0;
			
	    	str = linesPjpg.get(0);
			picButton.setText(str);
			checkedPic = 0;
			
			// Load a thumb of the selected picture
			ImageView img = (ImageView) findViewById(R.id.ll31img);
			String fetchURL = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + Global.PICBASE200 + str;

			// Lazy load the image with Picasso
			get()
					.load(fetchURL)
					.placeholder(R.drawable.nopic)
					.error(R.drawable.nopic)
					.into(img);

			img.setTag(fetchURL);  
			
			optButton.setText("none");
			extButton.setText("none");
			selectedOpts.clear();
			selectedExts.clear();
        	
	        setWhiteTitles();
	            	
	        // entering new item so turn off NEW and DEL
	        butDel.setVisibility(View.GONE);
	        butNew.setVisibility(View.INVISIBLE);
	            	
	        int newItem = len + 1;
	        ttCI = (TextView) findViewById(R.id.col3header);
	        ttCI.setText("New Item");
	        ttCI.setTextColor(Color.parseColor("#fe7f3d"));
	            	
	        ll3 = (LinearLayout) findViewById(R.id.LinearLayout31);
	        ll3.setVisibility(View.VISIBLE);
        	break;
	    case 1:
	    	len = linesC.length;
        	et21 = (EditText) findViewById(R.id.etCatName);
        	et21.setText("");
        	et22 = (EditText) findViewById(R.id.etEngName);
        	et22.setText("");
        	et23 = (EditText) findViewById(R.id.etChName);
        	et23.setText("");
        	
	        setWhiteTitles();
	            	
	        // entering new item so turn off NEW and DEL
	        butDel.setVisibility(View.GONE);
			butNew.setVisibility(View.INVISIBLE);
	            	
	        newItem = linesC.length + 1;
	        ttCI = (TextView) findViewById(R.id.col3header);
	        ttCI.setText("New Item " + newItem);
	        ttCI.setTextColor(Color.parseColor("#fe7f3d"));
	            	
	        ll3 = (LinearLayout) findViewById(R.id.LinearLayout32);
	        ll3.setVisibility(View.VISIBLE);
	    	break;
	    case 2:
	    	len = linesK.length;
        	et31 = (EditText) findViewById(R.id.etKitchenName);
        	et31.setText("");
        	et32 = (EditText) findViewById(R.id.etKitchenReplace);
        	et32.setText("");
        	
	        setWhiteTitles();
	            	
	        // entering new item so turn off NEW and DEL
	        butDel.setVisibility(View.GONE);
			butNew.setVisibility(View.INVISIBLE);
	            	
	        newItem = linesK.length + 1;
	        ttCI = (TextView) findViewById(R.id.col3header);
	        ttCI.setText("New Item " + newItem);
	        ttCI.setTextColor(Color.parseColor("#fe7f3d"));
	            	
	        ll3 = (LinearLayout) findViewById(R.id.LinearLayout33);
	        ll3.setVisibility(View.VISIBLE);
	    	break;
	    case 3:
	    	len = linesS.length;
	    	break;
	    case 4:
	    	len = linesO.length;
        	et61 = (EditText) findViewById(R.id.etOptName);
        	et61.setText("");
        	et62 = (EditText) findViewById(R.id.etOptEng);
        	et62.setText("");
        	et63 = (EditText) findViewById(R.id.etOptCh);
        	et63.setText("");
        	
	        setWhiteTitles();

	        // entering new item so turn off NEW and DEL
	        butDel.setVisibility(View.GONE);
			butNew.setVisibility(View.INVISIBLE);

	        newItem = linesO.length + 1;
	        ttCI = (TextView) findViewById(R.id.col3header);
	        ttCI.setText("New Item " + newItem);
	        ttCI.setTextColor(Color.parseColor("#fe7f3d"));
	            	
	        ll3 = (LinearLayout) findViewById(R.id.LinearLayout36);
	        ll3.setVisibility(View.VISIBLE);
	    	break;
	    case 5:
	    	len = linesE.length;
        	et71 = (EditText) findViewById(R.id.etExtName);
        	et71.setText("");
        	et72 = (EditText) findViewById(R.id.etExtEng);
        	et72.setText("");
        	et73 = (EditText) findViewById(R.id.etExtCh);
        	et73.setText("");

	        setWhiteTitles();

	        // entering new item so turn off NEW and DEL
	        butDel.setVisibility(View.GONE);
			butNew.setVisibility(View.INVISIBLE);

	        newItem = linesE.length + 1;
	        ttCI = (TextView) findViewById(R.id.col3header);
	        ttCI.setText("New Item " + newItem);
	        ttCI.setTextColor(Color.parseColor("#fe7f3d"));
	            	
	        ll3 = (LinearLayout) findViewById(R.id.LinearLayout37);
	        ll3.setVisibility(View.VISIBLE);
	    	break;
	    }
    }
    
    BroadcastReceiver wifiStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {            
            infoWifi = "Checking";
            
            SupplicantState supState;
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            supState = wifiInfo.getSupplicantState();
            infoWifi = "" + supState;
            if (supState.equals(SupplicantState.COMPLETED)) {
            	// wifi is up so set the title bar
            	infoWifi = "OK";
            } else {
            	// no wifi so give an update
                if (supState.equals(SupplicantState.SCANNING)) {
                	infoWifi = "Scanning";
                } else if (supState.equals(SupplicantState.DISCONNECTED)) {
                	infoWifi = "Not Available";
                } else {
                	infoWifi = "Connecting";
                }
            }
            
        }
    };
    
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
    
    private boolean catIsActive(String catCheck) {
        // loop through the dishes and see if any of them use this Cat
        boolean found = false;
		for (int i = 0; i < linesM.length; i++) {
        	String line = linesM[i].trim();
        	String[] menuColumns = line.split("\\|");
        	String cat = menuColumns[1];
        	if (cat.equalsIgnoreCase(catCheck.substring(0, cat.length()))) found = true;
		}
        if (found) {
            return true;
        } else {
            return false;
        }
    }
 
    private boolean optInUse(String optCheck) {
        // loop through the dishes and see if any of them use this Opt
        boolean found = false;
        String[] optFields = optCheck.split("\\|");
		for (int i = 0; i < linesM.length; i++) {
        	String line = linesM[i].trim();
        	String[] menuColumns = line.split("\\|");
        	String opt = menuColumns[7];
        	// see if opt contains first field (|) of optCheck? returns -1 if not found
        	if (opt.indexOf(optFields[0]) >= 0) found = true;
        	//Log.v("OPT", "opt=" + opt + ",optLEN=" + opt.length() + ",optF0=" + optFields[0] + ",optF0Len=" + optFields[0].length());
		}
        if (found) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean extInUse(String extCheck) {
        // loop through the dishes and see if any of them use this Ext
        boolean found = false;
    	String[] extFields = extCheck.split("\\|");
		for (int i = 0; i < linesM.length; i++) {
        	String line = linesM[i].trim();
        	String[] menuColumns = line.split("\\|");
        	String ext = menuColumns[8];
        	// see if ext contains first field (|) of extCheck? returns -1 if not found
        	if (ext.indexOf(extFields[0]) >= 0) found = true;
        	//Log.v("EXT", "ext=" + ext + ",extLEN=" + ext.length() + ",extF0=" + extFields[0] + ",extF0Len=" + extFields[0].length());
		}
        if (found) {
            return true;
        } else {
            return false;
        }
    }    
    
    private void setCatIdList() {
    	String tmpCat = "";
    	MenuPosition.clear();
    	// Loop through each line and populate the the Menu Positions
    	for(int i=0; i<linesM.length; i++) {
    		// parse each line into columns using the divider character "|"
    		String[] menuColumns = linesM[i].split("\\|");
    		String catColumns = menuColumns[1];
    		// if we have a new category, then add to the linked list
    		if (!tmpCat.equals(catColumns)) {
    			// add the new cat to the linked list
    			MenuPosition.add(i);
    			tmpCat = catColumns;
    		}
		}
    	// Build the ArrayList for the Category spinner
		linesCcol0.clear();
		for(int i=0; i<linesC.length; i++) {
			int start = 0;
			int end = linesC[i].indexOf("|");
			linesCcol0.add(i, linesC[i].substring(start, end));
	    }
    }
    
    private void newOptionsList() {
    	// get the option list for the button
    	linesOcol0.clear();
		for(int i=0; i<linesO.length; i++)
    	{
			int start = 0;
			int end = linesO[i].indexOf("|");
			linesOcol0.add(linesO[i].substring(start, end));
    	}
    }
	
    private void newExtrasList() {
    	// get the extra list for the button
    	linesEcol0.clear();
    	for(int i=0; i<linesE.length; i++)
    	{
    		int start = 0;
    		int end = linesE[i].indexOf("|");
    		linesEcol0.add(linesE[i].substring(start, end));
    	}
    }
	
    private void validationEmpty() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
    	alertDialog.setTitle("Cannot Save New Item");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("One ore more of your entries is empty. Please fill in all entries.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    		} });
    	alertDialog.show();
    }
    
    private void failedAuth0() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
    	alertDialog.setTitle("Connection");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("Data connection not available. Please try again.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    		} });
    	alertDialog.show();
    }
    
    private void failedAuth1() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
    	alertDialog.setTitle("Files Not Uploaded");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("Your changes have not been uploaded. If you exit, changes will be lost.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton2("Back", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    		} });
    	alertDialog.setButton("Exit", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			finish();
    		} });
    	alertDialog.show();
    }
    
    private void failedAuth2() {
    	AlertDialog alertDialog = new AlertDialog.Builder(MenuEditor.this).create();
    	alertDialog.setTitle("Uploading");
    	alertDialog.setIcon(android.R.drawable.stat_sys_warning);
    	alertDialog.setMessage("Uploading not successful. Please try again.");
    	alertDialog.setCancelable(false); 
    	alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    		} });
    	alertDialog.show();
    }
    
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    	curFile = itemPosition;
		
    	clearDetailArea();
    	removeChangeListeners();
    	
    	totItems = 0;
    	if (itemPosition == 0) totItems = linesM.length;
    	if (itemPosition == 1) totItems = linesC.length;
    	if (itemPosition == 2) totItems = linesK.length;
    	if (itemPosition == 3) totItems = linesS.length;
    	if (itemPosition == 4) totItems = linesO.length;
    	if (itemPosition == 5) totItems = linesE.length;
    	
		vf.setDisplayedChild(itemPosition);
    	
    	listLines = (ListView) findViewById(R.id.listB);
    	switch (itemPosition) {
            case 0:
            	//Toast.makeText(MenuEditor.this, "YES=" + itemPosition, Toast.LENGTH_SHORT).show();
            	// they clicked a file so give them the ADD NEW option
    			butNew.setVisibility(View.VISIBLE);
    			butSaveNew.setVisibility(View.INVISIBLE);
    			butDel.setVisibility(View.GONE);
    			
    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesM, curFile);
    	    	listLines.setAdapter(adapterLines);
    	    	listLines.setVisibility(View.VISIBLE);
    	    	
            	et11 = (EditText) findViewById(R.id.etDishEng);
            	et11.setText("");
            	et12 = (EditText) findViewById(R.id.etDishCh);
            	et12.setText("");
            	
            	et13 = (EditText) findViewById(R.id.etDescEng);
            	et13.setText("");
            	et14 = (EditText) findViewById(R.id.etDescCh);
            	et14.setText("");
            	
            	et15 = (EditText) findViewById(R.id.etPriceEng);
            	et15.setText("");            	
            	et16 = (EditText) findViewById(R.id.etPriceCh);
            	et16.setText("");            	
            	et17 = (EditText) findViewById(R.id.etPrice);
            	et17.setText("");  
            	
            	cb1 = (CheckBox) findViewById(R.id.checkUnavail);
            	cb1.setChecked(false);
            	cb2 = (CheckBox) findViewById(R.id.checkFavorite);
            	cb2.setChecked(false);
            	cb3 = (CheckBox) findViewById(R.id.checkNewDish);
            	cb3.setChecked(false);
            	cb4 = (CheckBox) findViewById(R.id.checkHealthy);
            	cb4.setChecked(false);
            	cb5 = (CheckBox) findViewById(R.id.checkDrink);
            	cb5.setChecked(false);
            	cb6 = (CheckBox) findViewById(R.id.checkCounter);
            	cb6.setChecked(false);
            	
            	// give them a color block indicator for the flags
            	TextView ttt = (TextView) findViewById(R.id.txtFavorite);
            	ttt.setText("Favorite");
            	ttt = (TextView) findViewById(R.id.blockFavorite);
            	ttt.setText("\u25A0");
            	ttt.setTextColor(getResources().getColor(R.color.favorite));
            	
            	ttt = (TextView) findViewById(R.id.txtNewDish);
            	ttt.setText("New Dish");
            	ttt = (TextView) findViewById(R.id.blockNewDish);
            	ttt.setText("\u25A0");
            	ttt.setTextColor(getResources().getColor(R.color.newdish));
            	
            	ttt = (TextView) findViewById(R.id.txtHealthy);
            	ttt.setText("Healthy");
            	ttt = (TextView) findViewById(R.id.blockHealthy);
            	ttt.setText("\u25A0");
            	ttt.setTextColor(getResources().getColor(R.color.healthy));
            	
            	ttt = (TextView) findViewById(R.id.txtDrink);
            	ttt.setText("Drink");
            	ttt = (TextView) findViewById(R.id.blockDrink);
            	ttt.setText("\u25A0");
            	ttt.setTextColor(getResources().getColor(R.color.drink));
            	
            	ttt = (TextView) findViewById(R.id.txtCounter);
            	ttt.setText("Print P3 Only");
            	ttt = (TextView) findViewById(R.id.blockCounter);
            	ttt.setText("\u25A0");
            	ttt.setTextColor(getResources().getColor(R.color.counter));
            	
    	        listLines.setOnItemClickListener(new OnItemClickListener() {
    	            public void onItemClick(AdapterView<?> parent, View v, final int pos2, long id) {
    	            	adapterLines.setSelectedPosition(pos2);
    	            	setWhiteTitles();
    	            	
    	            	// they clicked a menu item
    	            	butDel.setVisibility(View.VISIBLE);
    	    			butNew.setVisibility(View.GONE);
    	    			butSaveNew.setVisibility(View.INVISIBLE);
    	            	
    	            	et11 = (EditText) findViewById(R.id.etDishEng);
    	            	et12 = (EditText) findViewById(R.id.etDishCh);
    	            	et13 = (EditText) findViewById(R.id.etDescEng);
    	            	et14 = (EditText) findViewById(R.id.etDescCh);
    	            	et15 = (EditText) findViewById(R.id.etPriceEng);
    	            	et16 = (EditText) findViewById(R.id.etPriceCh);
    	            	et17 = (EditText) findViewById(R.id.etPrice);
    	            	
    	            	removeChangeListeners();
    	            	
    	        	    cb1.setOnClickListener(new OnClickListener() {
    	        	    	public void onClick(View v) {
    	        	    		uploadOrange();
    	        		        tvM = (TextView) findViewById(R.id.txtUnavail);
    	        		        tvM.setTextColor(Color.parseColor("#fe7f3d"));
    	        	    	}
    	        	    });
    	        	    
    	        	    cb2.setOnClickListener(new OnClickListener() {
  	        	    	  	public void onClick(View v) {
  	        	    	  		uploadOrange();
  	        	    	  		tvM = (TextView) findViewById(R.id.txtFavorite);
  	        	    	  		tvM.setTextColor(Color.parseColor("#fe7f3d"));
  	        	    	  	}
    	        	    });
    	        	    
    	        	    cb3.setOnClickListener(new OnClickListener() {
  	        	    	  	public void onClick(View v) {
  	        	    	  		uploadOrange();
  	        	    	  		tvM = (TextView) findViewById(R.id.txtNewDish);
  	        	    	  		tvM.setTextColor(Color.parseColor("#fe7f3d"));
  	        	    	  	}
    	        	    });
    	        	    
    	        	    cb4.setOnClickListener(new OnClickListener() {
  	        	    	  	public void onClick(View v) {
  	        	    	  		uploadOrange();
  	        	    	  		tvM = (TextView) findViewById(R.id.txtHealthy);
  	        	    	  		tvM.setTextColor(Color.parseColor("#fe7f3d"));
  	        	    	  	}
    	        	    });
    	        	    
    	        	    cb5.setOnClickListener(new OnClickListener() {
  	        	    	  	public void onClick(View v) {
  	        	    	  		uploadOrange();
  	        	    	  		tvM = (TextView) findViewById(R.id.txtDrink);
  	        	    	  		tvM.setTextColor(Color.parseColor("#fe7f3d"));
  	        	    	  	}
    	        	    });
    	        	    
    	        	    cb6.setOnClickListener(new OnClickListener() {
  	        	    	  	public void onClick(View v) {
  	        	    	  		uploadOrange();
  	        	    	  		tvM = (TextView) findViewById(R.id.txtCounter);
  	        	    	  		tvM.setTextColor(Color.parseColor("#fe7f3d"));
  	        	    	  	}
    	        	    });
    	        	    
    	        		optButton = (Button) findViewById(R.id.butOptions);
    	        	    optButton.setOnClickListener(new OnClickListener() {
    	        	    	  public void onClick(View v) {
    	        	    		  showOptDialog();
    	        	    	  }
    	        	    });
    	            	
    	        		extButton = (Button) findViewById(R.id.butExtras);
    	        	    extButton.setOnClickListener(new OnClickListener() {
    	        	    	  public void onClick(View v) {
    	        	    		  showExtDialog();
    	        	    	  }
    	        	    });
    	        	    
    	        		catButton = (Button) findViewById(R.id.spinnerCat);
    	        	    catButton.setOnClickListener(new OnClickListener() {
    	        	    	  public void onClick(View v) {
    	        	    		  showCatDialog();
    	        	    	  }
    	        	    });
    	        	    
    	        		picButton = (Button) findViewById(R.id.spinnerPic);
    	        	    picButton.setOnClickListener(new OnClickListener() {
    	        	    	  public void onClick(View v) {
    	        	    		  showPicDialog();
    	        	    	  }
    	        	    });
    	            	
    	            	String line = linesM[pos2].trim();
    	            	String[] menuColumns = line.split("\\|");
    	            	
    	            	String type = menuColumns[0];
    	            	String[] menuDish = menuColumns[2].split("\\\\");
    	            	String[] menuDesc = menuColumns[4].split("\\\\");
    	            	String[] menuPrice = menuColumns[5].split("\\\\");
    	            	
    	            	// set up the UnAvailable, Favorite, NewDish, Healthy, Drink, Counter checks
    	            	if (type.substring(0,1).equalsIgnoreCase("0")) {
    	            		cb1.setChecked(false);
    	            	} else {
    	            		cb1.setChecked(true);
    	            	}
    	            	if (type.substring(1,2).equalsIgnoreCase("0")) {
    	            		cb2.setChecked(false);
    	            	} else {
    	            		cb2.setChecked(true);
    	            	}
    	            	if (type.substring(2,3).equalsIgnoreCase("0")) {
    	            		cb3.setChecked(false);
    	            	} else {
    	            		cb3.setChecked(true);
    	            	}
    	            	if (type.substring(3,4).equalsIgnoreCase("0")) {
    	            		cb4.setChecked(false);
    	            	} else {
    	            		cb4.setChecked(true);
    	            	}
    	            	if (type.substring(4,5).equalsIgnoreCase("0")) {
    	            		cb5.setChecked(false);
    	            	} else {
    	            		cb5.setChecked(true);
    	            	}
    	            	if (type.substring(5,6).equalsIgnoreCase("0")) {
    	            		cb6.setChecked(false);
    	            	} else {
    	            		cb6.setChecked(true);
    	            	}
    	            	
    	            	int cat = categoryGetIndex(menuColumns[1]);
    	            	checkedCat = cat;
    			    	String str = linesCcol0.get(cat);
    					catButton.setText(str);
    	            	
    	            	int pic = picGetIndex(menuColumns[3]);
    	            	checkedPic = pic;
    			    	str = linesPjpg.get(pic);
    					picButton.setText(str);
    					
    					// Load a thumb of the selected picture
   	  					ImageView img = (ImageView) findViewById(R.id.ll31img);
   						String fetchURL = Global.ProtocolPrefix + Global.ServerIP + "/" + Global.SMID + "/" + Global.PICBASE200 + str;

						// Lazy load the image with Picasso
						get()
								.load(fetchURL)
								.placeholder(R.drawable.nopic)
								.error(R.drawable.nopic)
								.into(img);
						img.setTag(fetchURL);
        				
        				// allow them to take a picture if they want a new image
        				Button camBut = (Button) findViewById(R.id.ll31camimg);
        		        camBut.setOnClickListener( new CameraClickHandler() );

    	            	et11.setText(menuDish[0]);
    	            	et12.setText(menuDish[1]);
    	            	et13.setText(menuDesc[0]);
    	            	et14.setText(menuDesc[1]);
    	            	
    	            	String s = menuPrice[0];
    	            	s = s.replace("%", "\n");
    	            	et15.setText(s);
    	            	
    	            	s = menuPrice[1];
    	            	s = s.replace("%", "\n");
    	            	et16.setText(s);

    	            	et17.setText(menuColumns[6]);
    	            	
    	            	// set the selected for the OPTIONS for the dish 
    	            	String oAll = menuColumns[7];
    	            	String[] oCol = oAll.split("%");
    	            	selectedOpts.clear();
    	            	for(int i=0; i<oCol.length; i++) {
    	            		if (!oCol[i].equals("none")) selectedOpts.add(oCol[i]);
    	            	}
    					StringBuilder stringBuilder = new StringBuilder();
    					for(CharSequence opt : selectedOpts)
    						stringBuilder.append(opt + "  ");
    					optButton.setText(stringBuilder.toString());
    	            	
    	            	// set the selected for the EXTRAS for the dish 
    	            	String eAll = menuColumns[8];
    	            	String[] eCol = eAll.split("%");
    	            	selectedExts.clear();
    	            	for(int i=0; i<eCol.length; i++) {
    	            		if (!eCol[i].equals("none")) selectedExts.add(eCol[i]);
    	            	}
    					stringBuilder = new StringBuilder();
    					for(CharSequence ext : selectedExts)
    						stringBuilder.append(ext + "  ");
    					extButton.setText(stringBuilder.toString());
    					
    					// now that all the values are populated, setup the text watchers
    				    watcher11 = new GenericTextWatcher(et11);
    	            	et11.addTextChangedListener(watcher11);
    	            	watcher12 = new GenericTextWatcher(et12);
    	            	et12.addTextChangedListener(watcher12);
    	            	watcher13 = new GenericTextWatcher(et13);
    	            	et13.addTextChangedListener(watcher13);
    	            	watcher14 = new GenericTextWatcher(et14);
    	            	et14.addTextChangedListener(watcher14);
    	            	watcher15 = new GenericTextWatcher(et15);
    	            	et15.addTextChangedListener(watcher15);
    	            	watcher16 = new GenericTextWatcher(et16);
    	            	et16.addTextChangedListener(watcher16);
    	            	watcher17 = new GenericTextWatcher(et17);
    	            	et17.addTextChangedListener(watcher17);
    	            	
    	            	curItem = pos2 + 1;
    	            	ttCI = (TextView) findViewById(R.id.col3header);
    	            	ttCI.setText("Item " + curItem + "/" + totItems);
    	            	ttCI.setTextColor(Color.parseColor("#fe7f3d"));
    	            	
    	            	ll1 = (LinearLayout) findViewById(R.id.LinearLayout31);
    	            	ll1.setVisibility(View.VISIBLE);
    	            	
    	            	updateMoveButtons(0,curItem,totItems);
    	            }
    	        });
            	break;
            case 1:
            	// Category
    			butNew.setVisibility(View.VISIBLE);
    			butSaveNew.setVisibility(View.INVISIBLE);
    			butDel.setVisibility(View.GONE);
    			
    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesC, curFile);
    	    	listLines.setAdapter(adapterLines);
    	    	listLines.setVisibility(View.VISIBLE);
    	    	
            	et21 = (EditText) findViewById(R.id.etCatName);
            	et21.setText("");
            	et22 = (EditText) findViewById(R.id.etEngName);
            	et22.setText("");
            	et23 = (EditText) findViewById(R.id.etChName);
            	et23.setText("");
            	
    	        listLines.setOnItemClickListener(new OnItemClickListener() 
    	        {
    	            public void onItemClick(AdapterView<?> parent, View v, final int pos2, long id) 
    	            {
    	            	adapterLines.setSelectedPosition(pos2);
    	            	setWhiteTitles();
    	            	
    	            	// they clicked a category item
    	            	butDel.setVisibility(View.VISIBLE);
    	    			butNew.setVisibility(View.GONE);
    	    			butSaveNew.setVisibility(View.INVISIBLE);
    	            	 
    	            	et21 = (EditText) findViewById(R.id.etCatName);
    	            	et22 = (EditText) findViewById(R.id.etEngName);
    	            	et23 = (EditText) findViewById(R.id.etChName);
    	            	
    	            	removeChangeListeners();
    	            	
    	            	String line = linesC[pos2];
    	            	String[] catColumns = line.split("\\|");
    	            	String[] catLang = catColumns[1].split("\\\\");

    	            	et21.setText(catColumns[0]);
    	            	et22.setText(catLang[0]);
    	            	et23.setText(catLang[1]);
    	            	
    					// now that all the values are populated, setup the text watchers
    	            	
    	            	// Not allowed to change key value
    				    watcher21 = new GenericTextWatcher(et21);
    	            	et21.addTextChangedListener(watcher21);
    	            	//et21.setEnabled(true);
    	            	
    	            	watcher22 = new GenericTextWatcher(et22);
    	            	et22.addTextChangedListener(watcher22);
    	            	watcher23 = new GenericTextWatcher(et23);
    	            	et23.addTextChangedListener(watcher23);
    	            	
    	            	curItem = pos2 + 1;
    	            	ttCI = (TextView) findViewById(R.id.col3header);
    	            	ttCI.setText("Item " + curItem + "/" + totItems);
    	            	ttCI.setTextColor(Color.parseColor("#fe7f3d"));
    	            	
    	            	ll2 = (LinearLayout) findViewById(R.id.LinearLayout32);
    	            	ll2.setVisibility(View.VISIBLE);
    	            	
    	            	updateMoveButtons(1,curItem,totItems);
    	            }
    	        });
        	    break;
        	case 2:
            	// Kitchen codes
    			butNew.setVisibility(View.VISIBLE);
    			butSaveNew.setVisibility(View.INVISIBLE);
    			butDel.setVisibility(View.GONE);
    			
    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesK, curFile);
    	    	listLines.setAdapter(adapterLines);
    	    	listLines.setVisibility(View.VISIBLE);
    	    	
            	et31 = (EditText) findViewById(R.id.etKitchenName);
            	et31.setText("");
            	et32 = (EditText) findViewById(R.id.etKitchenReplace);
            	et32.setText("");
            	
    	        listLines.setOnItemClickListener(new OnItemClickListener() 
    	        {
    	            public void onItemClick(AdapterView<?> parent, View v, final int pos2, long id) 
    	            {
    	            	adapterLines.setSelectedPosition(pos2);
    	            	setWhiteTitles();
    	            	
    	            	// they clicked a kitchen code
    	            	butDel.setVisibility(View.VISIBLE);
    	    			butNew.setVisibility(View.GONE);
    	    			butSaveNew.setVisibility(View.INVISIBLE);
    	            	
    	            	et31 = (EditText) findViewById(R.id.etKitchenName);
    	            	et32 = (EditText) findViewById(R.id.etKitchenReplace);
    	            	
    	            	removeChangeListeners();
    	            	
    	            	String line = linesK[pos2];
    	            	String[] kLine = line.split("\\|");

    	            	et31.setText(kLine[0]);
    	            	et32.setText(kLine[1]);
    	            	
    					// now that all the values are populated, setup the text watchers
    				    watcher31 = new GenericTextWatcher(et31);
    	            	et31.addTextChangedListener(watcher31);
    	            	watcher32 = new GenericTextWatcher(et32);
    	            	et32.addTextChangedListener(watcher32);
    	            	
    	            	curItem = pos2 + 1;
    	            	ttCI = (TextView) findViewById(R.id.col3header);
    	            	ttCI.setText("Item " + curItem + "/" + totItems);
    	            	ttCI.setTextColor(Color.parseColor("#fe7f3d"));
    	            	
    	            	ll3 = (LinearLayout) findViewById(R.id.LinearLayout33);
    	            	ll3.setVisibility(View.VISIBLE);
    	            }
    	        });
        	    break;
        	case 3:
            	// Settings
    			butNew.setVisibility(View.INVISIBLE);
    			butSaveNew.setVisibility(View.INVISIBLE);
    			butDel.setVisibility(View.GONE);
    			
    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesS, curFile);
    	    	listLines.setAdapter(adapterLines);
    	    	listLines.setVisibility(View.VISIBLE);
    	    	
            	et51 = (EditText) findViewById(R.id.etSettings);
            	et51.setText("");
            	
    	        listLines.setOnItemClickListener(new OnItemClickListener() 
    	        {
    	            public void onItemClick(AdapterView<?> parent, View v, final int pos2, long id) 
    	            {
    	            	adapterLines.setSelectedPosition(pos2);
    	            	setWhiteTitles();
    	            	
    	            	// they clicked on the settings (single line)
    	            	butDel.setVisibility(View.GONE);
    	    			butNew.setVisibility(View.INVISIBLE);
    	    			butSaveNew.setVisibility(View.INVISIBLE);
    	            	
    	            	et51 = (EditText) findViewById(R.id.etSettings);
    	            	et51.setText(linesS[pos2]);
    	            	
    	            	removeChangeListeners();
    	            	
    					// now that all the values are populated, setup the text watchers
    				    watcher51 = new GenericTextWatcher(et51);
    	            	et51.addTextChangedListener(watcher51);
    	            	
    	            	curItem = pos2 + 1;
    	            	ttCI = (TextView) findViewById(R.id.col3header);
    	            	ttCI.setText("Item " + curItem + "/" + totItems);
    	            	ttCI.setTextColor(Color.parseColor("#fe7f3d"));
    	            	
    	            	ll5 = (LinearLayout) findViewById(R.id.LinearLayout35);
    	            	ll5.setVisibility(View.VISIBLE);
    	            }
    	        });
        	    break;
        	case 4:
            	// Dish Options
    			butNew.setVisibility(View.VISIBLE);
    			butSaveNew.setVisibility(View.INVISIBLE);
    			butDel.setVisibility(View.GONE);
    			
    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesO, curFile);
    	    	listLines.setAdapter(adapterLines);
    	    	listLines.setVisibility(View.VISIBLE);
    	    	
            	et61 = (EditText) findViewById(R.id.etOptName);
            	et61.setText("");
            	et62 = (EditText) findViewById(R.id.etOptEng);
            	et62.setText("");
            	et63 = (EditText) findViewById(R.id.etOptCh);
            	et63.setText("");
            	
    	        listLines.setOnItemClickListener(new OnItemClickListener() 
    	        {
    	            public void onItemClick(AdapterView<?> parent, View v, final int pos2, long id) 
    	            {
    	            	adapterLines.setSelectedPosition(pos2);
    	            	setWhiteTitles();
    	            	
    	            	// they clicked a dish Option
    	            	butDel.setVisibility(View.GONE);
    	    			butNew.setVisibility(View.INVISIBLE);
    	    			butSaveNew.setVisibility(View.INVISIBLE);
    	            	
    	            	et61 = (EditText) findViewById(R.id.etOptName);
    	            	et62 = (EditText) findViewById(R.id.etOptEng);
    	            	et63 = (EditText) findViewById(R.id.etOptCh);
    	            	
    	            	removeChangeListeners();
    	            	
    	            	String line = linesO[pos2];
    	            	String[] optColumns = line.split("\\|");
    	            	String[] optLang = optColumns[1].split("\\\\");

    	            	et61.setText(optColumns[0]);
    	            	
    	            	String s = optLang[0];
    	            	s = s.replace("%", "\n");
    	            	et62.setText(s);
    	            	
    	            	s = optLang[1];
    	            	s = s.replace("%", "\n");
    	            	et63.setText(s);
    	            	
    					// now that all the values are populated, setup the text watchers
    	            	
    	            	// Not allowed to change key value
    	            	et61.setEnabled(false);
    				    //watcher61 = new GenericTextWatcher(et61);
    	            	//et61.addTextChangedListener(watcher61);
    	            	
    	            	watcher62 = new GenericTextWatcher(et62);
    	            	et62.addTextChangedListener(watcher62);
    	            	watcher63 = new GenericTextWatcher(et63);
    	            	et63.addTextChangedListener(watcher63);

    	            	curItem = pos2 + 1;
    	            	ttCI = (TextView) findViewById(R.id.col3header);
    	            	ttCI.setText("Item " + curItem + "/" + totItems);
    	            	ttCI.setTextColor(Color.parseColor("#fe7f3d"));
    	            	
    	            	ll6 = (LinearLayout) findViewById(R.id.LinearLayout36);
    	            	ll6.setVisibility(View.VISIBLE);
    	            }
    	        });
        	    break;
        	case 5:
            	// Extras
    			butNew.setVisibility(View.VISIBLE);
    			butSaveNew.setVisibility(View.INVISIBLE);
    			butDel.setVisibility(View.GONE);
    			
    	    	adapterLines = new SelectedAdapter(MenuEditor.this, R.layout.lines_item, linesE, curFile);
    	    	listLines.setAdapter(adapterLines);
    	    	listLines.setVisibility(View.VISIBLE);
    	    	
            	et71 = (EditText) findViewById(R.id.etExtName);
            	et71.setText("");
            	et72 = (EditText) findViewById(R.id.etExtEng);
            	et72.setText("");
            	et73 = (EditText) findViewById(R.id.etExtCh);
            	et73.setText("");
            	
    	        listLines.setOnItemClickListener(new OnItemClickListener() 
    	        {
    	            public void onItemClick(AdapterView<?> parent, View v, final int pos2, long id) 
    	            {
    	            	adapterLines.setSelectedPosition(pos2);
    	            	setWhiteTitles();
    	            	
    	            	// they clicked an Extra line
    	            	butDel.setVisibility(View.VISIBLE);
    	    			butNew.setVisibility(View.GONE);
    	    			butSaveNew.setVisibility(View.INVISIBLE);
    	            	
    	            	et71 = (EditText) findViewById(R.id.etExtName);
    	            	et72 = (EditText) findViewById(R.id.etExtEng);
    	            	et73 = (EditText) findViewById(R.id.etExtCh);
    	            	
    	            	removeChangeListeners();
    	     	            	           	
    	            	String line = linesE[pos2];
    	            	String[] extColumns = line.split("\\|");
    	            	String[] extLang = extColumns[1].split("\\\\");

    	            	et71.setText(extColumns[0]);
    	            	String s = extLang[0];
    	            	s = s.replace("%", "\n");
    	            	et72.setText(s);
    	            	
    	            	s = extLang[1];
    	            	s = s.replace("%", "\n");
    	            	et73.setText(s);
    	            	
    					// now that all the values are populated, setup the text watchers
    	            	
    	            	// Not allowed to change key value
    	            	et71.setEnabled(false);
    				    //watcher71 = new GenericTextWatcher(et71);
    	            	//et71.addTextChangedListener(watcher71);
    	            	
    	            	watcher72 = new GenericTextWatcher(et72);
    	            	et72.addTextChangedListener(watcher72);
    	            	watcher73 = new GenericTextWatcher(et73);
    	            	et73.addTextChangedListener(watcher73);

    	            	curItem = pos2 + 1;
    	            	ttCI = (TextView) findViewById(R.id.col3header);
    	            	ttCI.setText("Item " + curItem + "/" + totItems);
    	            	ttCI.setTextColor(Color.parseColor("#fe7f3d"));
    	            	
    	            	ll7 = (LinearLayout) findViewById(R.id.LinearLayout37);
    	            	ll7.setVisibility(View.VISIBLE);
    	            }
    	        });
    	        break;   	        
    		}

        return true;
    }
    
    private void updateMoveButtons(int type, int curItem, int totItems) {
    	// Only allow type=0 (dishes) to move up or down
    	// Also allow type=1 (whole categories) to move up or down
    	// Within dishes, Up is DISABLED if curItem = FIRST in cat, Down is DISABLED if curItem = LAST in cat
    	// within categories, Specials is a HARD category which cannot be moved		
		if (curItem != -1) {
		    switch (type) {
		    case 0:
				String cat = catButton.getText().toString();
				int last = lastInCat(cat); // 0 rel
				int first = firstInCat(cat); // 0 rel
				int pos = (curItem - 1) - first;
			    butMoveDown.setVisibility(View.INVISIBLE);
			    butMoveUp.setVisibility(View.INVISIBLE);
		    	//Toast.makeText(MenuEditor.this, " cat=" + cat + 
		    	//		                        " last=" + last +
		    	//		                        " first=" + first +
		    	//		                        " pos=" + pos, Toast.LENGTH_SHORT).show();
		    	if (pos>0) butMoveUp.setVisibility(View.VISIBLE);
		    	if (curItem < (last+1)) butMoveDown.setVisibility(View.VISIBLE);
		    	break;
		    case 1:
		    	pos = curItem - 1;
		    	last = linesC.length - 1;
			    butMoveDown.setVisibility(View.INVISIBLE);
			    butMoveUp.setVisibility(View.INVISIBLE);
		    	if (pos>1) butMoveUp.setVisibility(View.VISIBLE);
		    	// Specials CANT move either direction (curItem is "1" relative
		    	if ((pos < last) && (pos > 0)) butMoveDown.setVisibility(View.VISIBLE);
		    	break;
		    case 2:
			    butMoveDown.setVisibility(View.INVISIBLE);
			    butMoveUp.setVisibility(View.INVISIBLE);
		    	break;
		    case 3:
			    butMoveDown.setVisibility(View.INVISIBLE);
			    butMoveUp.setVisibility(View.INVISIBLE);
		    	break;
		    case 4:
			    butMoveDown.setVisibility(View.INVISIBLE);
			    butMoveUp.setVisibility(View.INVISIBLE);
		    	break;
		    case 5:
			    butMoveDown.setVisibility(View.INVISIBLE);
			    butMoveUp.setVisibility(View.INVISIBLE);
		    	break;  	    	
		    }
		}
    }
    
    private void rebuildMenuDishes() {
		//    Scan through the Menu dishes and re-order them based on the Cat List ordering
		//    1.  For each CAT in CAT array list
		//        Grab all the dishes with CAT match and append them to new Menu list
		//    2.  Save new Menu list, local and Global.
		//    3.  Mark the file as changed
		// arraycopy(Object src, int srcPos, Object dest, int destPos, int length) 
	    int j=0;
	    String[] newLinesM = new String[linesM.length];
		for (String cat : linesCcol0) {
	    	// Loop through each menu dish line and populate the the Menu Positions
	    	for(int i=0; i<linesM.length; i++) {
	    		// parse each line into columns using the divider character "|"
	    		String[] menuColumns = linesM[i].split("\\|");
	    		String catColumn = menuColumns[1];
	    		// if we have a new category, then add to the linked list
	    		if (cat.equals(catColumn)) {
	    			// add the new cat to the new menu string array	
					System.arraycopy(linesM, i, newLinesM, j, 1);
					j++;
	    		}
			}
		}
		Global.MENUTXT = saveArray2File(newLinesM);
		writeOutFile(Global.MENUTXT, filesLocalName[0]);
		linesM = Global.MENUTXT.split("\\n");
		filesModified[0] = true;		
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0,0,menu.NONE,"SmartMenu ID");
        MenuItem item0 = menu.getItem(0);
        item0.setIcon(null);
        item0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        item0.setTitle(" ID " + "\n" + "   " + Global.SMID + "   ");
    	
        menu.add(0,1,menu.NONE,"Menu Version");
        MenuItem item1 = menu.getItem(1);
        item1.setIcon(null);
        item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        item1.setTitle(" Menu Version " + "\n" + "   " + Global.MenuVersion + "   ");
        
        menu.add(0,2,menu.NONE,"INFO");
        MenuItem item2 = menu.getItem(2);
        item2.setIcon(R.drawable.ic_dialog_info);
        item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        menu.add(0,3,menu.NONE,"EXIT");
        MenuItem item3 = menu.getItem(3);
        item3.setIcon(null);
        item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        item3.setTitle("EXIT");
                
    	return(super.onCreateOptionsMenu(menu));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	if (item.getItemId() == 3) {
    		if (!anyFilesModified()) {
    			finish(); 
    		} else {
    			failedAuth1();
    		}
    		return(true);
    	}
    	if (item.getItemId() == 2) {
    		LayoutInflater factory = LayoutInflater.from(this);            
            final View textEntryView = factory.inflate(R.layout.info_dialog, null);
            
            final CustomDialog customDialog = new CustomDialog(this);
            customDialog.setContentView(textEntryView);
            customDialog.show();
            customDialog.setCancelable(true);
            
            // MJW temp crash for ACRA testing --- add back in the 'customDialog.' later
            TextView tv = (TextView) customDialog.findViewById(R.id.AboutVersion);
            tv.setText("Version number: " + getVersionName());
            tv = (TextView) customDialog.findViewById(R.id.AboutServer);
        	tv.setText("Server: " + Global.ServerIP);
            tv = (TextView) customDialog.findViewById(R.id.AboutPath);
        	tv.setText("SmartMenu ID: " + Global.SMID);
            tv = (TextView) customDialog.findViewById(R.id.AboutMenuver);
        	tv.setText("Menu Version: " + Global.MenuVersion);
        	
            tv = (TextView) customDialog.findViewById(R.id.AboutItems);
        	tv.setText("Total Menu Items: " + linesM.length);
            tv = (TextView) customDialog.findViewById(R.id.AboutCats);
        	tv.setText("Total Menu Categories: " + linesCcol0.size());
            tv = (TextView) customDialog.findViewById(R.id.AboutOpts);
        	tv.setText("Options: " + linesOcol0.size());
            tv = (TextView) customDialog.findViewById(R.id.AboutExts);
        	tv.setText("Extras: " + linesEcol0.size());
        	
            tv = (TextView) customDialog.findViewById(R.id.AboutWifi);
        	tv.setText("Wifi Status: " + infoWifi);
            
    		return(true);
    	}
    	return(super.onOptionsItemSelected(item));
    }
  
	private String getVersionName() {
		String version = "";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			version = "Package name not found";
		}
		return version;
	}

	private int getVersionCode() {
		int version = -1;
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
		}
		return version;
	}
    
    // below is the over ride that will disable the back button
    public void onBackPressed () {
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

    private void jsonSetter(JSONArray array, String key, Object replace) {
		for (int i=0; i<array.length(); i++) {
			try {
				JSONObject obj = array.getJSONObject(i);
				String value = obj.getString("name");
				if (value.equalsIgnoreCase(key)) {
					obj.putOpt("value", replace);
				}
			} catch (JSONException e) {
			}
		}
	}
	
	// Log helper function
	public static void log(String message) {
		log(message, null);
	}
	public static void log(String message, Throwable e) {		
		if (mLog != null) {
			try {
				mLog.println(message);
			} catch (IOException ex) {}
		}		
	}
    
}