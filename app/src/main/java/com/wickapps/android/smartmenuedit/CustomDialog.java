package com.wickapps.android.smartmenuedit;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

/** Class Must extends with Dialog */
/** Implement onClickListener to dismiss dialog when OK Button is pressed */

public class CustomDialog extends Dialog implements OnClickListener {
	Button okButton;

	public CustomDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	//	@Override
	public void onClick(View v) {
		/** When OK Button is clicked, dismiss the dialog */
		if (v == okButton)
			dismiss();
	}
}
