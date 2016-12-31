package cn.garymb.ygomobile.data.wrapper;

import org.json.JSONException;
import org.json.JSONObject;

import cn.garymb.ygomobile.common.Constants;
import cn.garymb.ygomobile.model.data.CardImageUrlInfo;
import cn.garymb.ygomobile.model.data.ResourcesConstants;

public class CardImageUrlWrapper extends MyCardJSONRequestWrapper {

	public CardImageUrlWrapper(int requestType) {
		super(requestType);
		mUrls.add(ResourcesConstants.CARDIMAGE_URL);
	}

	@Override
	protected void handleJSONResult(JSONObject object) throws JSONException {
		CardImageUrlInfo info = new CardImageUrlInfo();
		info.fromJSONData(object);
		getParam().getData().putParcelable(Constants.REQUEST_RESULT_KEY_CARDIMAGE_URL, info);
	}

}
