package cn.garymb.ygomobile.ygo;

import java.util.ArrayList;
import java.util.List;

import cn.garymb.ygomobile.R;


import android.content.res.Resources;
import android.util.SparseIntArray;

public class YGOArrayStore {
	public static final int TYPE_MONSTER		=	    0x1;        //
	public static final int TYPE_SPELL			=		0x2;		//
	public static final int TYPE_TRAP			=	    0x4;		//
	public static final int TYPE_NORMAL			=		0x10;		//
	public static final int TYPE_EFFECT			=		0x20;		//
	public static final int TYPE_FUSION			=		0x40;		//
	public static final int TYPE_RITUAL			=		0x80;		//
	public static final int TYPE_TRAPMONSTER 	=    	0x100;		//
	public static final int TYPE_SPIRIT			=		0x200;		//
	public static final int TYPE_UNION			=		0x400;		//
	public static final int TYPE_DUAL			=		0x800;		//
	public static final int TYPE_TUNER			=		0x1000;		//
	public static final int TYPE_SYNCHRO		=		0x2000;	    //
	public static final int TYPE_TOKEN			=		0x4000;		//
	public static final int TYPE_QUICKPLAY		=		0x10000;	//
	public static final int TYPE_CONTINUOUS		=		0x20000;	//
	public static final int TYPE_EQUIP			=		0x40000;	//
	public static final int TYPE_FIELD			=		0x80000;	//
	public static final int TYPE_COUNTER		=		0x100000;	//
	public static final int TYPE_FLIP			=		0x200000;	//
	public static final int TYPE_TOON			=		0x400000;	//
	public static final int TYPE_XYZ			=		0x800000;	//
	public static final int TYPE_PENDULUM		=		0x1000000;  //

	public static final int RACE_WARRIOR		=		0x1;			//
	public static final int RACE_SPELLCASTER	=		0x2;			//
	public static final int RACE_FAIRY			=		0x4;			//
	public static final int RACE_FIEND			=		0x8;			//
	public static final int RACE_ZOMBIE			=		0x10;		//
	public static final int RACE_MACHINE		=		0x20;		//
	public static final int RACE_AQUA			=		0x40;		//
	public static final int RACE_PYRO			=		0x80;		//
	public static final int RACE_ROCK			=		0x100;		//
	public static final int RACE_WINDBEAST		=		0x200;		//
	public static final int RACE_PLANT			=		0x400;		//
	public static final int RACE_INSECT			=		0x800;		//
	public static final int RACE_THUNDER		=		0x1000;		//
	public static final int RACE_DRAGON			=		0x2000;		//
	public static final int RACE_BEAST			=		0x4000;		//
	public static final int RACE_BEASTWARRIOR	=		0x8000;		//
	public static final int RACE_DINOSAUR		=		0x10000;		//
	public static final int RACE_FISH			=		0x20000;		//
	public static final int RACE_SEASERPENT		=		0x40000;		//
	public static final int RACE_REPTILE		=		0x80000;		//
	public static final int RACE_PSYCHO			=		0x100000;	//
	public static final int RACE_DEVINE			=		0x200000;	//
	public static final int RACE_CREATORGOD		=		0x400000;	//
	public static final int RACE_PHANTOMDRAGON	=		0x800000;	//
	
	public static final int ATTRIBUTE_EARTH		=		0x01;		//
	public static final int ATTRIBUTE_WATER		=		0x02;		//
	public static final int ATTRIBUTE_FIRE		=		0x04;		//
	public static final int ATTRIBUTE_WIND		=		0x08;		//
	public static final int ATTRIBUTE_LIGHT		=		0x10;		//
	public static final int ATTRIBUTE_DARK		=		0x20;		//
	public static final int ATTRIBUTE_DEVINE	=		0x40;		//
	
	
	public static final int  CARD_LEVEL_MASK = 0xFFFF;
	
	public static final List<SparseIntArray> sTypeMaps = new ArrayList<SparseIntArray>(3);
	
