#include "config.h"
#include "game.h"
#include "image_manager.h"
#include "data_manager.h"
#include "deck_manager.h"
#include "replay.h"
#include "materials.h"
#include "duelclient.h"
#include "netserver.h"
#include "single_mode.h"

#ifndef WIN32
#include <sys/types.h>
#include <dirent.h>
#endif

#ifdef _IRR_ANDROID_PLATFORM_
#include <android/CAndroidGUIEditBox.h>
#include <android/CAndroidGUIComboBox.h>
#include <android/CAndroidGUIListBox.h>
#define ANDROID_IRR_OES_TEXTURE_NPOT 37 //Hard-code here! see COGLES2ExtensionHandler::IRR_OES_texture_npot
#endif

const unsigned short PRO_VERSION = 0x1321;

namespace ygo {

Game* mainGame;
#ifdef _IRR_ANDROID_PLATFORM_
bool Game::Initialize(android_app* app) {
	this->appMain = app;
#else
bool Game::Initialize() {
#endif
	srand(time(0));
	irr::SIrrlichtCreationParameters params = irr::SIrrlichtCreationParameters();
#ifdef _IRR_ANDROID_PLATFORM_
	glversion = android::getOpenglVersion(app);
	if (glversion == 0) {
		params.DriverType = irr::video::EDT_OGLES1;
	} else{
		params.DriverType = irr::video::EDT_OGLES2;
	}
	params.PrivateData = app;
	params.Bits = 24;
	params.ZBufferBits = 16;
	params.AntiAlias  = 0;
	params.WindowSize = irr::core::dimension2d<u32>(0, 0);
#else
	if(gameConf.use_d3d)
		params.DriverType = irr::video::EDT_DIRECT3D9;
	else
		params.DriverType = irr::video::EDT_OPENGL;
	params.WindowSize = irr::core::dimension2d<u32>(1024, 640);
#endif
	device = irr::createDeviceEx(params);
	if(!device)
		return false;
#ifdef _IRR_ANDROID_PLATFORM_
	if (!android::perfromTrick(app)) {
		return false;
	}
	android::initJavaBridge(app, device);
	app->onInputEvent = android::handleInput;
	ILogger* logger = device->getLogger();
	IFileSystem * fs = device->getFileSystem();
	android::SDisplayMetrics metrics;
	android::getDisplayMetrics(app, metrics);
	xScale = metrics.widthPixels / 1024.0;
	yScale = metrics.heightPixels / 640.0;
//	xScale = 800 / 1024.0;
//	yScale = 480 / 640.0;
	char log_scale[256];
	sprintf(log_scale, "xScale = %f, yScale = %f", xScale, yScale);
	Printer::log(log_scale);
	path workPath = irr::android::getCustomizedResourceDir(appMain);
	path pwd = fs->getWorkingDirectory();
	Printer::log(pwd.c_str());
	fs->changeWorkingDirectoryTo(workPath);
	Printer::log(workPath.c_str());
	/* Your media must be somewhere inside the assets folder. The assets folder is the root for the file system.
	 This example copies the media in the Android.mk makefile. */
	stringc mediaPath = "media/";

	// The Android assets file-system does not know which sub-directories it has (blame google).
	// So we have to add all sub-directories in assets manually. Otherwise we could still open the files,
	// but existFile checks will fail (which are for example needed by getFont).
	for ( u32 i=0; i < fs->getFileArchiveCount(); ++i )
	{
		IFileArchive* archive = fs->getFileArchive(i);
		if ( archive->getType() == EFAT_ANDROID_ASSET )
		{
			archive->addDirectoryToFileList(mediaPath);
			break;
		}
	}
#else
	xScale = 1.0;
	yScale = 1.0;
#endif
	LoadConfig();
	linePattern = 0x0f0f;
	waitFrame = 0;
	signalFrame = 0;
	showcard = 0;
	is_attacking = false;
	lpframe = 0;
	lpcstring = 0;
	always_chain = false;
	ignore_chain = false;
	is_building = false;
	memset(&dInfo, 0, sizeof(DuelInfo));
	memset(chatTiming, 0, sizeof(chatTiming));
	deckManager.LoadLFList();
	driver = device->getVideoDriver();
#ifdef _IRR_ANDROID_PLATFORM_
	int quality = android::getCardQuality(app);
	isNPOTSupported = driver->queryFeature((irr::video::E_VIDEO_DRIVER_FEATURE)ANDROID_IRR_OES_TEXTURE_NPOT);
	if (isNPOTSupported) {
		if (quality == 1) {
			driver->setTextureCreationFlag(irr::video::ETCF_CREATE_MIP_MAPS, false);
		} else {
			driver->setTextureCreationFlag(irr::video::ETCF_CREATE_MIP_MAPS, true);
		}
	} else {
		driver->setTextureCreationFlag(irr::video::ETCF_ALLOW_NON_POWER_2, true);
		driver->setTextureCreationFlag(irr::video::ETCF_CREATE_MIP_MAPS, false);
	}
#else
	driver->setTextureCreationFlag(irr::video::ETCF_CREATE_MIP_MAPS, false);
#endif
	driver->setTextureCreationFlag(irr::video::ETCF_OPTIMIZED_FOR_QUALITY, true);

	imageManager.SetDevice(device);
	if(!imageManager.Initial())
		return false;
	if(!dataManager.LoadDB("cards.cdb"))
		return false;
	if(!dataManager.LoadStrings("strings.conf"))
		return false;
	env = device->getGUIEnvironment();
	numFont = irr::gui::CGUITTFont::createTTFont(env, gameConf.numfont, (int)16 * yScale);
	adFont = irr::gui::CGUITTFont::createTTFont(env, gameConf.numfont, (int)12 * yScale);
	lpcFont = irr::gui::CGUITTFont::createTTFont(env, gameConf.numfont, (int)48 * yScale);
	guiFont = irr::gui::CGUITTFont::createTTFont(env, gameConf.textfont, (int)gameConf.textfontsize * yScale);
	textFont = guiFont;
	smgr = device->getSceneManager();
	device->setWindowCaption(L"[---]");
	device->setResizable(false);
	//main menu
	wchar_t strbuf[256];
	myswprintf(strbuf, L"YGOPro Version:%X.0%X.%X", PRO_VERSION >> 12, (PRO_VERSION >> 4) & 0xff, PRO_VERSION & 0xf);
#ifdef _IRR_ANDROID_PLATFORM_
	wMainMenu = env->addWindow(rect<s32>(320 * xScale, 150 * yScale, 700 * xScale, 465 * yScale), false, strbuf);
	wMainMenu->getCloseButton()->setVisible(false);
	btnLanMode = env->addButton(rect<s32>(10 * xScale, 30 * yScale, 370 * xScale, 80 * yScale), wMainMenu, BUTTON_LAN_MODE, dataManager.GetSysString(1200));
	btnServerMode = env->addButton(rect<s32>(10 * xScale, 85 * yScale, 370 * xScale, 135 * yScale), wMainMenu, BUTTON_SINGLE_MODE, dataManager.GetSysString(1201));
	btnReplayMode = env->addButton(rect<s32>(10 * xScale, 140 * yScale, 370 * xScale, 190 * yScale), wMainMenu, BUTTON_REPLAY_MODE, dataManager.GetSysString(1202));
	btnDeckEdit = env->addButton(rect<s32>(10 * xScale, 195 * yScale, 370 * xScale, 245 * yScale), wMainMenu, BUTTON_DECK_EDIT, dataManager.GetSysString(1204));
	btnModeExit = env->addButton(rect<s32>(10 * xScale, 250 * yScale, 370 * xScale, 300 * yScale), wMainMenu, BUTTON_MODE_EXIT, dataManager.GetSysString(1210));

	//lan mode
	wLanWindow = env->addWindow(rect<s32>(200 * xScale, 80 * yScale, 820 * xScale, 590 * yScale), false, dataManager.GetSysString(1200));
	wLanWindow->getCloseButton()->setVisible(false);
	wLanWindow->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1220), rect<s32>(35 * xScale, 40 * yScale, 220 * xScale, 75 * yScale), false, false, wLanWindow);
	ebNickName = CAndroidGUIEditBox::addAndroidEditBox(gameConf.nickname, true, env, rect<s32>(110 * xScale, 25 * yScale, 450 * xScale, 65 * yScale), wLanWindow);
	ebNickName->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	lstHostList = CAndroidGUIListBox::addAndroidGUIListBox(env, rect<s32>(20 * xScale, 75 * yScale, 600 * xScale, 320 * yScale), wLanWindow, LISTBOX_LAN_HOST, true, 40 * xScale);
	lstHostList->setItemHeight(18);
	btnLanRefresh = env->addButton(rect<s32>(250 * xScale, 330 * yScale, 350 * xScale, 370 * yScale), wLanWindow, BUTTON_LAN_REFRESH, dataManager.GetSysString(1217));
	env->addStaticText(dataManager.GetSysString(1221), rect<s32>(35 * xScale, 390 * yScale, 220 * xScale, 410 * yScale), false, false, wLanWindow);
	ebJoinIP = CAndroidGUIEditBox::addAndroidEditBox(gameConf.lastip, true, env, rect<s32>(110 * xScale, 380 * yScale, 270 * xScale, 420 * yScale), wLanWindow);
	ebJoinIP->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	ebJoinPort = CAndroidGUIEditBox::addAndroidEditBox(gameConf.lastport, true, env, rect<s32>(280 * xScale, 380 * yScale, 340 * xScale, 420 * yScale), wLanWindow);
	ebJoinPort->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1222), rect<s32>(35 * xScale, 440 * yScale, 220 * xScale, 460 * yScale), false, false, wLanWindow);
	ebJoinPass = CAndroidGUIEditBox::addAndroidEditBox(gameConf.roompass, true, env, rect<s32>(110 * xScale, 430 * yScale, 250 * xScale, 470 * yScale), wLanWindow);
	ebJoinPass->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnJoinHost = env->addButton(rect<s32>(460 * xScale, 380 * yScale, 590 * xScale, 420 * yScale), wLanWindow, BUTTON_JOIN_HOST, dataManager.GetSysString(1223));
	btnJoinCancel = env->addButton(rect<s32>(460 * xScale, 430 * yScale, 590 * xScale, 470 * yScale), wLanWindow, BUTTON_JOIN_CANCEL, dataManager.GetSysString(1212));
	btnCreateHost = env->addButton(rect<s32>(460 * xScale, 25 * yScale, 590 * xScale, 65 * yScale), wLanWindow, BUTTON_CREATE_HOST, dataManager.GetSysString(1224));
#else
	wMainMenu = env->addWindow(rect<s32>(370 * xScale, 200 * yScale, 650 * xScale, 415 * yScale), false, strbuf);
	wMainMenu->getCloseButton()->setVisible(false);
	btnLanMode = env->addButton(rect<s32>(10 * xScale, 30 * yScale, 270 * xScale, 60 * yScale), wMainMenu, BUTTON_LAN_MODE, dataManager.GetSysString(1200));
	btnServerMode = env->addButton(rect<s32>(10 * xScale, 65 * yScale, 270 * xScale, 95 * yScale), wMainMenu, BUTTON_SINGLE_MODE, dataManager.GetSysString(1201));
	btnReplayMode = env->addButton(rect<s32>(10 * xScale, 100 * yScale, 270 * xScale, 130 * yScale), wMainMenu, BUTTON_REPLAY_MODE, dataManager.GetSysString(1202));
//	btnTestMode = env->addButton(rect<s32>(10, 135, 270, 165), wMainMenu, BUTTON_TEST_MODE, dataManager.GetSysString(1203));
	btnDeckEdit = env->addButton(rect<s32>(10 * xScale, 135 * yScale, 270 * xScale, 165 * yScale), wMainMenu, BUTTON_DECK_EDIT, dataManager.GetSysString(1204));
	btnModeExit = env->addButton(rect<s32>(10 * xScale, 170 * yScale, 270 * xScale, 200 * yScale), wMainMenu, BUTTON_MODE_EXIT, dataManager.GetSysString(1210));
	//lan mode
	wLanWindow = env->addWindow(rect<s32>(220 * xScale, 100 * yScale, 800 * xScale, 520 * yScale), false, dataManager.GetSysString(1200));
	wLanWindow->getCloseButton()->setVisible(false);
	wLanWindow->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1220), rect<s32>(10 * xScale, 30 * yScale, 220 * xScale, 50 * yScale), false, false, wLanWindow);
	ebNickName = env->addEditBox(gameConf.nickname, rect<s32>(110 * xScale, 25 * yScale, 450 * xScale, 50 * yScale), true, wLanWindow);
	lstHostList = env->addListBox(rect<s32>(10 * xScale, 60 * yScale, 570 * xScale, 320 * yScale), wLanWindow, LISTBOX_LAN_HOST, true);
	ebNickName->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	lstHostList->setItemHeight(18);
	btnLanRefresh = env->addButton(rect<s32>(240 * xScale, 325 * yScale, 340 * xScale, 350 * yScale), wLanWindow, BUTTON_LAN_REFRESH, dataManager.GetSysString(1217));
	env->addStaticText(dataManager.GetSysString(1221), rect<s32>(10 * xScale, 360 * yScale, 220 * xScale, 380 * yScale), false, false, wLanWindow);
	ebJoinIP = env->addEditBox(gameConf.lastip, rect<s32>(110 * xScale, 355 * yScale, 250 * xScale, 380 * yScale), true, wLanWindow);
	ebJoinIP->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	ebJoinPort = env->addEditBox(gameConf.lastport, rect<s32>(260 * xScale, 355 * yScale, 320 * xScale, 380 * yScale), true, wLanWindow);
	ebJoinPort->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1222), rect<s32>(10 * xScale, 390 * yScale, 220 * xScale, 410 * yScale), false, false, wLanWindow);
	ebJoinPass = env->addEditBox(gameConf.roompass, rect<s32>(110 * xScale, 385 * yScale, 250 * xScale, 410 * yScale), true, wLanWindow);
	ebJoinPass->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnJoinHost = env->addButton(rect<s32>(460 * xScale, 355 * yScale, 570 * xScale, 380 * yScale), wLanWindow, BUTTON_JOIN_HOST, dataManager.GetSysString(1223));
	btnJoinCancel = env->addButton(rect<s32>(460 * xScale, 385 * yScale, 570 * xScale, 410 * yScale), wLanWindow, BUTTON_JOIN_CANCEL, dataManager.GetSysString(1212));
	btnCreateHost = env->addButton(rect<s32>(460 * xScale, 25 * yScale, 570 * xScale, 50 * yScale), wLanWindow, BUTTON_CREATE_HOST, dataManager.GetSysString(1224));
