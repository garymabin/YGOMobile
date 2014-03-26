/*
 * AdvancedSettingsActivity.java
 *
 *  Created on: 2014年3月18日
 *      Author: mabin
 */
package cn.garymb.ygomobile;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author mabin
 *
 */
public class AdvancedSettingsActivity extends Activity implements OnClickListener {
	
	private View mOpenglSettings;
	private TextView mOpenglDescTextView;
	private StaticApplication mApp;
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mApp = (StaticApplication) getApplication();
		setContentView(R.layout.setting_layout);
		initView();
		
	}

	/**
	 * 
	 * @return
	**/
	private void initView() {
		// TODO Auto-generated method stub
		mOpenglSettings = findViewById(R.id.opengl_setting_layout);
		mOpenglSettings.setOnClickListener(this);
		
		mOpenglDescTextView = (TextView) findViewById(R.id.opengl_setting_desc);
		mOpenglDescTextView.setText(getResources().getStringArray(R.array.opengl_detail)[mApp.getOpenglVersion()]);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		showOpenglSettingDialog();
	}
	
	private void showOpenglSettingDialog() {
		Builder builder = new Builder(this);
		android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mOpenglDescTextView.setText(getResources().getStringArray(R.array.opengl_detail)[which]);
				mApp.setOpenglVersion(which);
				Toast.makeText(AdvancedSettingsActivity.this, R.string.restart_hint, Toast.LENGTH_SHORT).show();
			}
		};
		builder.setTitle(R.string.opengl).setItems(R.array.opengl_version, listener).create().show();
	}
}
