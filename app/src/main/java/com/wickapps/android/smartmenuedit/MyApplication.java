package com.wickapps.android.smartmenuedit;

import org.acra.annotation.ReportsCrashes;
import org.acra.*;

import android.app.Application;

@ReportsCrashes(formKey = "",

		customReportContent = { ReportField.REPORT_ID,
				ReportField.APP_VERSION_CODE,
				ReportField.APP_VERSION_NAME,
				ReportField.PACKAGE_NAME,
				ReportField.PHONE_MODEL,
				ReportField.ANDROID_VERSION,
				ReportField.STACK_TRACE,
				ReportField.TOTAL_MEM_SIZE,
				ReportField.AVAILABLE_MEM_SIZE,
				ReportField.USER_APP_START_DATE,
				ReportField.USER_CRASH_DATE,
				ReportField.LOGCAT,
				ReportField.SHARED_PREFERENCES },

		formUri = "http://order.lilysbeijing.com/phpcommon/crashed1118.php",
		httpMethod = org.acra.sender.HttpSender.Method.POST,
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.msg_crash_text,
		resDialogText = R.string.msg_crash_text,
		resDialogIcon = android.R.drawable.ic_dialog_info,
		resDialogTitle = R.string.msg_crash_title)

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}