#endif
	//create host
	wCreateHost = env->addWindow(rect<s32>(320 * xScale, 100 * yScale, 700 * xScale, 520 * yScale), false, dataManager.GetSysString(1224));
	wCreateHost->getCloseButton()->setVisible(false);
	wCreateHost->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1226), rect<s32>(20 * xScale, 30 * yScale, 220 * xScale, 50 * yScale), false, false, wCreateHost);
#ifdef _IRR_ANDROID_PLATFORM_
	cbLFlist = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(140 * xScale, 25 * yScale, 300 * xScale, 50 * yScale), wCreateHost);
#else
	cbLFlist = env->addComboBox(rect<s32>(140 * xScale, 25 * yScale, 300 * xScale, 50 * yScale), wCreateHost);
#endif
	for(unsigned int i = 0; i < deckManager._lfList.size(); ++i)
		cbLFlist->addItem(deckManager._lfList[i].listName, deckManager._lfList[i].hash);
	env->addStaticText(dataManager.GetSysString(1225), rect<s32>(20 * xScale, 60 * yScale, 220 * xScale, 80 * yScale), false, false, wCreateHost);
#ifdef _IRR_ANDROID_PLATFORM_
	cbRule = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(140 * xScale, 55 * yScale, 300 * xScale, 80 * yScale), wCreateHost);
#else
	cbRule = env->addComboBox(rect<s32>(140 * xScale, 55 * yScale, 300 * xScale, 80 * yScale), wCreateHost);
#endif
	cbRule->addItem(dataManager.GetSysString(1240));
	cbRule->addItem(dataManager.GetSysString(1241));
	cbRule->addItem(dataManager.GetSysString(1242));
	cbRule->addItem(dataManager.GetSysString(1243));
	env->addStaticText(dataManager.GetSysString(1227), rect<s32>(20 * xScale, 90 * yScale, 220 * xScale, 110 * yScale), false, false, wCreateHost);
#ifdef _IRR_ANDROID_PLATFORM_
	cbMatchMode = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(140 * xScale, 85 * yScale, 300 * xScale, 110 * yScale), wCreateHost);
#else
	cbMatchMode = env->addComboBox(rect<s32>(140 * xScale, 85 * yScale, 300 * xScale, 110 * yScale), wCreateHost);
#endif
	cbMatchMode->addItem(dataManager.GetSysString(1244));
	cbMatchMode->addItem(dataManager.GetSysString(1245));
	cbMatchMode->addItem(dataManager.GetSysString(1246));
	env->addStaticText(dataManager.GetSysString(1237), rect<s32>(20 * xScale, 120 * yScale, 320 * xScale, 140 * yScale), false, false, wCreateHost);
	myswprintf(strbuf, L"%d", 180);
#ifdef _IRR_ANDROID_PLATFORM_
	ebTimeLimit = CAndroidGUIEditBox::addAndroidEditBox(strbuf, true, env, rect<s32>(140 * xScale, 115 * yScale, 220 * xScale, 140 * yScale), wCreateHost);
#else
	ebTimeLimit = env->addEditBox(strbuf, rect<s32>(140 * xScale, 115 * yScale, 220 * xScale, 140 * yScale), true, wCreateHost);
#endif
	ebTimeLimit->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1228), rect<s32>(20 * xScale, 150 * yScale, 320 * xScale, 170 * yScale), false, false, wCreateHost);
	chkEnablePriority = env->addCheckBox(false, rect<s32>(20 * xScale, 180 * yScale, 360 * xScale, 200 * yScale), wCreateHost, -1, dataManager.GetSysString(1236));
	chkNoCheckDeck = env->addCheckBox(false, rect<s32>(20 * xScale, 210 * yScale, 170 * xScale, 230 * yScale), wCreateHost, -1, dataManager.GetSysString(1229));
	chkNoShuffleDeck = env->addCheckBox(false, rect<s32>(180 * xScale, 210 * yScale, 360 * xScale, 230 * yScale), wCreateHost, -1, dataManager.GetSysString(1230));
	env->addStaticText(dataManager.GetSysString(1231), rect<s32>(20 * xScale, 240 * yScale, 320 * xScale, 260 * yScale), false, false, wCreateHost);
	myswprintf(strbuf, L"%d", 8000);
#ifdef _IRR_ANDROID_PLATFORM_
	ebStartLP = CAndroidGUIEditBox::addAndroidEditBox(strbuf, true, env, rect<s32>(140 * xScale, 235 * yScale, 220 * xScale, 260 * yScale), wCreateHost);
#else
	ebStartLP = env->addEditBox(strbuf, rect<s32>(140 * xScale, 235 * yScale, 220 * xScale, 260 * yScale), true, wCreateHost);
#endif
	ebStartLP->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1232), rect<s32>(20 * xScale, 270 * yScale, 320 * xScale, 290 * yScale), false, false, wCreateHost);
	myswprintf(strbuf, L"%d", 5);
#ifdef _IRR_ANDROID_PLATFORM_
	ebStartHand = CAndroidGUIEditBox::addAndroidEditBox(strbuf, true, env, rect<s32>(140 * xScale, 265 * yScale, 220 * xScale, 290 * yScale), wCreateHost);
#else
	ebStartHand = env->addEditBox(strbuf, rect<s32>(140 * xScale, 265 * yScale, 220 * xScale, 290 * yScale), true, wCreateHost);
#endif
	ebStartHand->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1233), rect<s32>(20 * xScale, 300 * yScale, 320 * xScale, 320 * yScale), false, false, wCreateHost);
	myswprintf(strbuf, L"%d", 1);
#ifdef _IRR_ANDROID_PLATFORM_
	ebDrawCount = CAndroidGUIEditBox::addAndroidEditBox(strbuf, true, env, rect<s32>(140 * xScale, 295 * yScale, 220 * xScale, 320 * yScale), wCreateHost);
#else
	ebDrawCount = env->addEditBox(strbuf, rect<s32>(140 * xScale, 295 * yScale, 220 * xScale, 320 * yScale), true, wCreateHost);
#endif
	ebDrawCount->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1234), rect<s32>(10 * xScale, 360 * yScale, 220 * xScale, 380 * yScale), false, false, wCreateHost);
#ifdef _IRR_ANDROID_PLATFORM_
	ebServerName = CAndroidGUIEditBox::addAndroidEditBox(gameConf.gamename, true, env, rect<s32>(110 * xScale, 355 * yScale, 250 * xScale, 380 * yScale), wCreateHost);
#else
	ebServerName = env->addEditBox(gameConf.gamename, rect<s32>(110 * xScale, 355 * yScale, 250 * xScale, 380 * yScale), true, wCreateHost);
#endif
	ebServerName->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1235), rect<s32>(10 * xScale, 390 * yScale, 220 * xScale, 410 * yScale), false, false, wCreateHost);
#ifdef _IRR_ANDROID_PLATFORM_
	ebServerPass = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(110 * xScale, 385 * yScale, 250 * xScale, 410 * yScale), wCreateHost);
#else
	ebServerPass = env->addEditBox(L"", rect<s32>(110 * xScale, 385 * yScale, 250 * xScale, 410 * yScale), true, wCreateHost);
#endif
	ebServerPass->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnHostConfirm = env->addButton(rect<s32>(260 * xScale, 355 * yScale, 370 * xScale, 380 * yScale), wCreateHost, BUTTON_HOST_CONFIRM, dataManager.GetSysString(1211));
	btnHostCancel = env->addButton(rect<s32>(260 * xScale, 385 * yScale, 370 * xScale, 410 * yScale), wCreateHost, BUTTON_HOST_CANCEL, dataManager.GetSysString(1212));
	//host(single)
#ifdef _IRR_ANDROID_PLATFORM_
	wHostPrepare = env->addWindow(rect<s32>(0 * xScale, 0 * yScale, 512 * xScale, 540 * yScale), false, dataManager.GetSysString(1250));
	wHostPrepare->setDraggable(false);
	wHostPrepare->getCloseButton()->setVisible(false);
	wHostPrepare->setVisible(false);
	btnHostPrepDuelist = env->addButton(rect<s32>(10 * xScale, 30 * yScale, 110 * xScale, 55 * yScale), wHostPrepare, BUTTON_HP_DUELIST, dataManager.GetSysString(1251));
	for (int i = 0; i < 2; ++i) {
		stHostPrepDuelist[i] = env->addStaticText(L"",
				rect<s32>(60 * xScale, (65 + i * 45) * yScale, 260 * xScale,
						(105 + i * 45) * yScale), true, false, wHostPrepare);
		stHostPrepDuelist[i]->setTextAlignment(EGUIA_CENTER, EGUIA_CENTER);
		btnHostPrepKick[i] = env->addButton(
				rect<s32>(10 * xScale, (65 + i * 45) * yScale, 50 * xScale,
						(105 + i * 45) * yScale), wHostPrepare, BUTTON_HP_KICK,
				L"X");
		chkHostPrepReady[i] = env->addCheckBox(false,
				rect<s32>(270 * xScale, (65 + i * 45) * yScale, 310 * xScale,
						(105 + i * 45) * yScale), wHostPrepare,
				CHECKBOX_HP_READY, L"");
		chkHostPrepReady[i]->setEnabled(false);
	}
	for (int i = 2; i < 4; ++i) {
		stHostPrepDuelist[i] = env->addStaticText(L"",
				rect<s32>(60 * xScale, (145 + i * 45) * yScale, 260 * xScale,
						(185 + i * 45) * yScale), true, false, wHostPrepare);
		stHostPrepDuelist[i]->setTextAlignment(EGUIA_CENTER, EGUIA_CENTER);
		btnHostPrepKick[i] = env->addButton(
				rect<s32>(10 * xScale, (145 + i * 45) * yScale, 50 * xScale,
						(185 + i * 45) * yScale), wHostPrepare, BUTTON_HP_KICK,
				L"X");
		chkHostPrepReady[i] = env->addCheckBox(false,
				rect<s32>(270 * xScale, (145 + i * 45) * yScale, 310 * xScale,
						(185 + i * 45) * yScale), wHostPrepare,
				CHECKBOX_HP_READY, L"");
		chkHostPrepReady[i]->setEnabled(false);
	}
	btnHostPrepOB = env->addButton(
			rect<s32>(10 * xScale, 180 * yScale, 110 * xScale, 205 * yScale),
			wHostPrepare, BUTTON_HP_OBSERVER, dataManager.GetSysString(1252));
	myswprintf(dataManager.strBuffer, L"%ls%d", dataManager.GetSysString(1253),
			0);
	stHostPrepOB = env->addStaticText(dataManager.strBuffer,
			rect<s32>(10 * xScale, 210 * yScale, 270 * xScale, 230 * yScale),
			false, false, wHostPrepare);
	stHostPrepRule = env->addStaticText(L"",
			rect<s32>(300 * xScale, 30 * yScale, 460 * xScale, 230 * yScale),
			false, true, wHostPrepare);
	env->addStaticText(dataManager.GetSysString(1254),
			rect<s32>(10 * xScale, 385 * yScale, 110 * xScale, 410 * yScale),
			false, false, wHostPrepare);
	cbDeckSelect = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(120 * xScale, 380 * yScale, 270 * xScale, 405 * yScale), wHostPrepare);
	btnHostPrepStart = env->addButton(
			rect<s32>(280 * xScale, 380 * yScale, 390 * xScale, 405 * yScale),
			wHostPrepare, BUTTON_HP_START, dataManager.GetSysString(1215));
	btnHostPrepCancel = env->addButton(
			rect<s32>(400 * xScale, 380 * yScale, 510 * xScale, 405 * yScale),
			wHostPrepare, BUTTON_HP_CANCEL, dataManager.GetSysString(1212));
