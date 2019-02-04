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

import java.util.ArrayList;
import org.json.JSONArray;

public class Global {
	public static String	ServerReturn204 =		"/phpcommon/return204.php";
	public static String	UPLOADER =				"/phpcommon/uploadfile0413.php";
	public static String	LISTER =				"listfiles-a.php";
	
	public static String	PICBASE200 =			"fetch200/";
	public static String	PICBASE800 =			"fetch800/"; 

	// The following 4 settings are needed for initial boot
	public static String	ProtocolPrefix = 		"http://"; // Default to non SSL
	public static String	ServerIP = 				"";
	public static String	ServerIPHint = 			"order.lilysbeijing.com";
	public static String	SMID =					"";
	public static Boolean   CheckAvailability =     false;

	public static String	AdminPin = 		    	"";
	
	public static JSONArray Settings =				null;

	public static String 	MENUTXT = 				"menu text will download into here";
	public static String 	CATEGORYTXT = 			"category text will download into here";
	public static String	KITCHENTXT	=			"kitchen codes will download into here";
	public static String	SETTINGSTXT	=			"about message will download into here";
	public static String	OPTIONSTXT	=			"dish options will download into here";
	public static String	EXTRASTXT	=			"dish extras will download into here";
	public static String	PICLISTTXT	=			"list of pics from the server will download into here";
	
	public static String	MenuVersion =			"";
	
    public static int		NumSpecials =			0; // This will hold the number of specials
	public static int		MenuMaxItems = 			0; // This will hold the number of menu items
	public static int		NumCategory = 			0; // This will hold the number of categories

	public static int		ConnectTimeout =		15000;
	public static int		ReadTimeout =			15000;
	
	public static ArrayList<String> fetchURL200 = new ArrayList<String>(); // strings for lazy loading of the small portrait images	
}