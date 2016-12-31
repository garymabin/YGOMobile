package cn.garymb.ygomobile.model.data;

import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;

public class CardImageUrlInfo extends BaseInfo {
	
	public String mZhImgUrl;
	
	public String mZhThumbnailUrl;
	
	public String mEnImgHQUrl;
	
	public String mEnImgLQUrl;

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void fromJSONData(JSONObject data) throws JSONException {
		if (data.has(JSON_KEY_ZH_IMAGE_URL)) {
			mZhImgUrl = data.getString(JSON_KEY_ZH_IMAGE_URL);
		}
		if (data.has(JSON_KEY_ZH_THUMBNAIL_URL)) {
			mZhThumbnailUrl = data.getString(JSON_KEY_ZH_THUMBNAIL_URL);
		}
		if (data.has(JSON_KEY_EN_IMAGE_URL)) {
			mEnImgHQUrl = data.getString(JSON_KEY_EN_IMAGE_URL);
		}
		if (data.has(JSON_KEY_EN_LQ_IMAGE_URL)) {
			mEnImgLQUrl = data.getString(JSON_KEY_EN_LQ_IMAGE_URL);
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mZhImgUrl);
		dest.writeString(mZhThumbnailUrl);
		dest.writeString(mEnImgHQUrl);
		dest.writeString(mEnImgLQUrl);
	}

	@Override
	protected void readFromParcel(Parcel source) {
		this.mZhImgUrl = source.readString();
		this.mZhThumbnailUrl = source.readString();
		this.mEnImgHQUrl = source.readString();
		this.mEnImgLQUrl = source.readString();
	}

	@Override
	public ByteBuffer toByteBuffer(ByteBuffer buffer) {
		return buffer;
	}

	@Override
	public void fromByteBuffer(ByteBuffer buffer) {
	}
	
	public static final Creator<CardImageUrlInfo> CREATOR = new Creator<CardImageUrlInfo>() {

		@Override
		public CardImageUrlInfo createFromParcel(Parcel source) {
			CardImageUrlInfo info = new CardImageUrlInfo();
			info.readFromParcel(source);
			return null;
		}

		@Override
		public CardImageUrlInfo[] newArray(int size) {
			return new CardImageUrlInfo[size];
		}
	};

}