#else
	wHostPrepare = env->addWindow(rect<s32>(270 * xScale, 120 * yScale, 750 * xScale, 440 * yScale), false, dataManager.GetSysString(1250));
	wHostPrepare->getCloseButton()->setVisible(false);
	wHostPrepare->setVisible(false);
	btnHostPrepDuelist = env->addButton(rect<s32>(10 * xScale, 30 * yScale, 110 * xScale, 55 * yScale), wHostPrepare, BUTTON_HP_DUELIST, dataManager.GetSysString(1251));
	for(int i = 0; i < 2; ++i) {
		stHostPrepDuelist[i] = env->addStaticText(L"", rect<s32>(40 * xScale, (65 + i * 25) * yScale, 240 * xScale, (85 + i * 25) * yScale), true, false, wHostPrepare);
		btnHostPrepKick[i] = env->addButton(rect<s32>(10 * xScale, (65 + i * 25) * yScale, 30 * xScale, (85 + i * 25) * yScale), wHostPrepare, BUTTON_HP_KICK, L"X");
		chkHostPrepReady[i] = env->addCheckBox(false, rect<s32>(250 * xScale, (65 + i * 25) * yScale, 270 * xScale, (85 + i * 25) * yScale), wHostPrepare, CHECKBOX_HP_READY, L"");
		chkHostPrepReady[i]->setEnabled(false);
	}
	for(int i = 2; i < 4; ++i) {
		stHostPrepDuelist[i] = env->addStaticText(L"", rect<s32>(40 * xScale, (75 + i * 25) * yScale, 240 * xScale, (95 + i * 25) * yScale), true, false, wHostPrepare);
		btnHostPrepKick[i] = env->addButton(rect<s32>(10 * xScale, (75 + i * 25) * yScale, 30 * xScale, (95 + i * 25) * yScale), wHostPrepare, BUTTON_HP_KICK, L"X");
		chkHostPrepReady[i] = env->addCheckBox(false, rect<s32>(250 * xScale, (75 + i * 25) * yScale, 270 * xScale, (95 + i * 25) * yScale), wHostPrepare, CHECKBOX_HP_READY, L"");
		chkHostPrepReady[i]->setEnabled(false);
	}
	btnHostPrepOB = env->addButton(rect<s32>(10 * xScale, 180 * yScale, 110 * xScale, 205 * yScale), wHostPrepare, BUTTON_HP_OBSERVER, dataManager.GetSysString(1252));
	myswprintf(dataManager.strBuffer, L"%ls%d", dataManager.GetSysString(1253), 0);
	stHostPrepOB = env->addStaticText(dataManager.strBuffer, rect<s32>(10 * xScale, 210 * yScale, 270 * xScale, 230 * yScale), false, false, wHostPrepare);
	stHostPrepRule = env->addStaticText(L"", rect<s32>(280 * xScale, 30 * yScale, 460 * xScale, 230 * yScale), false, true, wHostPrepare);
	env->addStaticText(dataManager.GetSysString(1254), rect<s32>(10 * xScale, 235 * yScale, 110 * xScale, 255 * yScale), false, false, wHostPrepare);
#ifdef _IRR_ANDROID_PLATFORM_
	cbDeckSelect = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(120 * xScale, 230 * yScale, 270 * xScale, 255 * yScale), wHostPrepare);
#else
	cbDeckSelect = env->addComboBox(rect<s32>(120 * xScale, 230 * yScale, 270 * xScale, 255 * yScale), wHostPrepare);
#endif
	btnHostPrepStart = env->addButton(rect<s32>(230 * xScale, 280 * yScale, 340 * xScale, 305 * yScale), wHostPrepare, BUTTON_HP_START, dataManager.GetSysString(1215));
	btnHostPrepCancel = env->addButton(rect<s32>(350 * xScale, 280 * yScale, 460 * xScale, 305 * yScale), wHostPrepare, BUTTON_HP_CANCEL, dataManager.GetSysString(1212));
#endif

	//img
	wCardImg = env->addStaticText(L"", rect<s32>(1 * xScale, 1 * yScale, 199 * xScale, 273 * yScale), true, false, 0, -1, true);
	wCardImg->setBackgroundColor(0xc0c0c0c0);
	wCardImg->setVisible(false);
	imgCard = env->addImage(rect<s32>(9 * xScale, 9 * yScale, 187 * xScale, 262 * yScale), wCardImg);
	imgCard->setUseAlphaChannel(true);
#ifdef _IRR_ANDROID_PLATFORM_
	//phase
	wPhase = env->addStaticText(L"", rect<s32>(455 * xScale, 305 * yScale, 870 * xScale, 335 * yScale));
	wPhase->setVisible(false);
	btnDP = env->addButton(rect<s32>(0 * xScale, 0 * yScale, 50 * xScale, 30 * yScale), wPhase, -1, L"\xff24\xff30");
	btnDP->setEnabled(false);
	btnDP->setPressed(true);
	btnDP->setVisible(false);
	btnSP = env->addButton(rect<s32>(65 * xScale, 0 * yScale, 115 * xScale, 30 * yScale), wPhase, -1, L"\xff33\xff30");
	btnSP->setEnabled(false);
	btnSP->setPressed(true);
	btnSP->setVisible(false);
	btnM1 = env->addButton(rect<s32>(130 * xScale, 0 * yScale, 180 * xScale, 30 * yScale), wPhase, -1, L"\xff2d\xff11");
	btnM1->setEnabled(false);
	btnM1->setPressed(true);
	btnM1->setVisible(false);
	btnBP = env->addButton(rect<s32>(195 * xScale, 0 * yScale, 245 * xScale, 30 * yScale), wPhase, BUTTON_BP, L"\xff22\xff30");
	btnBP->setVisible(false);
	btnM2 = env->addButton(rect<s32>(260 * xScale, 0 * yScale, 310 * xScale, 30 * yScale), wPhase, BUTTON_M2, L"\xff2d\xff12");
	btnM2->setVisible(false);
	btnEP = env->addButton(rect<s32>(325 * xScale, 0 * yScale, 375 * xScale, 30 * yScale), wPhase, BUTTON_EP, L"\xff25\xff30");
	btnEP->setVisible(false);
#else
	//phase
	wPhase = env->addStaticText(L"", rect<s32>(475 * xScale, 310 * yScale, 850 * xScale, 330 * yScale));
	wPhase->setVisible(false);
	btnDP = env->addButton(rect<s32>(0 * xScale, 0 * yScale, 50 * xScale, 20 * yScale), wPhase, -1, L"\xff24\xff30");
	btnDP->setEnabled(false);
	btnDP->setPressed(true);
	btnDP->setVisible(false);
	btnSP = env->addButton(rect<s32>(65 * xScale, 0 * yScale, 115 * xScale, 20 * yScale), wPhase, -1, L"\xff33\xff30");
	btnSP->setEnabled(false);
	btnSP->setPressed(true);
	btnSP->setVisible(false);
	btnM1 = env->addButton(rect<s32>(130 * xScale, 0 * yScale, 180 * xScale, 20 * yScale), wPhase, -1, L"\xff2d\xff11");
	btnM1->setEnabled(false);
	btnM1->setPressed(true);
	btnM1->setVisible(false);
	btnBP = env->addButton(rect<s32>(195 * xScale, 0 * yScale, 245 * xScale, 20 * yScale), wPhase, BUTTON_BP, L"\xff22\xff30");
	btnBP->setVisible(false);
	btnM2 = env->addButton(rect<s32>(260 * xScale, 0 * yScale, 310 * xScale, 20 * yScale), wPhase, BUTTON_M2, L"\xff2d\xff12");
	btnM2->setVisible(false);
	btnEP = env->addButton(rect<s32>(325 * xScale, 0 * yScale, 375 * xScale, 20 * yScale), wPhase, BUTTON_EP, L"\xff25\xff30");
	btnEP->setVisible(false);
#endif
	//tab
	wInfos = env->addTabControl(rect<s32>(1 * xScale, 275 * yScale, 301 * xScale, 639 * yScale), 0, true);
	wInfos->setVisible(false);
	//info
	irr::gui::IGUITab* tabInfo = wInfos->addTab(dataManager.GetSysString(1270));
	stName = env->addStaticText(L"", rect<s32>(10 * xScale, 10 * yScale, 287 * xScale, 32 * yScale), true, false, tabInfo, -1, false);
	stName->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	stInfo = env->addStaticText(L"", rect<s32>(15 * xScale, 37 * yScale, 296 * xScale, 60 * yScale), false, true, tabInfo, -1, false);
	stInfo->setOverrideColor(SColor(255, 0, 0, 255));
	stDataInfo = env->addStaticText(L"", rect<s32>(15 * xScale, 60 * yScale, 296 * xScale, 83 * yScale), false, true, tabInfo, -1, false);
	stDataInfo->setOverrideColor(SColor(255, 0, 0, 255));
	stText = env->addStaticText(L"", rect<s32>(15 * xScale, 83 * yScale, 287 * xScale, 324 * yScale), false, true, tabInfo, -1, false);
#ifdef _IRR_ANDROID_PLATFORM_
	scrCardText = env->addScrollBar(false, rect<s32>(247 * xScale, 83 * yScale, 287 * xScale, 324 * yScale), tabInfo, SCROLL_CARDTEXT);
#else
	scrCardText = env->addScrollBar(false, rect<s32>(267 * xScale, 83 * yScale, 287 * xScale, 324 * yScale), tabInfo, SCROLL_CARDTEXT);
#endif
	scrCardText->setLargeStep(1);
	scrCardText->setSmallStep(1);
	scrCardText->setVisible(false);
	//log
	irr::gui::IGUITab* tabLog =  wInfos->addTab(dataManager.GetSysString(1271));
#ifdef _IRR_ANDROID_PLATFORM_
	lstLog = CAndroidGUIListBox::addAndroidGUIListBox(env, rect<s32>(10 * xScale, 10 * yScale, 290 * xScale, 290 * yScale), tabLog, LISTBOX_LOG, false, 40 * xScale);
#else
	lstLog = env->addListBox(rect<s32>(10 * xScale, 10 * yScale, 290 * xScale, 290 * yScale), tabLog, LISTBOX_LOG, false);
#endif
	lstLog->setItemHeight(18);
	btnClearLog = env->addButton(rect<s32>(160 * xScale, 300 * yScale, 260 * xScale, 325 * yScale), tabLog, BUTTON_CLEAR_LOG, dataManager.GetSysString(1272));
	//system
	irr::gui::IGUITab* tabSystem = wInfos->addTab(dataManager.GetSysString(1273));
	chkAutoPos = env->addCheckBox(false, rect<s32>(20 * xScale, 20 * yScale, 280 * xScale, 45 * yScale), tabSystem, -1, dataManager.GetSysString(1274));
	chkAutoPos->setChecked(true);
	chkRandomPos = env->addCheckBox(false, rect<s32>(40 * xScale, 50 * yScale, 300 * xScale, 75 * yScale), tabSystem, -1, dataManager.GetSysString(1275));
	chkAutoChain = env->addCheckBox(false, rect<s32>(20 * xScale, 80 * yScale, 280 * xScale, 105 * yScale), tabSystem, -1, dataManager.GetSysString(1276));
	chkAutoChain->setChecked(true);
	chkWaitChain = env->addCheckBox(false, rect<s32>(20 * xScale, 110 * yScale, 280 * xScale, 135 * yScale), tabSystem, -1, dataManager.GetSysString(1277));
	chkIgnore1 = env->addCheckBox(false, rect<s32>(20 * xScale, 170 * yScale, 280 * xScale, 195 * yScale), tabSystem, -1, dataManager.GetSysString(1290));
	chkIgnore2 = env->addCheckBox(false, rect<s32>(20 * xScale, 200 * yScale, 280 * xScale, 225 * yScale), tabSystem, -1, dataManager.GetSysString(1291));
	chkIgnore2->setChecked(false);
	//
	wHand = env->addWindow(rect<s32>(500 * xScale, 450 * yScale, 825 * xScale, 605 * yScale), false, L"");
	wHand->getCloseButton()->setVisible(false);
	wHand->setDraggable(false);
	wHand->setDrawTitlebar(false);
	wHand->setVisible(false);
	for(int i = 0; i < 3; ++i) {
		btnHand[i] = env->addButton(rect<s32>((10 + 105 * i) * xScale, 10 * yScale, (105 + 105 * i)  * xScale, 144 * yScale), wHand, BUTTON_HAND1 + i, L"");
		btnHand[i]->setImage(imageManager.tHand[i]);
	}
