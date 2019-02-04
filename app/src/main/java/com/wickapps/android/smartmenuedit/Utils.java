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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.util.DisplayMetrics;

import android.app.Activity;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {

    public static String DownloadText(String link) throws Exception {
        InputStream in = null;
        String responseData = "";

        try {
            final URL url = new URL(link);

            OkHttpClient.Builder b = new OkHttpClient.Builder();
            b.readTimeout(Global.ReadTimeout, TimeUnit.MILLISECONDS);
            b.writeTimeout(Global.ReadTimeout, TimeUnit.MILLISECONDS);
            b.connectTimeout(Global.ConnectTimeout, TimeUnit.MILLISECONDS);
            final OkHttpClient client = b.build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            responseData = response.body().string();
        } catch (Exception e1) {
            throw new Exception("Unexpected code " + e1);
        }
        return responseData;
    }

    public static String GetDateTime() {
    	Date dt = new Date();
    	Integer hours = dt.getHours();
    	String formathr = String.format("%02d", hours);
    	Integer minutes = dt.getMinutes();
    	String formatmin = String.format("%02d", minutes);
    	Integer month = dt.getMonth() + 1;
    	String formatmon = String.format("%02d", month);
    	Integer day = dt.getDate();
    	String formatdy = String.format("%02d", day);
    	String curTime = formatmon + formatdy + "-" + formathr + formatmin;
    	return curTime;
    }
    
    public static String FancyDate() {
    	String[] daysofweek = new String[] {"Sun","Mon","Tue","Wed","Thur","Fri","Sat"};
    	String[] months = new String[] {"Jan","Feb","Mar","April","May","June","July","Aug","Sept","Oct","Nov","Dec"};
    	String[] daysuffix = new String[] {"st","nd","rd","th","th","th","th","th","th","th","th","th","th","th","th","th","th","th","th","th","st","nd","rd","th","th","th","th","th","th","th","st"};
    	Date dt = new Date();
    	Integer dow = dt.getDay();
    	String formatdow = daysofweek[dow];
    	Integer month = dt.getMonth();
    	String formatmon = months[month];
    	Integer day = dt.getDate();
    	String formatdy = String.format("%2d", day);
    	String curDate = formatdow + ", " + formatmon + " " + formatdy + daysuffix[day-1];
    	return curDate;
    }

	public static int Uploader(File file, String fname, String fpath) throws Exception {
		int statusCode = -1;
		final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");
		final URL url = new URL(Global.ProtocolPrefix + Global.ServerIP + Global.UPLOADER);

		OkHttpClient.Builder b = new OkHttpClient.Builder();
		b.readTimeout(Global.ReadTimeout, TimeUnit.MILLISECONDS);
		b.writeTimeout(Global.ReadTimeout, TimeUnit.MILLISECONDS);
		b.connectTimeout(Global.ConnectTimeout, TimeUnit.MILLISECONDS);
		final OkHttpClient client = b.build();

		RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("filename", fname)
				.addFormDataPart("MAX_FILE_SIZE","300000")
				.addFormDataPart("filepath", fpath)
				.addFormDataPart("uploadedfile", fname,
						RequestBody.create(MEDIA_TYPE_TEXT, file))
				.build();

		Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.build();

		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			// Successful Upload
			statusCode = response.code();
		}
		return statusCode;
	}

    /**
     * Generate a random integer in the range [lowEnd...highEnd].
     *
     * @param highEnd the high end of the range of possible number
     * @return a random integer in [0...highEnd]
     */
    public static int randomInt(int highEnd) {
        int theNum;
        // Pick a random number in the range
        // then truncate it to an integer
        Random r = new Random();
        theNum = r.nextInt( highEnd + 1);
        return theNum;
    }  
    
    public static int getDPI(int size, DisplayMetrics metrics){
        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;        
    }
    
    public static int getFontSize (Activity activity) { 
    DisplayMetrics dMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);

    switch(dMetrics.densityDpi){
    case DisplayMetrics.DENSITY_HIGH:
    	// Lenovo
        return 16;
    case DisplayMetrics.DENSITY_MEDIUM:
    	// Cube, Kindle, Archos
        return 18;
    case DisplayMetrics.DENSITY_LOW:
        // ICS Buy-Now Newest
    	return 20;
    }
    // Unknown
    return 18;
    }
    
    public static int getPicHeight (Activity activity) { 
    DisplayMetrics dMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);

    switch(dMetrics.densityDpi){
    case DisplayMetrics.DENSITY_HIGH:
    	// Lenovo
        return 550;
    case DisplayMetrics.DENSITY_MEDIUM:
    	// Cube 500, Kindle 500, Archos 465
        return 465;
    case DisplayMetrics.DENSITY_LOW:
        // ICS Buy Now Newest
    	return 450;
    }
    // Unknown
    return 420;
    } 
    
    public static int getGridHeight (Activity activity) { 
    DisplayMetrics dMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);

    switch(dMetrics.densityDpi){
    case DisplayMetrics.DENSITY_HIGH:
    	// Lenovo
        return 175;
    case DisplayMetrics.DENSITY_MEDIUM:
    	// Cube 500, Kindle 500, Archos 465
        return 175;
    case DisplayMetrics.DENSITY_LOW:
        // ICS Buy Now Newest
    	return 175;
    }
    // Unknown
    return 175;
    } 
    
    public static int getLineHeight (Activity activity) { 
    DisplayMetrics dMetrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);

    switch(dMetrics.densityDpi){
    case DisplayMetrics.DENSITY_HIGH:
    	// Lenovo
        return 27;
    case DisplayMetrics.DENSITY_MEDIUM:
    	// Cube 500, Kindle 500, Archos 465
        return 24;
    case DisplayMetrics.DENSITY_LOW:
        // ICS Buy Now Newest
    	return 27;
    }
    // Unknown
    return 24;
    } 
    
    public static boolean deleteDirectory(File path) {
    	if (path.exists()) {
    		File[] files = path.listFiles();
    		if (files == null) {
    			return true;
    		}
    		if (files != null) {
    			for (int i=0; i<files.length; i++) {
    				if (files[i].isDirectory()) {
    					deleteDirectory(files[i]);
    				}
    				else {
    					files[i].delete();
    				}
    			}
    		}
    	}
    	return(path.delete());
    }
        
    public static String filenameFromURL (String str) {
    	// URL comes in such as: 			http://www.smart.com/files/name.jpg
    	// string s gets returned as: 		name.jpg
    	//   http://www.lilysamericandiner.com/fetch200/name.jpg
    	//      .*//                        .*/      .*/ (1).jpg
    	String s = str;
    	String name = str;
    	//s = s.replaceAll("[^\\p{L}\\p{N}.\\/\\-\\+\\!\\$,\\s]", "");
        Pattern p = Pattern.compile("(.*//)(.*/)(.*/)(.*.jpg)");
        Matcher m=p.matcher(s);
        if(m.find()) {
      	  name  = m.group(4).trim();
        }
    	return(name);
    }
    
    public static String removeBOMchar(String tmp) {
        char[] UTF16LE = {0xFF, 0xFE};
        char[] UTF8 = {0xEF, 0xBB, 0xBF};
        String sTemp = tmp;
        //sTemp.replaceAll("^\\xEF\\xBB\\xBF", "");
        sTemp = sTemp.replace("\uFEFF", "");
        // sTemp = sTemp.substring(1, sTemp.length());
        return sTemp;
    }
    
	// This routine will remove all the lines in the files that begin with "//"
	// This allows for easy updating of the menufile.txt and others
    public static String removeCommentLines(String tmp) {
	  String sTemp = tmp;
	  sTemp = sTemp.replaceAll("\\/\\/.*\\r\\n", "");
	  return sTemp;
    }
    
	// This routine will remove all the lines in the files that begin with "1"
	// This allows for removal of unavailable dishes
    public static String removeUnAvailable(String tmp) {
	  String sTemp = tmp;
	  //sTemp = sTemp.replaceAll("^1.*\\r\\n", "");
	  //sTemp = sTemp.replaceAll("(?m)^1.*$", "");
	  //sTemp = sTemp.replaceAll("^1(.*)\\n", "");
	  // (?m) does multiline, and we need the ^ to match the beginning of each line
	  sTemp = sTemp.replaceAll("(?m)^1.....\\|.*\\r\\n", "");
	  return sTemp;
    }
    
}