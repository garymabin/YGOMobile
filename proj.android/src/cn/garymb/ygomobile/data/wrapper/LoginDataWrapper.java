package cn.garymb.ygomobile.data.wrapper;

import cn.garymb.ygomobile.common.Constants;

import android.os.Bundle;


public class LoginDataWrapper extends BaseDataWrapper {

	public LoginDataWrapper(int requestType) {
		super(requestType);
	}
	
	@Override
	public void parse(StringBuilder out) {
		if ("true".equals(out.toString())) {
			setResult(IBaseWrapper.TASK_STATUS_SUCCESS);
		} else {
			setResult(IBaseWrapper.TASK_STATUS_FAILED);
		}
	}
	
	@Override
	public void setParam(Bundle param) {
		super.setParam(param);
		StringBuilder buider = new StringBuilder();
		String name = param.getString(Constants.BUNDLE_KEY_USER_NAME);
		String pw = param.getString(Constants.BUNDLE_KEY_USER_PW);
		param.remove(Constants.BUNDLE_KEY_USER_PW);
		buider.append(LOGIN_URL).append("name=").append(name).append("&password=").append(pw);
		mUrls.add(buider.toString());
	}

}