#ifdef _IRR_ANDROID_PLATFORM_
	//
	wFTSelect = env->addWindow(rect<s32>(530 * xScale, 220 * yScale, 800 * xScale, 380 * yScale), false, L"");
	wFTSelect->getCloseButton()->setVisible(false);
	wFTSelect->setVisible(false);
	btnFirst = env->addButton(rect<s32>(10 * xScale, 30 * yScale, 260 * xScale, 75 * yScale), wFTSelect, BUTTON_FIRST, dataManager.GetSysString(100));
	btnSecond = env->addButton(rect<s32>(10 * xScale, 85 * yScale, 260 * xScale, 130 * yScale), wFTSelect, BUTTON_SECOND, dataManager.GetSysString(101));
	//message (310)
	wMessage = env->addWindow(rect<s32>(470 * xScale, 180 * yScale, 860 * xScale, 360 * yScale), false, dataManager.GetSysString(1216));
	wMessage->getCloseButton()->setVisible(false);
	wMessage->setVisible(false);
	stMessage = env->addStaticText(L"", rect<s32>(20 * xScale, 20 * yScale, 390 * xScale, 100 * yScale), false, true, wMessage, -1, false);
	stMessage->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	btnMsgOK = env->addButton(rect<s32>(130 * xScale, 115 * yScale, 260 * xScale, 165 * yScale), wMessage, BUTTON_MSG_OK, dataManager.GetSysString(1211));
	//auto fade message (310)
	wACMessage = env->addWindow(rect<s32>(490 * xScale, 240 * yScale, 840 * xScale, 300 * yScale), false, L"");
	wACMessage->getCloseButton()->setVisible(false);
	wACMessage->setVisible(false);
	wACMessage->setDrawBackground(false);
	stACMessage = env->addStaticText(L"", rect<s32>(0 * xScale, 0 * yScale, 350 * xScale, 60 * yScale), true, true, wACMessage, -1, true);
	stACMessage->setBackgroundColor(0xc0c0c0ff);
	stACMessage->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	//yes/no (310)
	wQuery = env->addWindow(rect<s32>(470 * xScale, 180 * yScale, 860 * xScale, 360 * yScale), false, dataManager.GetSysString(560));
	wQuery->getCloseButton()->setVisible(false);
	wQuery->setVisible(false);
	stQMessage =  env->addStaticText(L"", rect<s32>(20 * xScale, 20 * yScale, 390 * xScale, 100 * yScale), false, true, wQuery, -1, false);
	stQMessage->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	btnYes = env->addButton(rect<s32>(80 * xScale, 115 * yScale, 170 * xScale, 165 * yScale), wQuery, BUTTON_YES, dataManager.GetSysString(1213));
	btnNo = env->addButton(rect<s32>(200 * xScale, 115 * yScale, 290 * xScale, 165 * yScale), wQuery, BUTTON_NO, dataManager.GetSysString(1214));
	//options (310)
	wOptions = env->addWindow(rect<s32>(470 * xScale, 180 * yScale, 860 * xScale, 360 * yScale), false, L"");
	wOptions->getCloseButton()->setVisible(false);
	wOptions->setVisible(false);
	stOptions =  env->addStaticText(L"", rect<s32>(20 * xScale, 20 * yScale, 390 * xScale, 100 * yScale), false, true, wOptions, -1, false);
	stOptions->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	btnOptionOK = env->addButton(rect<s32>(130 * xScale, 115 * yScale, 260 * xScale, 165 * yScale), wOptions, BUTTON_OPTION_OK, dataManager.GetSysString(1211));
	btnOptionp = env->addButton(rect<s32>(20 * xScale, 115 * yScale, 100 * xScale, 165 * yScale), wOptions, BUTTON_OPTION_PREV, L"<<<");
	btnOptionn = env->addButton(rect<s32>(290 * xScale, 115 * yScale, 370 * xScale, 165 * yScale), wOptions, BUTTON_OPTION_NEXT, L">>>");
#else
	wFTSelect = env->addWindow(rect<s32>(550 * xScale, 240 * yScale, 780 * xScale, 340 * yScale), false, L"");
	wFTSelect->getCloseButton()->setVisible(false);
	wFTSelect->setVisible(false);
	btnFirst = env->addButton(rect<s32>(10 * xScale, 30 * yScale, 220 * xScale, 55 * yScale), wFTSelect, BUTTON_FIRST, dataManager.GetSysString(100));
	btnSecond = env->addButton(rect<s32>(10 * xScale, 60 * yScale, 220 * xScale, 85 * yScale), wFTSelect, BUTTON_SECOND, dataManager.GetSysString(101));

	wMessage = env->addWindow(rect<s32>(490 * xScale, 200 * yScale, 840 * xScale, 340 * yScale), false, dataManager.GetSysString(1216));
	wMessage->getCloseButton()->setVisible(false);
	wMessage->setVisible(false);
	stMessage =  env->addStaticText(L"", rect<s32>(20 * xScale, 20 * yScale, 350 * xScale, 100 * yScale), false, true, wMessage, -1, false);
	stMessage->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	btnMsgOK = env->addButton(rect<s32>(130 * xScale, 105 * yScale, 220 * xScale, 130 * yScale), wMessage, BUTTON_MSG_OK, dataManager.GetSysString(1211));
	//auto fade message (310)
	wACMessage = env->addWindow(rect<s32>(490 * xScale, 240 * yScale, 840 * xScale, 300 * yScale), false, L"");
	wACMessage->getCloseButton()->setVisible(false);
	wACMessage->setVisible(false);
	wACMessage->setDrawBackground(false);
	stACMessage = env->addStaticText(L"", rect<s32>(0 * xScale, 0 * yScale, 350 * xScale, 60 * yScale), true, true, wACMessage, -1, true);
	stACMessage->setBackgroundColor(0xc0c0c0ff);
	stACMessage->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	//yes/no (310)
	wQuery = env->addWindow(rect<s32>(490 * xScale, 200 * yScale, 840 * xScale, 340 * yScale), false, dataManager.GetSysString(560));
	wQuery->getCloseButton()->setVisible(false);
	wQuery->setVisible(false);
	stQMessage =  env->addStaticText(L"", rect<s32>(20 * xScale, 20 * yScale, 350 * xScale, 100 * yScale), false, true, wQuery, -1, false);
	stQMessage->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	btnYes = env->addButton(rect<s32>(100 * xScale, 105 * yScale, 150 * xScale, 130 * yScale), wQuery, BUTTON_YES, dataManager.GetSysString(1213));
	btnNo = env->addButton(rect<s32>(200 * xScale, 105 * yScale, 250 * xScale, 130 * yScale), wQuery, BUTTON_NO, dataManager.GetSysString(1214));

	//options (310)
	wOptions = env->addWindow(rect<s32>(490 * xScale, 200 * yScale, 840 * xScale, 340 * yScale), false, L"");
	wOptions->getCloseButton()->setVisible(false);
	wOptions->setVisible(false);
	stOptions =  env->addStaticText(L"", rect<s32>(20 * xScale, 20 * yScale, 350 * xScale, 100 * yScale), false, true, wOptions, -1, false);
	stOptions->setTextAlignment(irr::gui::EGUIA_UPPERLEFT, irr::gui::EGUIA_CENTER);
	btnOptionOK = env->addButton(rect<s32>(130 * xScale, 105 * yScale, 220 * xScale, 130 * yScale), wOptions, BUTTON_OPTION_OK, dataManager.GetSysString(1211));
	btnOptionp = env->addButton(rect<s32>(20 * xScale, 105 * yScale, 60 * xScale, 130 * yScale), wOptions, BUTTON_OPTION_PREV, L"<<<");
	btnOptionn = env->addButton(rect<s32>(290 * xScale, 105 * yScale, 330 * xScale, 130 * yScale), wOptions, BUTTON_OPTION_NEXT, L">>>");
#endif
	//pos select
	wPosSelect = env->addWindow(rect<s32>(340 * xScale, 200 * yScale, 935 * xScale, 410 * yScale), false, dataManager.GetSysString(561));
	wPosSelect->getCloseButton()->setVisible(false);
	wPosSelect->setVisible(false);
	btnPSAU = irr::gui::CGUIImageButton::addImageButton(env, rect<s32>(10 * xScale, 45 * yScale, 150 * xScale, 185 * yScale), wPosSelect, BUTTON_POS_AU);
	btnPSAU->setImageScale(core::vector2df(0.5, 0.5));
	btnPSAD = irr::gui::CGUIImageButton::addImageButton(env, rect<s32>(155 * xScale, 45 * yScale, 295 * xScale, 185 * yScale), wPosSelect, BUTTON_POS_AD);
	btnPSAD->setImageScale(core::vector2df(0.5, 0.5));
	btnPSAD->setImage(imageManager.tCover, rect<s32>(0, 0, 177 * xScale, 254 * yScale));
	btnPSDU = irr::gui::CGUIImageButton::addImageButton(env, rect<s32>(300 * xScale, 45 * yScale, 440 * xScale, 185 * yScale), wPosSelect, BUTTON_POS_DU);
	btnPSDU->setImageScale(core::vector2df(0.5, 0.5));
	btnPSDU->setImageRotation(270);
	btnPSDD = irr::gui::CGUIImageButton::addImageButton(env, rect<s32>(445 * xScale, 45 * yScale, 585 * xScale, 185 * yScale), wPosSelect, BUTTON_POS_DD);
	btnPSDD->setImageScale(core::vector2df(0.5, 0.5));
	btnPSDD->setImageRotation(270);
	btnPSDD->setImage(imageManager.tCover, rect<s32>(0 * xScale, 0 * yScale, 177 * xScale, 254 * yScale));
	//card select
#ifdef _IRR_ANDROID_PLATFORM_
	wCardSelect = env->addWindow(rect<s32>(320 * xScale, 100 * yScale, 1000 * xScale, 430 * yScale), false, L"");
#else
	wCardSelect = env->addWindow(rect<s32>(320 * xScale, 100 * yScale, 1000 * xScale, 400 * yScale), false, L"");
#endif
	wCardSelect->getCloseButton()->setVisible(false);
	wCardSelect->setVisible(false);
	for(int i = 0; i < 5; ++i) {
		stCardPos[i] = env->addStaticText(L"", rect<s32>((40 + 125 * i) * xScale, 30 * yScale, (139 + 125 * i) * xScale, 50 * yScale), true, false, wCardSelect, -1, true);
		stCardPos[i]->setBackgroundColor(0xffffffff);
		stCardPos[i]->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
		btnCardSelect[i] = irr::gui::CGUIImageButton::addImageButton(env, rect<s32>((30 + 125 * i)  * xScale, 55 * yScale, (150 + 125 * i) * xScale, 225 * yScale), wCardSelect, BUTTON_CARD_0 + i);
		btnCardSelect[i]->setImageScale(core::vector2df(0.6f * xScale, 0.6f  * yScale));
	}
#ifdef _IRR_ANDROID_PLATFORM_
	scrCardList = env->addScrollBar(true, rect<s32>(30 * xScale, 235 * yScale, 650 * xScale, 275 * yScale), wCardSelect, SCROLL_CARD_SELECT);
	btnSelectOK = env->addButton(rect<s32>(300 * xScale, 285 * yScale, 380 * xScale, 325 * yScale), wCardSelect, BUTTON_CARD_SEL_OK, dataManager.GetSysString(1211));
#else
	scrCardList = env->addScrollBar(true, rect<s32>(30 * xScale, 235 * yScale, 650 * xScale, 255 * yScale), wCardSelect, SCROLL_CARD_SELECT);
	btnSelectOK = env->addButton(rect<s32>(300 * xScale, 265 * yScale, 380 * xScale, 290 * yScale), wCardSelect, BUTTON_CARD_SEL_OK, dataManager.GetSysString(1211));
#endif
	//announce number
	wANNumber = env->addWindow(rect<s32>(550 * xScale, 200 * yScale, 780 * xScale, 295 * yScale), false, L"");
	wANNumber->getCloseButton()->setVisible(false);
	wANNumber->setVisible(false);
#ifdef _IRR_ANDROID_PLATFORM_
	cbANNumber = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(40 * xScale, 30 * yScale, 190 * xScale, 50 * yScale), wANNumber, -1);
#else
	cbANNumber =  env->addComboBox(rect<s32>(40 * xScale, 30 * yScale, 190 * xScale, 50 * yScale), wANNumber, -1);
#endif
	cbANNumber->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnANNumberOK = env->addButton(rect<s32>(80 * xScale, 60 * yScale, 150 * xScale, 85 * yScale), wANNumber, BUTTON_ANNUMBER_OK, dataManager.GetSysString(1211));
	//announce card
	wANCard = env->addWindow(rect<s32>(560 * xScale, 170 * yScale, 770 * xScale, 370 * yScale), false, L"");
	wANCard->getCloseButton()->setVisible(false);
	wANCard->setVisible(false);
#ifdef _IRR_ANDROID_PLATFORM_
	ebANCard = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(20 * xScale, 25 * yScale, 190 * xScale, 45 * yScale), wANCard, EDITBOX_ANCARD);
	lstANCard = CAndroidGUIListBox::addAndroidGUIListBox(env, rect<s32>(20 * xScale, 50 * yScale, 190 * xScale, 160 * yScale), wANCard, LISTBOX_ANCARD, true, 40 * xScale);
#else
	ebANCard = env->addEditBox(L"", rect<s32>(20 * xScale, 25 * yScale, 190 * xScale, 45 * yScale), true, wANCard, EDITBOX_ANCARD);
	lstANCard = env->addListBox(rect<s32>(20 * xScale, 50 * yScale, 190 * xScale, 160 * yScale), wANCard, LISTBOX_ANCARD, true);
#endif
	ebANCard->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnANCardOK = env->addButton(rect<s32>(60 * xScale, 165 * yScale, 150 * xScale, 190 * yScale), wANCard, BUTTON_ANCARD_OK, dataManager.GetSysString(1211));
	//announce attribute
	wANAttribute = env->addWindow(rect<s32>(500 * xScale, 200 * yScale, 830 * xScale, 285 * yScale), false, dataManager.GetSysString(562));
	wANAttribute->getCloseButton()->setVisible(false);
	wANAttribute->setVisible(false);
	for(int filter = 0x1, i = 0; i < 7; filter <<= 1, ++i)
		chkAttribute[i] = env->addCheckBox(false, rect<s32>((10 + (i % 4) * 80) * xScale, (25 + (i / 4) * 25) * yScale, (90 + (i % 4) * 80) * xScale, (50 + (i / 4) * 25) * yScale),
		                                   wANAttribute, CHECK_ATTRIBUTE, dataManager.FormatAttribute(filter));
	//announce attribute
	wANRace = env->addWindow(rect<s32>(480 * xScale, 200 * yScale, 850 * xScale, 385 * yScale), false, dataManager.GetSysString(563));
	wANRace->getCloseButton()->setVisible(false);
	wANRace->setVisible(false);
	for(int filter = 0x1, i = 0; i < 23; filter <<= 1, ++i)
		chkRace[i] = env->addCheckBox(false, rect<s32>((10 + (i % 4) * 90) * xScale, (25 + (i / 4) * 25) * yScale, (100 + (i % 4) * 90) * xScale, (50 + (i / 4) * 25) * yScale),
		                              wANRace, CHECK_RACE, dataManager.FormatRace(filter));
	//selection hint
	stHintMsg = env->addStaticText(L"", rect<s32>(500 * xScale, 60 * yScale, 820 * xScale, 90 * yScale), true, false, 0, -1, false);
	stHintMsg->setBackgroundColor(0xc0ffffff);
	stHintMsg->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	stHintMsg->setVisible(false);
	stTip = env->addStaticText(L"", rect<s32>(0 * xScale, 0 * yScale, 150 * xScale, 150 * yScale), false, true, 0, -1, true);
	stTip->setBackgroundColor(0xc0ffffff);
	stTip->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	stTip->setVisible(false);
	//cmd menu
	wCmdMenu = env->addWindow(rect<s32>(10 * xScale, 10 * yScale, 110 * xScale, 179 * yScale), false, L"");
	wCmdMenu->setDrawTitlebar(false);
	wCmdMenu->setVisible(false);
	wCmdMenu->getCloseButton()->setVisible(false);