	static {
		SparseIntArray monsterArray = new SparseIntArray();
		//monster card
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_ALL, TYPE_MONSTER);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_NORMAL, TYPE_NORMAL);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_EFFECT, TYPE_EFFECT);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_SYNRO, TYPE_SYNCHRO);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_TUNER, TYPE_TUNER);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_XYZ, TYPE_XYZ);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_FUSION, TYPE_FUSION);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_RITUAL, TYPE_RITUAL);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_SPIRIT, TYPE_SPIRIT);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_FLIP, TYPE_FLIP);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_GEMINI, TYPE_DUAL);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_UNION, TYPE_UNION);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_TOKEN, TYPE_TOKEN);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_TOON, TYPE_TOON);
		monsterArray.append(ICardFilter.CARD_FILTER_TYPE_MONSTER_PENDULUM, TYPE_PENDULUM);
		sTypeMaps.add(monsterArray);
		
		//spell card
		SparseIntArray spellArray = new SparseIntArray();
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_ALL, TYPE_SPELL);
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_NORMAL, TYPE_NORMAL);
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_QUICK, TYPE_QUICKPLAY);
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_CONTINUOUS, TYPE_CONTINUOUS);
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_EQUIP, TYPE_EQUIP);
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_FIELD, TYPE_FIELD);
		spellArray.append(ICardFilter.CARD_FILTER_TYPE_SPELL_RITUAL, TYPE_RITUAL);
		sTypeMaps.add(spellArray);
		
		//trap card
		SparseIntArray trapArray = new SparseIntArray();
		trapArray.append(ICardFilter.CARD_FILTER_TYPE_TRAP_ALL, TYPE_TRAP);
		trapArray.append(ICardFilter.CARD_FILTER_TYPE_TRAP_NOARMAL, TYPE_NORMAL);
		trapArray.append(ICardFilter.CARD_FILTER_TYPE_TRAP_CONTINUOUS, TYPE_CONTINUOUS);
		trapArray.append(ICardFilter.CARD_FILTER_TYPE_TRAP_COUNTER, TYPE_COUNTER);
		sTypeMaps.add(trapArray);
		
		//race
		SparseIntArray raceArray = new SparseIntArray();
		raceArray.append(ICardFilter.CARD_FILTER_RACE_ALL, 0xFFFFFF);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_WARRIOR, RACE_WARRIOR);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_SPELLCASTER, RACE_SPELLCASTER);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_FAIRY, RACE_FAIRY);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_FIEND, RACE_FIEND);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_ZOMBIE, RACE_ZOMBIE);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_MACHINE, RACE_MACHINE);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_AQUA, RACE_AQUA);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_PYRO, RACE_PYRO);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_ROCK, RACE_ROCK);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_WINDBEAST, RACE_WINDBEAST);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_PLANT, RACE_PLANT);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_INSECT, RACE_INSECT);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_THUNDER, RACE_THUNDER);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_DRAGON, RACE_DRAGON);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_BEAST, RACE_BEAST);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_BEASTWARRIOR, RACE_BEASTWARRIOR);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_DINOSAUR, RACE_DINOSAUR);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_FISH, RACE_FISH);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_SEASERPENT, RACE_SEASERPENT);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_REPTILE, RACE_REPTILE);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_PSYCHO, RACE_PSYCHO);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_DEVINE, RACE_DEVINE);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_CREATORGOD, RACE_CREATORGOD);
		raceArray.append(ICardFilter.CARD_FILTER_RACE_PHANTOMDRAGON, RACE_PHANTOMDRAGON);
		sTypeMaps.add(raceArray);
		
		//attribute
		SparseIntArray attrArray = new SparseIntArray();
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_ALL, 0xFF);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_EARTH, ATTRIBUTE_EARTH);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_WATER, ATTRIBUTE_WATER);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_FIRE, ATTRIBUTE_FIRE);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_WIND, ATTRIBUTE_WIND);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_LIGHT, ATTRIBUTE_LIGHT);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_DARK, ATTRIBUTE_DARK);
		attrArray.append(ICardFilter.CARD_FILTER_ATTR_DEVINE, ATTRIBUTE_DEVINE);
		
		sTypeMaps.add(attrArray);
		
	}
	
	private String[] mOTArray;
	
	private String[] mRaceArray;
	
	private String[] mAttrArray;
	
	private String[] mMixCardTypeArray;
	
	private String mUnknown = "???";
	
	public YGOArrayStore(Resources res) {
		mOTArray = res.getStringArray(R.array.card_limit);
		mRaceArray = res.getStringArray(R.array.card_race);
		
		mAttrArray = res.getStringArray(R.array.card_attr);
		
		mMixCardTypeArray = res.getStringArray(R.array.card_mix_type);
	}
	
	public String getCardType(int code) {
		StringBuilder builder = new StringBuilder();
		int filter = 1, i = 0;
		builder.append('[');
		for(; filter != 0x2000000; filter <<= 1, ++i) {
			if((code & filter) > 0) {
				builder.append(mMixCardTypeArray[i]).append('|');
			}
		}
		if (builder.length() > 2) {
			builder.delete(builder.length() - 1, builder.length());
		    builder.append(']');
		} else {
			return mUnknown;
		}
		return builder.toString();
	}
	
	public String getCardRace(int code) {
		int x = 0;
		while (code > 1) {
			code >>= 1;
			x++;
		}
		if (x < mRaceArray.length) {
			return mRaceArray[++x];
		} else {
			return mUnknown;
		}
	}
	
	public String getCardAttr(int code) {
		int x = 0;
		while (code > 1) {
			code >>= 1;
			x++;
		}
		if (x < mAttrArray.length) {
			return mAttrArray[++x];
		} else {
			return mUnknown;
		}
	}
	
	public String getCardOT(int code) {
		if (code < 1 || code >= mOTArray.length) {
			return mUnknown;
		} else {
			return mOTArray[code];
		}
	}
}
