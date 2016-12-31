package cn.garymb.ygomobile.widget;

import cn.garymb.ygomobile.R;
import cn.garymb.ygomobile.widget.DonateItemsLayout.ItemSelectListener;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;

public class DonateDialogConfigController extends BaseDialogConfigController implements ItemSelectListener {
	
	private static final String ALIPAY_PACKAGE_NAME = "com.eg.android.AlipayGphone";
	
	public static final int DONATE_METHOD_ALIPAY_WAP = 0x1000;
	public static final int DONATE_METHOD_ALIPAY_MOBILE_APP_INSTALLED = 0x1001;
	public static final int DONATE_METHOD_ALIPAY_MOBILE_APP_NOT_INSTALLED = 0x1002;
	
	private DonateItemsLayout mDonateMethodsLayout;
	
	private int mDonateMethod;
	
	private String mAlipayVersionString;
	
	
	public DonateDialogConfigController(DialogConfigUIBase configUI, View view) {
		super(configUI, view);
		final Context context = configUI.getContext();
		final Resources res = context.getResources();
		mDonateMethodsLayout = (DonateItemsLayout) view.findViewById(R.id.donate_via_panel);
		mDonateMethodsLayout.setItemSelectedListener(this);
		final PackageManager pm = context.getPackageManager();
		final LayoutInflater inflater = LayoutInflater.from(context);
		mAlipayVersionString = checkIfAlipayInstalled(pm);
		if (mAlipayVersionString != null) {
			addDonateMethod(inflater, DONATE_METHOD_ALIPAY_MOBILE_APP_INSTALLED);
			mDonateMethod = DONATE_METHOD_ALIPAY_MOBILE_APP_INSTALLED;
		} else {
			addDonateMethod(inflater, DONATE_METHOD_ALIPAY_MOBILE_APP_NOT_INSTALLED);
			mDonateMethod = DONATE_METHOD_ALIPAY_MOBILE_APP_NOT_INSTALLED;
		}
		addDonateMethod(inflater, DONATE_METHOD_ALIPAY_WAP);
		mDonateMethodsLayout.setCurrentChoice(0);
		mConfigUI.setTitle(R.string.donate);
		mConfigUI.setPositiveButton(res.getString(R.string.donate));
		mConfigUI.setCancelButton(res.getString(R.string.button_cancel));
	}
	
	private String checkIfAlipayInstalled(PackageManager pm) {
		PackageInfo info = null;
		try {
			info = pm.getPackageInfo(ALIPAY_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info != null) {
			return info.versionName; 
		} else {
			return null;
		}
	}

	private void addDonateMethod(LayoutInflater inflater, int code) {
		DonateItem item = (DonateItem) inflater.inflate(R.layout.donate_item, null);
		item.setId(code);
		switch (code) {
		case DONATE_METHOD_ALIPAY_WAP:
			item.setDescription(R.string.support_method_alipay_wap);
			break;
		case DONATE_METHOD_ALIPAY_MOBILE_APP_INSTALLED:
			item.setDescription(R.string.support_method_alipay_mobile_installed);
			break;
		case DONATE_METHOD_ALIPAY_MOBILE_APP_NOT_INSTALLED:
			item.setDescription(R.string.support_method_alipay_mobile_not_installed);
			break;
		default:
			break;
		}
		mDonateMethodsLayout.addView(item);
	}

	@Override
	public void onDonateItemSelected(int id) {
		mDonateMethod = id;
	}
	
	public String getAlipayVersionString() {
		return mAlipayVersionString;
	}
	
	public int getAlipayDonateMethod() {
		return mDonateMethod;
	}

}