#ifdef _IRR_ANDROID_PLATFORM_
	btnActivate = env->addButton(rect<s32>(1 * xScale, 1 * yScale, 99 * xScale, 40 * yScale), wCmdMenu, BUTTON_CMD_ACTIVATE, dataManager.GetSysString(1150));
	btnSummon = env->addButton(rect<s32>(1 * xScale, 41 * yScale, 99 * xScale, 81 * yScale), wCmdMenu, BUTTON_CMD_SUMMON, dataManager.GetSysString(1151));
	btnSPSummon = env->addButton(rect<s32>(1 * xScale, 82 * yScale, 99 * xScale, 122 * yScale), wCmdMenu, BUTTON_CMD_SPSUMMON, dataManager.GetSysString(1152));
	btnMSet = env->addButton(rect<s32>(1 * xScale, 123 * yScale, 99 * xScale, 164 * yScale), wCmdMenu, BUTTON_CMD_MSET, dataManager.GetSysString(1153));
	btnSSet = env->addButton(rect<s32>(1 * xScale, 165 * yScale, 99 * xScale, 205 * yScale), wCmdMenu, BUTTON_CMD_SSET, dataManager.GetSysString(1153));
	btnRepos = env->addButton(rect<s32>(1 * xScale, 206 * yScale, 99 * xScale, 246 * yScale), wCmdMenu, BUTTON_CMD_REPOS, dataManager.GetSysString(1154));
	btnAttack = env->addButton(rect<s32>(1 * xScale, 247 * yScale, 99 * xScale, 288 * yScale), wCmdMenu, BUTTON_CMD_ATTACK, dataManager.GetSysString(1157));
	btnShowList = env->addButton(rect<s32>(1 * xScale, 289 * yScale, 99 * xScale, 329 * yScale), wCmdMenu, BUTTON_CMD_SHOWLIST, dataManager.GetSysString(1158));
#else
	btnActivate = env->addButton(rect<s32>(1 * xScale, 1 * yScale, 99 * xScale, 21 * yScale), wCmdMenu, BUTTON_CMD_ACTIVATE, dataManager.GetSysString(1150));
	btnSummon = env->addButton(rect<s32>(1 * xScale, 22 * yScale, 99 * xScale, 42 * yScale), wCmdMenu, BUTTON_CMD_SUMMON, dataManager.GetSysString(1151));
	btnSPSummon = env->addButton(rect<s32>(1 * xScale, 43 * yScale, 99 * xScale, 63 * yScale), wCmdMenu, BUTTON_CMD_SPSUMMON, dataManager.GetSysString(1152));
	btnMSet = env->addButton(rect<s32>(1 * xScale, 64 * yScale, 99 * xScale, 84 * yScale), wCmdMenu, BUTTON_CMD_MSET, dataManager.GetSysString(1153));
	btnSSet = env->addButton(rect<s32>(1 * xScale, 85 * yScale, 99 * xScale, 105 * yScale), wCmdMenu, BUTTON_CMD_SSET, dataManager.GetSysString(1153));
	btnRepos = env->addButton(rect<s32>(1 * xScale, 106 * yScale, 99 * xScale, 126 * yScale), wCmdMenu, BUTTON_CMD_REPOS, dataManager.GetSysString(1154));
	btnAttack = env->addButton(rect<s32>(1 * xScale, 127 * yScale, 99 * xScale, 147 * yScale), wCmdMenu, BUTTON_CMD_ATTACK, dataManager.GetSysString(1157));
	btnShowList = env->addButton(rect<s32>(1 * xScale, 148 * yScale, 99 * xScale, 168 * yScale), wCmdMenu, BUTTON_CMD_SHOWLIST, dataManager.GetSysString(1158));
#endif
	//deck edit
	wDeckEdit = env->addStaticText(L"", rect<s32>(309 * xScale, 8 * yScale, 605 * xScale, 130 * yScale), true, false, 0, -1, true);
	wDeckEdit->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1300), rect<s32>(10 * xScale, 9 * yScale, 100 * xScale, 29 * yScale), false, false, wDeckEdit);
#ifdef _IRR_ANDROID_PLATFORM_
	cbDBLFList = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(80 * xScale, 5 * yScale, 220 * xScale, 30 * yScale), wDeckEdit, COMBOBOX_DBLFLIST);
#else
	cbDBLFList = env->addComboBox(rect<s32>(80 * xScale, 5 * yScale, 220 * xScale, 30 * yScale), wDeckEdit, COMBOBOX_DBLFLIST);
#endif
	env->addStaticText(dataManager.GetSysString(1301), rect<s32>(10 * xScale, 39 * yScale, 100 * xScale, 59 * yScale), false, false, wDeckEdit);
#ifdef _IRR_ANDROID_PLATFORM_
	cbDBDecks = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(80 * xScale, 35 * yScale, 220 * xScale, 60 * yScale), wDeckEdit, COMBOBOX_DBDECKS);
#else
	cbDBDecks = env->addComboBox(rect<s32>(80 * xScale, 35 * yScale, 220 * xScale, 60 * yScale), wDeckEdit, COMBOBOX_DBDECKS);
#endif
	for(unsigned int i = 0; i < deckManager._lfList.size(); ++i)
		cbDBLFList->addItem(deckManager._lfList[i].listName);
	btnSaveDeck = env->addButton(rect<s32>(225 * xScale, 35 * yScale, 290 * xScale, 60 * yScale), wDeckEdit, BUTTON_SAVE_DECK, dataManager.GetSysString(1302));
#ifdef _IRR_ANDROID_PLATFORM_
	ebDeckname = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(80 * xScale, 65 * yScale, 220 * xScale, 90 * yScale), wDeckEdit, -1);
#else
	ebDeckname = env->addEditBox(L"", rect<s32>(80 * xScale, 65 * yScale, 220 * xScale, 90 * yScale), true, wDeckEdit, -1);
#endif
	ebDeckname->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnSaveDeckAs = env->addButton(rect<s32>(225 * xScale, 65 * yScale, 290 * xScale, 90 * yScale), wDeckEdit, BUTTON_SAVE_DECK_AS, dataManager.GetSysString(1303));
	btnClearDeck = env->addButton(rect<s32>(240 * xScale, 95 * yScale, 290 * xScale, 116 * yScale), wDeckEdit, BUTTON_CLEAR_DECK, dataManager.GetSysString(1304));
	btnSortDeck = env->addButton(rect<s32>(185 * xScale, 95 * yScale, 235 * xScale, 116 * yScale), wDeckEdit, BUTTON_SORT_DECK, dataManager.GetSysString(1305));
	btnShuffleDeck = env->addButton(rect<s32>(130 * xScale, 95 * yScale, 180 * xScale, 116 * yScale), wDeckEdit, BUTTON_SHUFFLE_DECK, dataManager.GetSysString(1307));
	btnDBExit = env->addButton(rect<s32>(10 * xScale, 95 * yScale, 90 * xScale, 116 * yScale), wDeckEdit, BUTTON_DBEXIT, dataManager.GetSysString(1306));
	btnSideOK = env->addButton(rect<s32>(510 * xScale, 40 * yScale, 820 * xScale, 80 * yScale), 0, BUTTON_SIDE_OK, dataManager.GetSysString(1334));
	btnSideOK->setVisible(false);
	//filters
	wFilter = env->addStaticText(L"", rect<s32>(610 * xScale, 8 * yScale, 1020 * xScale, 130 * yScale), true, false, 0, -1, true);
	wFilter->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1311), rect<s32>(10 * xScale, 5 * yScale, 70 * xScale, 25 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	cbCardType = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(60 * xScale, 3 * yScale, 120 * xScale, 23 * yScale), wFilter, COMBOBOX_MAINTYPE);
#else
	cbCardType = env->addComboBox(rect<s32>(60 * xScale, 3 * yScale, 120 * xScale, 23 * yScale), wFilter, COMBOBOX_MAINTYPE);
#endif
	cbCardType->addItem(dataManager.GetSysString(1310));
	cbCardType->addItem(dataManager.GetSysString(1312));
	cbCardType->addItem(dataManager.GetSysString(1313));
	cbCardType->addItem(dataManager.GetSysString(1314));
#ifdef _IRR_ANDROID_PLATFORM_
	cbCardType2 = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(130 * xScale, 3 * yScale, 190 * xScale, 23 * yScale), wFilter, -1);
#else
	cbCardType2 = env->addComboBox(rect<s32>(130 * xScale, 3 * yScale, 190 * xScale, 23 * yScale), wFilter, -1);
