package cn.garymb.ygomobile.ygo;

import java.util.List;

import cn.garymb.ygomobile.provider.YGOCards;

public class YGOCardFilter implements ICardFilter {
	
	private int mTypeIndex;
	
	private int mDetailType;
	
	private int mRaceIndex;
	private int mRace;
	
	private int mAttrIndex;
	private int mAttr;
	
	private int mOT;
	
	private int mAtkMax;
	private int mAtkMin;
	
	private int mDefMax;
	private int mDefMin;
	
	private List<Integer> mLevelList;
	private List<Integer> mEffectList;
	
	public YGOCardFilter() {
		reset();
	}
	
	private void reset() {
		mDetailType = CARD_FILTER_TYPE_ALL;
		mRace = CARD_FILTER_TYPE_ALL;
		mAttr = CARD_FILTER_TYPE_ALL;
		mOT = CARD_FILTER_OT_ALL;
		
		mAtkMax = CARD_FILTER_ATKDEF_DEF;
		mAtkMin = CARD_FILTER_ATKDEF_DEF;
		mDefMax = CARD_FILTER_ATKDEF_DEF;
		mDefMin = CARD_FILTER_ATKDEF_DEF;
		
		mLevelList = null;
		mEffectList = null;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onFilter(int type, int arg1, int arg2, Object obj) {
		switch (type) {
		case CARD_FILTER_TYPE_ALL:
			reset();
			break;
		case CARD_FILTER_MONSTER_TYPE:
		case CARD_FILTER_SPELL_TYPE:
		case CARD_FILTER_TRAP_TYPE:
			mTypeIndex = type;
			mDetailType = arg1;
			break;
		case CARD_FILTER_RACE:
			mRaceIndex = type;
			mRace = arg1;
			break;
		case CARD_FILTER_ATTR:
			mAttrIndex = type;
			mAttr = arg1;
			break;
		case CARD_FILTER_OT:
			mOT = arg1;
			break;
		case CARD_FILTER_ATK:
			mAtkMax = arg2;
			mAtkMin = arg1;
			break;
		case CARD_FILTER_DEF:
			mDefMax = arg2;
			mDefMin = arg1;
			break;
		case CARD_FILTER_LEVEL:
			mLevelList = (List<Integer>) obj;
			break;
		case CARD_FILTER_EFFECT:
			mEffectList = (List<Integer>) obj;
			break;
		default:
			break;
		}
	}
	
	@Override
	public void resetFilter() {
		reset();
	}
	
	@Override
	public String buildSelection() {
		StringBuilder sb = new StringBuilder();
		if (mDetailType != CARD_FILTER_TYPE_ALL) {
			if ((mTypeIndex == CARD_FILTER_TRAP_TYPE && mDetailType == CARD_FILTER_TYPE_TRAP_NOARMAL) ||
					(mTypeIndex == CARD_FILTER_SPELL_TYPE && mDetailType == CARD_FILTER_TYPE_SPELL_NORMAL)) {
				sb.append("(")
				.append(YGOCards.Datas.TYPE)
				.append("=")
				.append(YGOArrayStore.sTypeMaps.get(mTypeIndex).get(
						0)).append(") AND ");
			} else {
				sb.append("(")
						.append(YGOCards.Datas.TYPE)
						.append("&")
						.append(YGOArrayStore.sTypeMaps.get(mTypeIndex).get(
								mDetailType)).append(" > 0) AND ").append("(")
						.append(YGOCards.Datas.TYPE).append("&")
						.append(YGOArrayStore.sTypeMaps.get(mTypeIndex).get(0))
						.append(" > 0) AND ");
			}
		}
		if (mRace != CARD_FILTER_TYPE_ALL) {
			sb.append("(").append(YGOCards.Datas.RACE).append("&")
			.append(YGOArrayStore.sTypeMaps.get(mRaceIndex).get(mRace)).append(" > 0) AND ");
		}
		if (mAttr != CARD_FILTER_TYPE_ALL) {
			sb.append("(").append(YGOCards.Datas.ATTRIBUTE).append("&")
			.append(YGOArrayStore.sTypeMaps.get(mAttrIndex).get(mAttr)).append(" > 0) AND ");
		}
		if (mOT != CARD_FILTER_OT_ALL) {
			sb.append("(").append(YGOCards.Datas.OT).append("=")
			.append(mOT).append(") AND ");
		}
		if (mAtkMax != CARD_FILTER_ATKDEF_DEF && mAtkMin != CARD_FILTER_ATKDEF_DEF) {
			if (mAtkMax == mAtkMin) {
				sb.append("(").append(YGOCards.Datas.ATK).append("=")
				.append(mAtkMax).append(") AND ");
			} else {
				sb.append("(").append(YGOCards.Datas.ATK)
				.append(" BETWEEN ").append(mAtkMin).append(" AND ").append(mAtkMax).append(") AND ");;
			}
		}
		if (mDefMax != CARD_FILTER_ATKDEF_DEF && mDefMin != CARD_FILTER_ATKDEF_DEF) {
			if (mDefMax == mDefMin) {
				sb.append("(").append(YGOCards.Datas.DEF).append("=")
				.append(mDefMax).append(") AND ");
			} else {
				sb.append("(").append(YGOCards.Datas.DEF)
				.append(" BETWEEN ").append(mDefMin).append(" AND ").append(mDefMax).append(") AND ");;
			}
		}
		if (mLevelList != null && mLevelList.size() > 0) {
			int size = mLevelList.size();
			sb.append("(");
			for (int i = 0; i < size; i++) {
				sb.append("(").append(YGOCards.Datas.LEVEL).append("=").append(mLevelList.get(i) + 1).append(") OR "); 
			}
			sb.delete(sb.length() - 4, sb.length());
			sb.append(") AND ");
		}
		if (mEffectList != null && mEffectList.size() > 0) {
			int size = mEffectList.size();
			int effect = 0;
			for (int i = 0; i < size; i++) {
				int filter = 0x1;
				effect |= (filter <<= mEffectList.get(i));
			}
			sb.append("(").append(YGOCards.Datas.CATEGORY).append("&").append(effect).append(">0) AND ");
		}
		if (sb.length() > 5) {
			sb.delete(sb.length() - 5, sb.length());
		}
		return sb.length() == 0 ? null : sb.toString();
	}

}
