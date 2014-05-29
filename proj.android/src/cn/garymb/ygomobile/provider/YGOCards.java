package cn.garymb.ygomobile.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class YGOCards {

	public static final String AUTHROITY = "cn.garymb.ygomobile.provider";

	public static final String[] COMMON_DATA_PROJECTION = new String[] {
			Datas.ID_ALIAS, Datas.OT, Datas.TYPE, Datas.ATK, Datas.DEF, Datas.LEVEL,
			Datas.RACE, Datas.ATTRIBUTE, Texts.NAME };
	public static final String[] COMMON_DATA_PROJECTION_ID = new String[] {
		"_id", Datas.OT, Datas.TYPE, Datas.ATK, Datas.DEF, Datas.LEVEL,
		Datas.RACE, Datas.ATTRIBUTE, Texts.NAME };

	public static final String[] DETAIL_DATA_PEOJECTION = new String[] {
			Datas.ID_ALIAS, Datas.OT, Datas.TYPE, Datas.ATK, Datas.DEF, Datas.LEVEL,
			Datas.RACE, Datas.ATTRIBUTE, Texts.NAME, Texts.DESC };

	public static final Uri CONTENT_URI = Uri
			.parse("content://cn.garymb.ygomobile.provider/combined");
	
	public static final int COMMON_DATA_PROJECTION_TYPE_INDEX = 2;
	public static final int COMMON_DATA_PROJECTION_RACE_INDEX = 6;
	public static final int COMMON_DATA_PROJECTION_ATTR_INDEX = 7;
	public static final int COMMON_DATA_PROJECTION_NAME_INDEX = 8;

	public static final class Datas implements BaseColumns {

		public static final Uri CONTENT_URI = Uri
				.parse("content://cn.garymb.ygomobile.provider/datas");
		
		public static final String ID_ALIAS = "datas._id as _id";
		/**
		 * TCG or OCG
		 */
		public static final String OT = "ot";

		/**
		 * Card alias.
		 */
		public static final String ALIAS = "alias";

		/**
		 * Card set code.
		 */
		public static final String SETCODE = "setcode";

		/**
		 * Card TYPE: monster or spell or trap.
		 */
		public static final String TYPE = "type";

		/**
		 * Monster ATK.
		 */
		public static final String ATK = "atk";

		/**
		 * Monster DEF.
		 */
		public static final String DEF = "def";

		/**
		 * Monster level/rank.
		 */
		public static final String LEVEL = "level";

		/**
		 * Monster Race.
		 */
		public static final String RACE = "race";

		/**
		 * Monster attribute.
		 */
		public static final String ATTRIBUTE = "attribute";

		/**
		 * Card effect category.
		 */
		public static final String CATEGORY = "category";

	}

	public static final class Texts implements BaseColumns {

		public static final Uri CONTENT_URI = Uri
				.parse("content://cn.garymb.ygomobile.provider/texts");

		/**
		 * Card name.
		 */
		public static final String NAME = "name";

		/**
		 * Card description.
		 */
		public static final String DESC = "desc";

		/**
		 * StrX(additional info).
		 */
		public static final String STR1 = "str1";
		public static final String STR2 = "str2";
		public static final String STR3 = "str3";
		public static final String STR4 = "str4";
		public static final String STR5 = "str5";
		public static final String STR6 = "str6";
		public static final String STR7 = "str7";
		public static final String STR8 = "str8";
		public static final String STR9 = "str9";
		public static final String STR10 = "str10";
		public static final String STR11 = "str11";
		public static final String STR12 = "str12";
		public static final String STR13 = "str13";
		public static final String STR14 = "str14";
		public static final String STR15 = "str15";
		public static final String STR16 = "str16";
	}

}