#endif
	cbCardType2->addItem(dataManager.GetSysString(1310), 0);
	env->addStaticText(dataManager.GetSysString(1315), rect<s32>(205 * xScale, 5 * yScale, 280 * xScale, 25 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	cbLimit = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(260 * xScale, 3 * yScale, 390 * xScale, 23 * yScale), wFilter, -1);
#else
	cbLimit = env->addComboBox(rect<s32>(260 * xScale, 3 * yScale, 390 * xScale, 23 * yScale), wFilter, -1);
#endif
	cbLimit->addItem(dataManager.GetSysString(1310));
	cbLimit->addItem(dataManager.GetSysString(1316));
	cbLimit->addItem(dataManager.GetSysString(1317));
	cbLimit->addItem(dataManager.GetSysString(1318));
	cbLimit->addItem(dataManager.GetSysString(1240));
	cbLimit->addItem(dataManager.GetSysString(1241));
	env->addStaticText(dataManager.GetSysString(1319), rect<s32>(10 * xScale, 28 * yScale, 70 * xScale, 48 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	cbAttribute = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(60 * xScale, 26 * yScale, 190 * xScale, 46 * yScale), wFilter, -1);
#else
	cbAttribute = env->addComboBox(rect<s32>(60 * xScale, 26 * yScale, 190 * xScale, 46 * yScale), wFilter, -1);
#endif
	cbAttribute->addItem(dataManager.GetSysString(1310), 0);
	for(int filter = 0x1; filter != 0x80; filter <<= 1)
		cbAttribute->addItem(dataManager.FormatAttribute(filter), filter);
	env->addStaticText(dataManager.GetSysString(1321), rect<s32>(10 * xScale, 51 * yScale, 70 * xScale, 71 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	cbRace = CAndroidGUIComboBox::addAndroidComboBox(env, rect<s32>(60 * xScale, 49 * yScale, 190 * xScale, 69 * yScale), wFilter, -1);
#else
	cbRace = env->addComboBox(rect<s32>(60 * xScale, 49 * yScale, 190 * xScale, 69 * yScale), wFilter, -1);
#endif
	cbRace->addItem(dataManager.GetSysString(1310), 0);
	for(int filter = 0x1; filter != 0x400000; filter <<= 1)
		cbRace->addItem(dataManager.FormatRace(filter), filter);
	env->addStaticText(dataManager.GetSysString(1322), rect<s32>(205 * xScale, 28 * yScale, 280 * xScale, 48 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	ebAttack = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(260 * xScale, 26 * yScale, 340 * xScale, 46 * yScale), wFilter);
#else
	ebAttack = env->addEditBox(L"", rect<s32>(260 * xScale, 26 * yScale, 340 * xScale, 46 * yScale), true, wFilter);
#endif
	ebAttack->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1323), rect<s32>(205 * xScale, 51 * yScale, 280 * xScale, 71 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	ebDefence = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(260 * xScale, 49 * yScale, 340 * xScale, 69 * yScale), wFilter);
#else
	ebDefence = env->addEditBox(L"", rect<s32>(260 * xScale, 49 * yScale, 340 * xScale, 69 * yScale), true, wFilter);
#endif
	ebDefence->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1324), rect<s32>(10 * xScale, 74 * yScale, 80 * xScale, 94 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	ebStar = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(60 * xScale, 72 * yScale, 140 * xScale, 92 * yScale), wFilter);
#else
	ebStar = env->addEditBox(L"", rect<s32>(60 * xScale, 72 * yScale, 140 * xScale, 92 * yScale), true, wFilter);
#endif
	ebStar->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	env->addStaticText(dataManager.GetSysString(1325), rect<s32>(205 * xScale, 74 * yScale, 280 * xScale, 94 * yScale), false, false, wFilter);
#ifdef _IRR_ANDROID_PLATFORM_
	ebCardName = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(260 * xScale, 72 * yScale, 390 * xScale, 92 * yScale), wFilter, EDITBOX_KEYWORD);
#else
	ebCardName = env->addEditBox(L"", rect<s32>(260 * xScale, 72 * yScale, 390 * xScale, 92 * yScale), true, wFilter, EDITBOX_KEYWORD);
#endif
	ebCardName->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnEffectFilter = env->addButton(rect<s32>(345 * xScale, 28 * yScale, 390 * xScale, 69 * yScale), wFilter, BUTTON_EFFECT_FILTER, dataManager.GetSysString(1326));
	btnStartFilter = env->addButton(rect<s32>(210 * xScale, 96 * yScale, 390 * xScale, 118 * yScale), wFilter, BUTTON_START_FILTER, dataManager.GetSysString(1327));
	wCategories = env->addWindow(rect<s32>(630 * xScale, 60 * yScale, 1000 * xScale, 270 * yScale), false, dataManager.strBuffer);
	wCategories->getCloseButton()->setVisible(false);
	wCategories->setDrawTitlebar(false);
	wCategories->setDraggable(false);
	wCategories->setVisible(false);
	btnCategoryOK = env->addButton(rect<s32>(135 * xScale, 175 * yScale, 235 * xScale, 200 * yScale), wCategories, BUTTON_CATEGORY_OK, dataManager.GetSysString(1211));
	for(int i = 0; i < 32; ++i)
		chkCategory[i] = env->addCheckBox(false, recti((10 + (i % 4) * 90)  * xScale, (10 + (i / 4) * 20) * yScale, (100 + (i % 4) * 90) * xScale, (30 + (i / 4) * 20) * yScale), wCategories, -1, dataManager.GetSysString(1100 + i));
#ifdef _IRR_ANDROID_PLATFORM_
	scrFilter = env->addScrollBar(false, recti(810 * xScale, 161 * yScale, 850 * xScale, 629 * yScale), 0, SCROLL_FILTER);
#else
	scrFilter = env->addScrollBar(false, recti(999 * xScale, 161 * yScale, 1019 * xScale, 629 * yScale), 0, SCROLL_FILTER);
#endif
	scrFilter->setLargeStep(10);
	scrFilter->setSmallStep(1);
	scrFilter->setVisible(false);

#ifdef _IRR_ANDROID_PLATFORM_
	//replay window
	wReplay = env->addWindow(rect<s32>(220 * xScale, 100 * yScale, 800 * xScale, 520 * yScale), false, dataManager.GetSysString(1202));
	wReplay->getCloseButton()->setVisible(false);
	wReplay->setVisible(false);
	lstReplayList = CAndroidGUIListBox::addAndroidGUIListBox(env, rect<s32>(10 * xScale, 30 * yScale, 350 * xScale, 400 * yScale), wReplay, LISTBOX_REPLAY_LIST, true, 40 * xScale);
	lstReplayList->setItemHeight(18);
	btnLoadReplay = env->addButton(rect<s32>(460 * xScale, 320 * yScale, 570 * xScale, 360 * yScale), wReplay, BUTTON_LOAD_REPLAY, dataManager.GetSysString(1348));
	btnReplayCancel = env->addButton(rect<s32>(460 * xScale, 370 * yScale, 570 * xScale, 410 * yScale), wReplay, BUTTON_CANCEL_REPLAY, dataManager.GetSysString(1347));
	env->addStaticText(dataManager.GetSysString(1349), rect<s32>(360 * xScale, 30 * yScale, 570 * xScale, 50 * yScale), false, true, wReplay);
	stReplayInfo = env->addStaticText(L"", rect<s32>(360 * xScale, 60 * yScale, 570 * xScale, 315 * yScale), false, true, wReplay);
	env->addStaticText(dataManager.GetSysString(1353), rect<s32>(360 * xScale, 240 * yScale, 570 * xScale, 260 * yScale), false, true, wReplay);

	ebRepStartTurn = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(360 * xScale, 275 * yScale, 460 * xScale, 295 * yScale), wReplay, -1);
	ebRepStartTurn->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	//single play window
	wSinglePlay = env->addWindow(rect<s32>(220 * xScale, 100 * yScale, 800 * xScale, 520 * yScale), false, dataManager.GetSysString(1201));
	wSinglePlay->getCloseButton()->setVisible(false);
	wSinglePlay->setVisible(false);
	lstSinglePlayList = CAndroidGUIListBox::addAndroidGUIListBox(env, rect<s32>(10 * xScale, 30 * yScale, 350 * xScale, 400 * yScale), wSinglePlay, LISTBOX_SINGLEPLAY_LIST, true, 40 * xScale);
	lstSinglePlayList->setItemHeight(18);
	btnLoadSinglePlay = env->addButton(rect<s32>(460 * xScale, 320 * yScale, 570 * xScale, 360 * yScale), wSinglePlay, BUTTON_LOAD_SINGLEPLAY, dataManager.GetSysString(1211));
	btnSinglePlayCancel = env->addButton(rect<s32>(460 * xScale, 370 * yScale, 570 * xScale, 410 * yScale), wSinglePlay, BUTTON_CANCEL_SINGLEPLAY, dataManager.GetSysString(1210));
	env->addStaticText(dataManager.GetSysString(1352), rect<s32>(360 * xScale, 30 * yScale, 570 * xScale, 50 * yScale), false, true, wSinglePlay);
	stSinglePlayInfo = env->addStaticText(L"", rect<s32>(360 * xScale, 60 * yScale, 570 * xScale, 295 * yScale), false, true, wSinglePlay);
	//replay save
	wReplaySave = env->addWindow(rect<s32>(490 * xScale, 180 * yScale, 840 * xScale, 340 * yScale), false, dataManager.GetSysString(1340));
	wReplaySave->getCloseButton()->setVisible(false);
	wReplaySave->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1342), rect<s32>(20 * xScale, 25 * yScale, 290 * xScale, 45 * yScale), false, false, wReplaySave);
	ebRSName = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(20 * xScale, 50 * yScale, 330 * xScale, 90 * yScale), wReplaySave, -1);
	ebRSName->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnRSYes = env->addButton(rect<s32>(70 * xScale, 100 * yScale, 160 * xScale, 150 * yScale), wReplaySave, BUTTON_REPLAY_SAVE, dataManager.GetSysString(1341));
	btnRSNo = env->addButton(rect<s32>(180 * xScale, 100 * yScale, 270 * xScale, 150 * yScale), wReplaySave, BUTTON_REPLAY_CANCEL, dataManager.GetSysString(1212));
	//replay control
	wReplayControl = env->addStaticText(L"", rect<s32>(205 * xScale, 48 * yScale, 295 * xScale, 273 * yScale), true, false, 0, -1, true);
	wReplayControl->setVisible(false);
	btnReplayStart = env->addButton(rect<s32>(5 * xScale, 5 * yScale, 85 * xScale, 45 * yScale), wReplayControl, BUTTON_REPLAY_START, dataManager.GetSysString(1343));
	btnReplayPause = env->addButton(rect<s32>(5 * xScale, 50 * yScale, 85 * xScale, 90 * yScale), wReplayControl, BUTTON_REPLAY_PAUSE, dataManager.GetSysString(1344));
	btnReplayStep = env->addButton(rect<s32>(5 * xScale, 95 * yScale, 85 * xScale, 135 * yScale), wReplayControl, BUTTON_REPLAY_STEP, dataManager.GetSysString(1345));
	btnReplaySwap = env->addButton(rect<s32>(5 * xScale, 140 * yScale, 85 * xScale, 180 * yScale), wReplayControl, BUTTON_REPLAY_SWAP, dataManager.GetSysString(1346));
	btnReplayExit = env->addButton(rect<s32>(5 * xScale, 185 * yScale, 85 * xScale, 225 * yScale), wReplayControl, BUTTON_REPLAY_EXIT, dataManager.GetSysString(1347));
	//chat
	wChat = env->addWindow(rect<s32>(305 * xScale, 615 * yScale, 1020 * xScale, 640 * yScale), false, L"");
	wChat->getCloseButton()->setVisible(false);
	wChat->setDraggable(false);
	wChat->setDrawTitlebar(false);
	wChat->setVisible(false);
#else
	//replay window
	wReplay = env->addWindow(rect<s32>(220 * xScale, 100 * yScale, 800 * xScale, 520 * yScale), false, dataManager.GetSysString(1202));
	wReplay->getCloseButton()->setVisible(false);
	wReplay->setVisible(false);
	lstReplayList = env->addListBox(rect<s32>(10 * xScale, 30 * yScale, 350 * xScale, 400 * yScale), wReplay, LISTBOX_REPLAY_LIST, true);
	lstReplayList->setItemHeight(18);
	btnLoadReplay = env->addButton(rect<s32>(460 * xScale, 355 * yScale, 570 * xScale, 380 * yScale), wReplay, BUTTON_LOAD_REPLAY, dataManager.GetSysString(1348));
	btnReplayCancel = env->addButton(rect<s32>(460 * xScale, 385 * yScale, 570 * xScale, 410 * yScale), wReplay, BUTTON_CANCEL_REPLAY, dataManager.GetSysString(1347));
	env->addStaticText(dataManager.GetSysString(1349), rect<s32>(360 * xScale, 30 * yScale, 570 * xScale, 50 * yScale), false, true, wReplay);
	stReplayInfo = env->addStaticText(L"", rect<s32>(360 * xScale, 60 * yScale, 570 * xScale, 350 * yScale), false, true, wReplay);
	env->addStaticText(dataManager.GetSysString(1353), rect<s32>(360 * xScale, 275 * yScale, 570 * xScale, 295 * yScale), false, true, wReplay);

	ebRepStartTurn = env->addEditBox(L"", rect<s32>(360 * xScale, 300 * yScale, 460 * xScale, 320 * yScale), true, wReplay, -1);
	ebRepStartTurn->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	//single play window
	wSinglePlay = env->addWindow(rect<s32>(220 * xScale, 100 * yScale, 800 * xScale, 520 * yScale), false, dataManager.GetSysString(1201));
	wSinglePlay->getCloseButton()->setVisible(false);
	wSinglePlay->setVisible(false);
	lstSinglePlayList = env->addListBox(rect<s32>(10 * xScale, 30 * yScale, 350 * xScale, 400 * yScale), wSinglePlay, LISTBOX_SINGLEPLAY_LIST, true);
	lstSinglePlayList->setItemHeight(18);
	btnLoadSinglePlay = env->addButton(rect<s32>(460 * xScale, 355 * yScale, 570 * xScale, 380 * yScale), wSinglePlay, BUTTON_LOAD_SINGLEPLAY, dataManager.GetSysString(1211));
	btnSinglePlayCancel = env->addButton(rect<s32>(460 * xScale, 385 * yScale, 570 * xScale, 410 * yScale), wSinglePlay, BUTTON_CANCEL_SINGLEPLAY, dataManager.GetSysString(1210));
	env->addStaticText(dataManager.GetSysString(1352), rect<s32>(360 * xScale, 30 * yScale, 570 * xScale, 50 * yScale), false, true, wSinglePlay);
	stSinglePlayInfo = env->addStaticText(L"", rect<s32>(360 * xScale, 60 * yScale, 570 * xScale, 350 * yScale), false, true, wSinglePlay);
	//replay save
	wReplaySave = env->addWindow(rect<s32>(510 * xScale, 200 * yScale, 820 * xScale, 320 * yScale), false, dataManager.GetSysString(1340));
	wReplaySave->getCloseButton()->setVisible(false);
	wReplaySave->setVisible(false);
	env->addStaticText(dataManager.GetSysString(1342), rect<s32>(20 * xScale, 25 * yScale, 290 * xScale, 45 * yScale), false, false, wReplaySave);
	ebRSName =  env->addEditBox(L"", rect<s32>(20 * xScale, 50 * yScale, 290 * xScale, 70 * yScale), true, wReplaySave, -1);
	ebRSName->setTextAlignment(irr::gui::EGUIA_CENTER, irr::gui::EGUIA_CENTER);
	btnRSYes = env->addButton(rect<s32>(70 * xScale, 80 * yScale, 140 * xScale, 105 * yScale), wReplaySave, BUTTON_REPLAY_SAVE, dataManager.GetSysString(1341));
	btnRSNo = env->addButton(rect<s32>(170 * xScale, 80 * yScale, 240 * xScale, 105 * yScale), wReplaySave, BUTTON_REPLAY_CANCEL, dataManager.GetSysString(1212));
	//replay control
	wReplayControl = env->addStaticText(L"", rect<s32>(205 * xScale, 143 * yScale, 295 * xScale, 273 * yScale), true, false, 0, -1, true);
	wReplayControl->setVisible(false);
	btnReplayStart = env->addButton(rect<s32>(5 * xScale, 5 * yScale, 85 * xScale, 25 * yScale), wReplayControl, BUTTON_REPLAY_START, dataManager.GetSysString(1343));
	btnReplayPause = env->addButton(rect<s32>(5 * xScale, 30 * yScale, 85 * xScale, 50 * yScale), wReplayControl, BUTTON_REPLAY_PAUSE, dataManager.GetSysString(1344));
	btnReplayStep = env->addButton(rect<s32>(5 * xScale, 55 * yScale, 85 * xScale, 75 * yScale), wReplayControl, BUTTON_REPLAY_STEP, dataManager.GetSysString(1345));
	btnReplaySwap = env->addButton(rect<s32>(5 * xScale, 80 * yScale, 85 * xScale, 100 * yScale), wReplayControl, BUTTON_REPLAY_SWAP, dataManager.GetSysString(1346));
	btnReplayExit = env->addButton(rect<s32>(5 * xScale, 105 * yScale, 85 * xScale, 125 * yScale), wReplayControl, BUTTON_REPLAY_EXIT, dataManager.GetSysString(1347));
	//chat
	wChat = env->addWindow(rect<s32>(305 * xScale, 615 * yScale, 1020 * xScale, 640 * yScale), false, L"");
	wChat->getCloseButton()->setVisible(false);
	wChat->setDraggable(false);
	wChat->setDrawTitlebar(false);
	wChat->setVisible(false);
