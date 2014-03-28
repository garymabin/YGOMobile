/*
 * AdvancedSettingsActivity.java
 *
 *  Created on: 2014年3月18日
 *      Author: mabin
 */
package cn.garymb.ygomobile;

import cn.garymb.ygomobile.core.StaticApplication;
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
	private View mCardQualSettings;
	private TextView mOpenglDescTextView;
	private TextView mCardQualTextView;
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
		
		mCardQualSettings = findViewById(R.id.card_setting_layout);
		mCardQualSettings.setOnClickListener(this);
		
		mCardQualTextView = (TextView) findViewById(R.id.card_setting_desc);
		mCardQualTextView.setText(getResources().getStringArray(R.array.card_quality)[mApp.getCardQuality()]);
		
		mOpenglDescTextView = (TextView) findViewById(R.id.opengl_setting_desc);
		mOpenglDescTextView.setText(getResources().getStringArray(R.array.opengl_detail)[mApp.getOpenglVersion()]);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.opengl_setting_layout) {
			showOpenglSettingDialog();
		} else if (v.getId() == R.id.card_setting_layout) {
			showCardQualitySettingDialog();
		}
	}
	
	/**
	 * 
	 * @return
	**/
	private void showCardQualitySettingDialog() {
		Builder builder = new Builder(this);
		android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mCardQualTextView.setText(getResources().getStringArray(R.array.card_quality)[which]);
				mApp.setCardQuality(which);
				Toast.makeText(AdvancedSettingsActivity.this, R.string.restart_hint, Toast.LENGTH_SHORT).show();
			}
		};
		builder.setTitle(R.string.card_quality).setItems(R.array.card_quality, listener).create().show();
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
