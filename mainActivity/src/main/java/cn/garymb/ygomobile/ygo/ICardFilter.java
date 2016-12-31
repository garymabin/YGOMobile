package cn.garymb.ygomobile.ygo;


public interface ICardFilter {
	
	public static final int CARD_FILTER_MONSTER_TYPE = 0;
	public static final int CARD_FILTER_SPELL_TYPE = 1;
	public static final int CARD_FILTER_TRAP_TYPE = 2;
	public static final int CARD_FILTER_RACE = 3;
	public static final int CARD_FILTER_ATTR = 4;
	public static final int CARD_FILTER_OT = 5;
	public static final int CARD_FILTER_ATK = 6;
	public static final int CARD_FILTER_DEF = 7;
	public static final int CARD_FILTER_LEVEL = 8;
	public static final int CARD_FILTER_EFFECT = 9;
	
	public static final int CARD_FILTER_TYPE_ALL =  0xFFFF; 
	
	public static final int CARD_FILTER_TYPE_MONSTER_ALL = 0x0;
	public static final int CARD_FILTER_TYPE_MONSTER_NORMAL = 0x1;
	public static final int CARD_FILTER_TYPE_MONSTER_EFFECT = 0x2;
	public static final int CARD_FILTER_TYPE_MONSTER_SYNRO = 0x3;
	public static final int CARD_FILTER_TYPE_MONSTER_TUNER = 0x4;
	public static final int CARD_FILTER_TYPE_MONSTER_XYZ = 0x5;
	public static final int CARD_FILTER_TYPE_MONSTER_FUSION = 0x6;
	public static final int CARD_FILTER_TYPE_MONSTER_RITUAL = 0x7;
	public static final int CARD_FILTER_TYPE_MONSTER_SPIRIT = 0x8;
	public static final int CARD_FILTER_TYPE_MONSTER_FLIP = 0x9;
	public static final int CARD_FILTER_TYPE_MONSTER_GEMINI = 0xa;
	public static final int CARD_FILTER_TYPE_MONSTER_UNION = 0xb;
	public static final int CARD_FILTER_TYPE_MONSTER_TOKEN = 0xc;
	public static final int CARD_FILTER_TYPE_MONSTER_TOON = 0xd;
	public static final int CARD_FILTER_TYPE_MONSTER_PENDULUM = 0xe;
	public static final int CARD_FILTER_TYPE_MONSTER_MAX = CARD_FILTER_TYPE_MONSTER_PENDULUM;
	
	
	public static final int CARD_FILTER_TYPE_SPELL_ALL = 0x0;
	public static final int CARD_FILTER_TYPE_SPELL_NORMAL = 0x1;
	public static final int CARD_FILTER_TYPE_SPELL_QUICK = 0x2;
	public static final int CARD_FILTER_TYPE_SPELL_CONTINUOUS = 0x3;
	public static final int CARD_FILTER_TYPE_SPELL_EQUIP = 0x4;
	public static final int CARD_FILTER_TYPE_SPELL_FIELD = 0x5;
	public static final int CARD_FILTER_TYPE_SPELL_RITUAL = 0x6;
	
	public static final int CARD_FILTER_TYPE_TRAP_ALL = 0x0;
	public static final int CARD_FILTER_TYPE_TRAP_NOARMAL = 0x1;
	public static final int CARD_FILTER_TYPE_TRAP_CONTINUOUS = 0x2;
	public static final int CARD_FILTER_TYPE_TRAP_COUNTER = 0x3;
	
	public static final int CARD_FILTER_RACE_ALL = 0x0;
	public static final int CARD_FILTER_RACE_WARRIOR = 0x1;
	public static final int CARD_FILTER_RACE_SPELLCASTER = 0x2;
	public static final int CARD_FILTER_RACE_FAIRY = 0x3;
	public static final int CARD_FILTER_RACE_FIEND = 0x4;
	public static final int CARD_FILTER_RACE_ZOMBIE = 0x5;
	public static final int CARD_FILTER_RACE_MACHINE = 0x6;
	public static final int CARD_FILTER_RACE_AQUA = 0x7;
	public static final int CARD_FILTER_RACE_PYRO = 0x8;
	public static final int CARD_FILTER_RACE_ROCK = 0x9;
	public static final int CARD_FILTER_RACE_WINDBEAST = 0xa;
	public static final int CARD_FILTER_RACE_PLANT = 0xb;
	public static final int CARD_FILTER_RACE_INSECT = 0xc;
	public static final int CARD_FILTER_RACE_THUNDER = 0xd;
	public static final int CARD_FILTER_RACE_DRAGON = 0xe;
	public static final int CARD_FILTER_RACE_BEAST = 0xf;
	public static final int CARD_FILTER_RACE_BEASTWARRIOR = 0x10;
	public static final int CARD_FILTER_RACE_DINOSAUR = 0x11;
	public static final int CARD_FILTER_RACE_FISH = 0x12;
	public static final int CARD_FILTER_RACE_SEASERPENT = 0x13;
	public static final int CARD_FILTER_RACE_REPTILE = 0x14;
	public static final int CARD_FILTER_RACE_PSYCHO = 0x15;
	public static final int CARD_FILTER_RACE_DEVINE = 0x16;
	public static final int CARD_FILTER_RACE_CREATORGOD = 0x17;
	public static final int CARD_FILTER_RACE_PHANTOMDRAGON = 0x18;
	
	public static final int CARD_FILTER_ATTR_ALL = 0x0;
	public static final int CARD_FILTER_ATTR_EARTH = 0x1;
	public static final int CARD_FILTER_ATTR_WATER = 0x2;
	public static final int CARD_FILTER_ATTR_FIRE = 0x3;
	public static final int CARD_FILTER_ATTR_WIND = 0x4;
	public static final int CARD_FILTER_ATTR_LIGHT = 0x5;
	public static final int CARD_FILTER_ATTR_DARK = 0x6;
	public static final int CARD_FILTER_ATTR_DEVINE = 0x7;
	
	public static final int CARD_FILTER_OT_ALL = 0x0;
	
	public static final int CARD_FILTER_ATKDEF_DEF = -1;
	
	
	void onFilter(int type, int arg1, int arg2, Object obj);
	
	void resetFilter();
	
	String buildSelection();
}