#endif
#ifdef _IRR_ANDROID_PLATFORM_
	ebChatInput = CAndroidGUIEditBox::addAndroidEditBox(L"", true, env, rect<s32>(3 * xScale, 2 * yScale, 710 * xScale, 22 * yScale), wChat, EDITBOX_CHAT);
#else
	ebChatInput = env->addEditBox(L"", rect<s32>(3 * xScale, 2 * yScale, 710 * xScale, 22 * yScale), true, wChat, EDITBOX_CHAT);
#endif
	//
	btnLeaveGame = env->addButton(rect<s32>(205 * xScale, 5 * yScale, 295 * xScale, 80 * yScale), 0, BUTTON_LEAVE_GAME, L"");
	btnLeaveGame->setVisible(false);
	device->setEventReceiver(&menuHandler);
	LoadConfig();
	env->getSkin()->setFont(guiFont);
	env->setFocus(wMainMenu);
	for (u32 i = 0; i < EGDC_COUNT; ++i) {
		SColor col = env->getSkin()->getColor((EGUI_DEFAULT_COLOR)i);
		col.setAlpha(224);
		env->getSkin()->setColor((EGUI_DEFAULT_COLOR)i, col);
	}
#ifdef _IRR_ANDROID_PLATFORM_
IGUIStaticText *text = env->addStaticText(L"",
		rect<s32>(15,15,300,60), false, false, 0, GUI_INFO_FPS );
