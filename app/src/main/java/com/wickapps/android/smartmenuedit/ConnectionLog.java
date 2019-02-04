package com.wickapps.android.smartmenuedit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

public class ConnectionLog {
	private String mPath;
	private Writer mWriter;
	private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");

	public ConnectionLog(Context context) throws IOException {
		// Set the directory to save text files, make it External storage so the users can view it on the device
		File logDir = context.getExternalFilesDir("SmartMenuLogs");
		if (!logDir.exists()) {
			logDir.mkdirs();
			// do not allow media scan
			new File(logDir, ".nomedia").createNewFile();
		}
		open(logDir.getAbsolutePath() + "/log");
	}

	public ConnectionLog(String basePath) throws IOException {
		open(basePath);
	}

	protected void open(String basePath) throws IOException {
		File f = new File(basePath + "-" + getTodayString());
		mPath = f.getAbsolutePath();
		mWriter = new BufferedWriter(new FileWriter(mPath,true), 2048);
	}

	private static String getTodayString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-hhmmss");
		return "smartmenu.txt";
	}

	public String getPath() {
		return mPath;
	}

	public void println(String message) throws IOException {
		mWriter.write(TIMESTAMP_FMT.format(new Date()));
		mWriter.write(message);
		mWriter.write('\n');
		mWriter.flush();
	}

	public void close() throws IOException {
		mWriter.close();
	}
}