package cn.garymb.ygomobile.provider;
import android.provider.BaseColumns;

public final class YGOImages {
	public static final String[] IMAGE_PEOJECTION = new String[] {
		Images._ID, Images.STATUS };
	
	public static final class Images implements BaseColumns {
		public static final String STATUS = "status";
	}
}