#endif
	hideChat = false;
	hideChatTimer = 0;
	return true;
}
void Game::MainLoop() {
	wchar_t cap[256];
	camera = smgr->addCameraSceneNode(0);
	irr::core::matrix4 mProjection;
	BuildProjectionMatrix(mProjection, -0.81f, 0.44f, -0.42f, 0.42f, 1.0f, 100.0f);
	camera->setProjectionMatrix(mProjection);
	mProjection.buildCameraLookAtMatrixLH(vector3df(3.95f, 8.0f, 7.8f), vector3df(3.95f, 0, 0), vector3df(0, 0, 1));
	camera->setViewMatrixAffector(mProjection);
	smgr->setAmbientLight(SColorf(1.0f, 1.0f, 1.0f));
	float atkframe = 0.1f;
	irr::ITimer* timer = device->getTimer();
	timer->setTime(0);
#ifdef _IRR_ANDROID_PLATFORM_
	IGUIElement *stat = device->getGUIEnvironment()->getRootGUIElement()->getElementFromId ( GUI_INFO_FPS );
#endif
	int fps = 0;
	int cur_time = 0;
#if defined(_IRR_ANDROID_PLATFORM_)
	ogles2Solid = 0;
	ogles2TrasparentAlpha = 0;
	ogles2BlendTexture = 0;
	if (glversion == 0 || glversion == 2) {
		ogles2Solid = video::EMT_SOLID;
		ogles2TrasparentAlpha = video::EMT_TRANSPARENT_ALPHA_CHANNEL;
		ogles2BlendTexture = video::EMT_ONETEXTURE_BLEND;
	} else {
		io::path solidvsFileName = "media/ogles2customsolid.frag";
		io::path TACvsFileName = "media/ogles2customTAC.frag";
		io::path blendvsFileName = "media/ogles2customblend.frag";
		io::path psFileName = "media/ogles2custom.vert";
		if (!driver->queryFeature(video::EVDF_PIXEL_SHADER_1_1) &&
				!driver->queryFeature(video::EVDF_ARB_FRAGMENT_PROGRAM_1))
		{
			Printer::log("WARNING: Pixel shaders disabled "
					"because of missing driver/hardware support.");
			psFileName = "";
		}
		if (!driver->queryFeature(video::EVDF_VERTEX_SHADER_1_1) &&
				!driver->queryFeature(video::EVDF_ARB_VERTEX_PROGRAM_1))
		{
			Printer::log("WARNING: Vertex shaders disabled "
					"because of missing driver/hardware support.");
			solidvsFileName = "";
			TACvsFileName = "";
			blendvsFileName = "";
		}
		video::IGPUProgrammingServices* gpu = driver->getGPUProgrammingServices();
		if (gpu) {
			char log_custom_shader[1024];
			const video::E_GPU_SHADING_LANGUAGE shadingLanguage = video::EGSL_DEFAULT;
			ogles2Solid = gpu->addHighLevelShaderMaterialFromFiles(
					psFileName, "vertexMain", video::EVST_VS_1_1,
					solidvsFileName, "pixelMain", video::EPST_PS_1_1,
					&customShadersCallback, video::EMT_SOLID, 0, shadingLanguage);
			ogles2TrasparentAlpha = gpu->addHighLevelShaderMaterialFromFiles(
					psFileName, "vertexMain", video::EVST_VS_1_1,
					TACvsFileName, "pixelMain", video::EPST_PS_1_1,
					&customShadersCallback, video::EMT_TRANSPARENT_ALPHA_CHANNEL, 0 , shadingLanguage);
			ogles2BlendTexture = gpu->addHighLevelShaderMaterialFromFiles(
					psFileName, "vertexMain", video::EVST_VS_1_1,
					blendvsFileName, "pixelMain", video::EPST_PS_1_1,
					&customShadersCallback, video::EMT_ONETEXTURE_BLEND, 0 , shadingLanguage);
			sprintf(log_custom_shader, "ogles2Sold = %d", ogles2Solid);
			Printer::log(log_custom_shader);
			sprintf(log_custom_shader, "ogles2BlendTexture = %d", ogles2BlendTexture);
			Printer::log(log_custom_shader);
			sprintf(log_custom_shader, "ogles2TrasparentAlpha = %d", ogles2TrasparentAlpha);
			Printer::log(log_custom_shader);
		}
	}
	matManager.mCard.MaterialType = (video::E_MATERIAL_TYPE)ogles2BlendTexture;
	matManager.mTexture.MaterialType = (video::E_MATERIAL_TYPE)ogles2TrasparentAlpha;
	matManager.mBackLine.MaterialType = (video::E_MATERIAL_TYPE)ogles2BlendTexture;
	matManager.mSelField.MaterialType = (video::E_MATERIAL_TYPE)ogles2BlendTexture;
	matManager.mOutLine.MaterialType = (video::E_MATERIAL_TYPE)ogles2Solid;
	matManager.mTRTexture.MaterialType = (video::E_MATERIAL_TYPE)ogles2TrasparentAlpha;
	matManager.mATK.MaterialType = (video::E_MATERIAL_TYPE)ogles2BlendTexture;
	if (!isNPOTSupported) {
		matManager.mCard.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mCard.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		matManager.mTexture.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mTexture.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		matManager.mBackLine.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mBackLine.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		matManager.mSelField.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mSelField.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		matManager.mOutLine.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mOutLine.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		matManager.mTRTexture.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mTRTexture.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		matManager.mATK.TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
		matManager.mATK.TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
	}
	if (glversion != 0) {
		matManager.mTRTexture.setFlag(video::EMF_LIGHTING, false);
	}
#endif
	while(device->run()) {
#ifdef _IRR_ANDROID_PLATFORM_
		linePattern = (linePattern + 1) % 30;
#else
		if(gameConf.use_d3d)
			linePattern = (linePattern + 1) % 30;
		else
			linePattern = (linePattern << 1) | (linePattern >> 15);
#endif
		atkframe += 0.1f;
		atkdy = (float)sin(atkframe);
		driver->beginScene(true, true, SColor(0, 0, 0, 0));
#ifdef _IRR_ANDROID_PLATFORM_
		driver->getMaterial2D().MaterialType = (video::E_MATERIAL_TYPE)ogles2Solid;
		if (!isNPOTSupported) {
			driver->getMaterial2D().TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
			driver->getMaterial2D().TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
		}
		driver->getMaterial2D().ZBuffer = ECFN_NEVER;
		driver->enableMaterial2D(true);
		if(imageManager.tBackGround) {
			driver->draw2DImage(imageManager.tBackGround, recti(0 * xScale, 0 * yScale, 1024 * xScale, 640 * yScale), recti(0, 0, imageManager.tBackGround->getOriginalSize().Width, imageManager.tBackGround->getOriginalSize().Height));
		}
		driver->enableMaterial2D(false);
#else
		if(imageManager.tBackGround)
			driver->draw2DImage(imageManager.tBackGround, recti(0 * xScale, 0 * yScale, 1024 * xScale, 640 * yScale), recti(0, 0, imageManager.tBackGround->getOriginalSize().Width, imageManager.tBackGround->getOriginalSize().Height));
#endif
		gMutex.Lock();
		if(dInfo.isStarted) {
			DrawBackGround();
			DrawCards();
			DrawMisc();
			smgr->drawAll();
			driver->setMaterial(irr::video::IdentityMaterial);
			driver->clearZBuffer();
		} else if(is_building) {
			DrawDeckBd();
		}
		DrawGUI();
		DrawSpec();
		gMutex.Unlock();
		if(signalFrame > 0) {
			signalFrame--;
			if(!signalFrame)
				frameSignal.Set();
		}
		if(waitFrame >= 0) {
			waitFrame++;
			if(waitFrame % 90 == 0) {
				stHintMsg->setText(dataManager.GetSysString(1390));
			} else if(waitFrame % 90 == 30) {
				stHintMsg->setText(dataManager.GetSysString(1391));
			} else if(waitFrame % 90 == 60) {
				stHintMsg->setText(dataManager.GetSysString(1392));
			}
		}
		driver->endScene();
		if(closeSignal.Wait(0))
			CloseDuelWindow();
		if(!device->isWindowActive())
			ignore_chain = false;
		fps++;
		cur_time = timer->getTime();
		if(cur_time < fps * 17 - 20)
#ifdef _WIN32
			Sleep(20);
#else
			usleep(20000);
#endif
		if(cur_time >= 1000) {

#ifdef _IRR_ANDROID_PLATFORM_
			if ( stat ) {
				stringw str = L"FPS: ";
				str += (s32)device->getVideoDriver()->getFPS();
				stat->setText ( str.c_str() );
			}
#else
			myswprintf(cap, L"FPS: %d", fps);
			device->setWindowCaption(cap);
#endif
			fps = 0;
			cur_time -= 1000;
			timer->setTime(0);
			if(dInfo.time_player == 0 || dInfo.time_player == 1)
				if(dInfo.time_left[dInfo.time_player])
					dInfo.time_left[dInfo.time_player]--;
		}
#ifdef _IRR_ANDROID_PLATFORM_
		device->yield(); // probably nicer to the battery
#endif
	}
	DuelClient::StopClient(true);
	if(mainGame->dInfo.isSingleMode)
		SingleMode::StopPlay(true);
#ifdef _WIN32
	Sleep(500);
#else
	usleep(500000);
#endif
	SaveConfig();
//	device->drop();
}
void Game::BuildProjectionMatrix(irr::core::matrix4& mProjection, f32 left, f32 right, f32 bottom, f32 top, f32 znear, f32 zfar) {
	for(int i = 0; i < 16; ++i)
		mProjection[i] = 0;
	mProjection[0] = 2.0f * znear / (right - left);
	mProjection[5] = 2.0f * znear / (top - bottom);
	mProjection[8] = (left + right) / (left - right);
	mProjection[9] = (top + bottom) / (bottom - top);
	mProjection[10] = zfar / (zfar - znear);
	mProjection[11] = 1.0f;
	mProjection[14] = znear * zfar / (znear - zfar);
}
void Game::InitStaticText(irr::gui::IGUIStaticText* pControl, u32 cWidth, u32 cHeight, irr::gui::CGUITTFont* font, const wchar_t* text) {
	SetStaticText(pControl, cWidth, font, text);
	if(font->getDimension(dataManager.strBuffer).Height <= cHeight) {
		scrCardText->setVisible(false);
		return;
	}
	SetStaticText(pControl, cWidth-25, font, text);
	u32 fontheight = font->getDimension(L"A").Height + font->getKerningHeight();
	u32 step = (font->getDimension(dataManager.strBuffer).Height - cHeight) / fontheight + 1;
	scrCardText->setVisible(true);
	scrCardText->setMin(0);
	scrCardText->setMax(step);
	scrCardText->setPos(0);
}
void Game::SetStaticText(irr::gui::IGUIStaticText* pControl, u32 cWidth, irr::gui::CGUITTFont* font, const wchar_t* text, u32 pos) {
	int pbuffer = 0;
	u32 _width = 0, _height = 0;
	for(int i = 0; text[i] != 0 && i < 1023; ++i) {
		u32 w = font->getCharDimension(text[i]).Width;
		if(text[i] == L'\n') {
			dataManager.strBuffer[pbuffer++] = L'\n';
			_width = 0;
			_height++;
			if(_height == pos)
				pbuffer = 0;
			continue;
		} else if(_width > 0 && _width + w > cWidth) {
			dataManager.strBuffer[pbuffer++] = L'\n';
			_width = 0;
			_height++;
			if(_height == pos)
				pbuffer = 0;
		}
		_width += w;
		dataManager.strBuffer[pbuffer++] = text[i];
	}
	dataManager.strBuffer[pbuffer] = 0;
	pControl->setText(dataManager.strBuffer);
}
void Game::RefreshDeck(irr::gui::IGUIComboBox* cbDeck) {
	cbDeck->clear();
#ifdef _WIN32
	WIN32_FIND_DATAW fdataw;
	HANDLE fh = FindFirstFileW(L"./deck/*.ydk", &fdataw);
	if(fh == INVALID_HANDLE_VALUE)
		return;
	do {
		if(!(fdataw.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
			wchar_t* pf = fdataw.cFileName;
			while(*pf) pf++;
			while(*pf != L'.') pf--;
			*pf = 0;
			cbDeck->addItem(fdataw.cFileName);
		}
	} while(FindNextFileW(fh, &fdataw));
	FindClose(fh);
#else
	DIR * dir;
	struct dirent * dirp;
	if((dir = opendir("./deck/")) == NULL)
		return;
	while((dirp = readdir(dir)) != NULL) {
		size_t len = strlen(dirp->d_name);
		if(len < 5 || strcasecmp(dirp->d_name + len - 4, ".ydk") != 0)
			continue;
		dirp->d_name[len - 4] = 0;
		wchar_t wname[256];
		BufferIO::DecodeUTF8(dirp->d_name, wname);
		cbDeck->addItem(wname);
	}
	closedir(dir);
#endif
	for(size_t i = 0; i < cbDeck->getItemCount(); ++i) {
		if(!wcscmp(cbDeck->getItem(i), gameConf.lastdeck)) {
			cbDeck->setSelected(i);
			break;
		}
	}
}
void Game::RefreshReplay() {
	lstReplayList->clear();
#ifdef _WIN32
	WIN32_FIND_DATAW fdataw;
	HANDLE fh = FindFirstFileW(L"./replay/*.yrp", &fdataw);
	if(fh == INVALID_HANDLE_VALUE)
		return;
	do {
		if(!(fdataw.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) && Replay::CheckReplay(fdataw.cFileName)) {
			lstReplayList->addItem(fdataw.cFileName);
		}
	} while(FindNextFileW(fh, &fdataw));
	FindClose(fh);
#else
	DIR * dir;
	struct dirent * dirp;
	if((dir = opendir("./replay/")) == NULL)
		return;
	while((dirp = readdir(dir)) != NULL) {
		size_t len = strlen(dirp->d_name);
		if(len < 5 || strcasecmp(dirp->d_name + len - 4, ".yrp") != 0)
			continue;
		wchar_t wname[256];
		BufferIO::DecodeUTF8(dirp->d_name, wname);
		if(Replay::CheckReplay(wname))
			lstReplayList->addItem(wname);
	}
	closedir(dir);
#endif
}
void Game::RefreshSingleplay() {
	lstSinglePlayList->clear();
#ifdef _WIN32
	WIN32_FIND_DATAW fdataw;
	HANDLE fh = FindFirstFileW(L"./single/*.lua", &fdataw);
	if(fh == INVALID_HANDLE_VALUE)
		return;
	do {
		if(!(fdataw.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
			lstSinglePlayList->addItem(fdataw.cFileName);
	} while(FindNextFileW(fh, &fdataw));
	FindClose(fh);
#else
	DIR * dir;
	struct dirent * dirp;
	if((dir = opendir("./single/")) == NULL)
		return;
	while((dirp = readdir(dir)) != NULL) {
		size_t len = strlen(dirp->d_name);
		if(len < 5 || strcasecmp(dirp->d_name + len - 4, ".lua") != 0)
			continue;
		wchar_t wname[256];
		BufferIO::DecodeUTF8(dirp->d_name, wname);
		lstSinglePlayList->addItem(wname);
	}
	closedir(dir);
#endif
}
void Game::LoadConfig() {
	FILE* fp = fopen("system.conf", "r");
	if(!fp)
		return;
	char linebuf[256];
	char strbuf[32];
	char valbuf[256];
	wchar_t wstr[256];
	gameConf.antialias = 0;
	gameConf.serverport = 7911;
	gameConf.textfontsize = 12;
	gameConf.nickname[0] = 0;
	gameConf.gamename[0] = 0;
	gameConf.lastdeck[0] = 0;
	gameConf.numfont[0] = 0;
	gameConf.textfont[0] = 0;
	gameConf.lastip[0] = 0;
	gameConf.lastport[0] = 0;
	gameConf.roompass[0] = 0;
	fseek(fp, 0, SEEK_END);
	int fsize = ftell(fp);
	fseek(fp, 0, SEEK_SET);
	while(ftell(fp) < fsize) {
		fgets(linebuf, 250, fp);
		sscanf(linebuf, "%s = %s", strbuf, valbuf);
		if(!strcmp(strbuf, "antialias")) {
			gameConf.antialias = atoi(valbuf);
		} else if(!strcmp(strbuf, "use_d3d")) {
			gameConf.use_d3d = atoi(valbuf) > 0;
		} else if(!strcmp(strbuf, "errorlog")) {
			enable_log = atoi(valbuf);
		} else if(!strcmp(strbuf, "nickname")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.nickname, 20);
		} else if(!strcmp(strbuf, "gamename")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.gamename, 20);
		} else if(!strcmp(strbuf, "lastdeck")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.lastdeck, 64);
		} else if(!strcmp(strbuf, "textfont")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			int textfontsize;
			sscanf(linebuf, "%s = %s %d", strbuf, valbuf, &textfontsize);
			gameConf.textfontsize = textfontsize;
			BufferIO::CopyWStr(wstr, gameConf.textfont, 256);
		} else if(!strcmp(strbuf, "numfont")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.numfont, 256);
		} else if(!strcmp(strbuf, "serverport")) {
			gameConf.serverport = atoi(valbuf);
		} else if(!strcmp(strbuf, "lastip")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.lastip, 20);
		} else if(!strcmp(strbuf, "lastport")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.lastport, 20);
		} else if(!strcmp(strbuf, "roompass")) {
			BufferIO::DecodeUTF8(valbuf, wstr);
			BufferIO::CopyWStr(wstr, gameConf.roompass, 20);
		}
	}
	fclose(fp);
}
void Game::SaveConfig() {
	FILE* fp = fopen("system.conf", "w");
	fprintf(fp, "#config file\n#nickname & gamename should be less than 20 characters\n");
	char linebuf[256];
	fprintf(fp, "use_d3d = %d\n", gameConf.use_d3d ? 1 : 0);
	fprintf(fp, "antialias = %d\n", gameConf.antialias);
	fprintf(fp, "errorlog = %d\n", enable_log);
	BufferIO::CopyWStr(ebNickName->getText(), gameConf.nickname, 20);
	BufferIO::EncodeUTF8(gameConf.nickname, linebuf);
	fprintf(fp, "nickname = %s\n", linebuf);
	BufferIO::EncodeUTF8(gameConf.gamename, linebuf);
	fprintf(fp, "gamename = %s\n", linebuf);
	BufferIO::EncodeUTF8(gameConf.lastdeck, linebuf);
	fprintf(fp, "lastdeck = %s\n", linebuf);
	BufferIO::EncodeUTF8(gameConf.textfont, linebuf);
	fprintf(fp, "textfont = %s %d\n", linebuf, gameConf.textfontsize);
	BufferIO::EncodeUTF8(gameConf.numfont, linebuf);
	fprintf(fp, "numfont = %s\n", linebuf);
	fprintf(fp, "serverport = %d\n", gameConf.serverport);
	BufferIO::EncodeUTF8(gameConf.lastip, linebuf);
	fprintf(fp, "lastip = %s\n", linebuf);
	BufferIO::EncodeUTF8(gameConf.lastport, linebuf);
	fprintf(fp, "lastport = %s\n", linebuf);
	fclose(fp);
}
void Game::ShowCardInfo(int code) {
	CardData cd;
	wchar_t formatBuffer[256];
	if(!dataManager.GetData(code, &cd))
		memset(&cd, 0, sizeof(CardData));
	imgCard->setImage(imageManager.GetTexture(code));
	imgCard->setScaleImage(true);
	if(cd.alias != 0 && (cd.alias - code < 10 || code - cd.alias < 10))
		myswprintf(formatBuffer, L"%ls[%08d]", dataManager.GetName(cd.alias), cd.alias);
	else myswprintf(formatBuffer, L"%ls[%08d]", dataManager.GetName(code), code);
	stName->setText(formatBuffer);
	if(cd.type & TYPE_MONSTER) {
		myswprintf(formatBuffer, L"[%ls] %ls/%ls", dataManager.FormatType(cd.type), dataManager.FormatRace(cd.race), dataManager.FormatAttribute(cd.attribute));
		stInfo->setText(formatBuffer);
		formatBuffer[0] = L'[';
		for(unsigned int i = 1; i <= cd.level; ++i)
			formatBuffer[i] = 0x2605;
		formatBuffer[cd.level + 1] = L']';
		formatBuffer[cd.level + 2] = L' ';
		if(cd.attack < 0 && cd.defence < 0)
			myswprintf(&formatBuffer[cd.level + 3], L"?/?");
		else if(cd.attack < 0)
			myswprintf(&formatBuffer[cd.level + 3], L"?/%d", cd.defence);
		else if(cd.defence < 0)
			myswprintf(&formatBuffer[cd.level + 3], L"%d/?", cd.attack);
		else
			myswprintf(&formatBuffer[cd.level + 3], L"%d/%d", cd.attack, cd.defence);
		stDataInfo->setText(formatBuffer);
		stText->setRelativePosition(rect<s32>(15 * xScale, 83  * yScale, 287 * xScale, 324  * yScale));
		scrCardText->setRelativePosition(rect<s32>(267 * xScale, 83 * yScale, 287 * xScale, 324 * yScale));
	} else {
		myswprintf(formatBuffer, L"[%ls]", dataManager.FormatType(cd.type));
		stInfo->setText(formatBuffer);
		stDataInfo->setText(L"");
		stText->setRelativePosition(rect<s32>(15 * xScale, 60 * yScale, 287 * xScale, 324 * yScale));
		scrCardText->setRelativePosition(rect<s32>(267 * xScale, 60 * yScale, 287 * xScale, 324 * yScale));
	}
	showingtext = dataManager.GetText(code);
	const auto& tsize = stText->getRelativePosition();
	InitStaticText(stText, tsize.getWidth(), tsize.getHeight(), textFont, showingtext);
}
void Game::AddChatMsg(wchar_t* msg, int player) {
	for(int i = 7; i > 0; --i) {
		chatMsg[i] = chatMsg[i - 1];
		chatTiming[i] = chatTiming[i - 1];
		chatType[i] = chatType[i - 1];
	}
	chatMsg[0].clear();
	chatTiming[0] = 1200;
	chatType[0] = player;
	switch(player) {
	case 0: //from host
		chatMsg[0].append(dInfo.hostname);
		chatMsg[0].append(L": ");
		break;
	case 1: //from client
		chatMsg[0].append(dInfo.clientname);
		chatMsg[0].append(L": ");
		break;
	case 2: //host tag
		chatMsg[0].append(dInfo.hostname_tag);
		chatMsg[0].append(L": ");
		break;
	case 3: //client tag
		chatMsg[0].append(dInfo.clientname_tag);
		chatMsg[0].append(L": ");
		break;
	case 7: //local name
		chatMsg[0].append(mainGame->ebNickName->getText());
		chatMsg[0].append(L": ");
		break;
	case 8: //system custom message, no prefix.
		chatMsg[0].append(L"[System]: ");
		break;
	case 9: //error message
		chatMsg[0].append(L"[Script error:] ");
		break;
	default: //from watcher or unknown
		if(player < 11 || player > 19)
			chatMsg[0].append(L"[---]: ");
	}
	chatMsg[0].append(msg);
}
void Game::ClearTextures() {
	matManager.mCard.setTexture(0, 0);
	mainGame->imgCard->setImage(0);
	mainGame->btnPSAU->setImage();
	mainGame->btnPSDU->setImage();
	mainGame->btnCardSelect[0]->setImage();
	mainGame->btnCardSelect[1]->setImage();
	mainGame->btnCardSelect[2]->setImage();
	mainGame->btnCardSelect[3]->setImage();
	mainGame->btnCardSelect[4]->setImage();
	imageManager.ClearTexture();
}
void Game::CloseDuelWindow() {
	for(auto wit = fadingList.begin(); wit != fadingList.end(); ++wit) {
		if(wit->isFadein)
			wit->autoFadeoutFrame = 1;
	}
	wACMessage->setVisible(false);
	wANAttribute->setVisible(false);
	wANCard->setVisible(false);
	wANNumber->setVisible(false);
	wANRace->setVisible(false);
	wCardImg->setVisible(false);
	wCardSelect->setVisible(false);
	wCmdMenu->setVisible(false);
	wFTSelect->setVisible(false);
	wHand->setVisible(false);
	wInfos->setVisible(false);
	wMessage->setVisible(false);
	wOptions->setVisible(false);
	wPhase->setVisible(false);
	wPosSelect->setVisible(false);
	wQuery->setVisible(false);
	wReplayControl->setVisible(false);
	wReplaySave->setVisible(false);
	stHintMsg->setVisible(false);
	btnSideOK->setVisible(false);
	btnLeaveGame->setVisible(false);
	wChat->setVisible(false);
	lstLog->clear();
	logParam.clear();
	lstHostList->clear();
	DuelClient::hosts.clear();
	ClearTextures();
	closeDoneSignal.Set();
}
int Game::LocalPlayer(int player) {
	return dInfo.isFirst ? player : 1 - player;
}
const wchar_t* Game::LocalName(int local_player) {
	return local_player == 0 ? dInfo.hostname : dInfo.clientname;
}

}
