#include "config.h"
#include "game.h"
#include "data_manager.h"
#include "image_manager.h"
#include "deck_manager.h"
#include <event2/thread.h>
#include "duelclient.h"
#include "netserver.h"
#include "single_mode.h"

#ifdef _IRR_IPHONE_PLATFORM_
#import "AppDelegate.h"
#import <Foundation/Foundation.h>

@implementation AppDelegate

@synthesize window;

static ygo::Game _game;
static float atkframe = 0.1f;
static int fps = 0;
static int cur_time = 0;
static irr::ITimer* timer;
static IGUIElement *stat;

- (void)applicationDidFinishLaunching:(UIApplication*)application
{
    // if you need ViewController or you don't want to see warning:
    // "Application windows are expected to have a root view controller
    // at the end of application launch" create custom UIWindow here
    // and apply your ViewController to it in following way:
    // window.rootViewController = YourViewController
    // it's important to do this step before createDevice method.
    AppDelegate* delegate = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    delegate.window = self.window;
	ygo::mainGame = &_game;
    ygo::mainGame->Initialize();
    device = ygo::mainGame->device;
    timer = device->getTimer();
	timer->setTime(0);
    ygo::mainGame->InitScene();
    stat = device->getGUIEnvironment()->getRootGUIElement()->getElementFromId (GUI_INFO_FPS);
    [self performSelectorOnMainThread:@selector(applicationUpdate) withObject:nil waitUntilDone:NO];
}

- (void) applicationUpdate
{
    while (device)
    {
        @autoreleasepool {
            while(CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.002f, TRUE) == kCFRunLoopRunHandledSource);
        }
        
        if(device->run())
        {
            _game.linePattern = (_game.linePattern + 1) % 30;
            atkframe += 0.1f;
            _game.atkdy = (float)sin(atkframe);
            _game.driver->beginScene(true, true, SColor(0, 0, 0, 0));
            _game.driver->getMaterial2D().MaterialType = (video::E_MATERIAL_TYPE)_game.ogles2Solid;
            if (!_game.isNPOTSupported) {
                _game.driver->getMaterial2D().TextureLayer[0].TextureWrapU = ETC_CLAMP_TO_EDGE;
                _game.driver->getMaterial2D().TextureLayer[0].TextureWrapV = ETC_CLAMP_TO_EDGE;
            }
            _game.driver->enableMaterial2D(true);
            _game.driver->getMaterial2D().ZBuffer = ECFN_NEVER;
            if(ygo::imageManager.tBackGround) {
                _game.driver->draw2DImage(ygo::imageManager.tBackGround, recti(0 * _game.xScale, 0 * _game.yScale, 1024 * _game.xScale, 640 * _game.yScale), recti(0, 0, ygo::imageManager.tBackGround->getOriginalSize().Width, ygo::imageManager.tBackGround->getOriginalSize().Height));
            }
            _game.gMutex.Lock();
            if(_game.dInfo.isStarted) {
                _game.DrawBackGround();
                _game.DrawCards();
                _game.DrawMisc();
                _game.smgr->drawAll();
                _game.driver->setMaterial(irr::video::IdentityMaterial);
                _game.driver->clearZBuffer();
            } else if(_game.is_building) {
                _game.DrawDeckBd();
            }
            _game.DrawGUI();
            _game.DrawSpec();
            _game.gMutex.Unlock();
            if(_game.signalFrame > 0) {
                _game. signalFrame--;
                if(!_game.signalFrame)
                    _game.frameSignal.Set();
            }
            if(_game.waitFrame >= 0) {
                _game.waitFrame++;
                if(_game.waitFrame % 90 == 0) {
                    _game.stHintMsg->setText(ygo::dataManager.GetSysString(1390));
                } else if(_game.waitFrame % 90 == 30) {
                    _game.stHintMsg->setText(ygo::dataManager.GetSysString(1391));
                } else if(_game.waitFrame % 90 == 60) {
                    _game.stHintMsg->setText(ygo::dataManager.GetSysString(1392));
                }
            }
            _game.driver->endScene();
            if(_game.closeSignal.Wait(0))
                _game.CloseDuelWindow();
            if(!device->isWindowActive())
                _game.ignore_chain = false;
            fps++;
            cur_time = timer->getTime();
            if(cur_time < fps * 17 - 20)
                usleep(20000);
            if(cur_time >= 1000) {
                if (stat) {
                    stringw str = L"FPS: ";
                    str += (s32)device->getVideoDriver()->getFPS();
                    stat->setText ( str.c_str() );
                }
                fps = 0;
                cur_time -= 1000;
                timer->setTime(0);
                if(_game.dInfo.time_player == 0 || _game.dInfo.time_player == 1)
                    if(_game.dInfo.time_left[_game.dInfo.time_player])
                        _game.dInfo.time_left[_game.dInfo.time_player]--;
            }
        }
        else
            break;
    }
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // you should pause rendering here, because some iOS versions,
    // doesn't allow to send OpenGL rendering commands, when app
    // is inactive.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // you should unpause rendering here.
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // you should pause rendering here, because some iOS versions,
    // doesn't allow to send OpenGL rendering commands, when app
    // is inactive.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // you should unpause rendering here.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    ygo::DuelClient::StopClient(true);
	if(ygo::mainGame->dInfo.isSingleMode)
        ygo::SingleMode::StopPlay(true);
    ygo::mainGame->SaveConfig();
    ygo::mainGame->device->drop();
}

@end

#endif

int enable_log = 0;
bool exit_on_return = false;

#if defined _IRR_ANDROID_PLATFORM_
void android_main(android_app* app) {
	app->inputPollSource.process = android::process_input;
	app_dummy();
#else
int main(int argc, char* argv[]) {
#ifdef _IRR_IPHONE_PLATFORM_
    @autoreleasepool {
        return UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
    }
#endif
#endif
#if not defined(_IRR_IPHONE_PLATFORM_)
#ifdef _WIN32
	WORD wVersionRequested;
	WSADATA wsaData;
	wVersionRequested = MAKEWORD(2, 2);
	WSAStartup(wVersionRequested, &wsaData);
	evthread_use_windows_threads();
#else
	evthread_use_pthreads();
#endif //_WIN32
	ygo::Game _game;
	ygo::mainGame = &_game;
#ifdef _IRR_ANDROID_PLATFORM_
	if(!ygo::mainGame->Initialize(app))
		return;
#else
	if(!ygo::mainGame->Initialize())
		return 0;
#endif
#if not defined(_IRR_ANDROID_PLATFORM_)
	for(int i = 1; i < argc; ++i) {
		/*command line args:
		 * -j: join host (host info from system.conf)
		 * -d: deck edit
		 * -r: replay */
		if(argv[i][0] == '-' && argv[i][1] == 'e') {
			ygo::dataManager.LoadDB(&argv[i][2]);
		} else if(!strcmp(argv[i], "-j") || !strcmp(argv[i], "-d") || !strcmp(argv[i], "-r") || !strcmp(argv[i], "-s")) {
			exit_on_return = true;
			irr::SEvent event;
			event.EventType = irr::EET_GUI_EVENT;
			event.GUIEvent.EventType = irr::gui::EGET_BUTTON_CLICKED;
			if(!strcmp(argv[i], "-j")) {
				event.GUIEvent.Caller = ygo::mainGame->btnLanMode;
				ygo::mainGame->device->postEventFromUser(event);
				//TODO: wait for wLanWindow show. if network connection faster than wLanWindow, wLanWindow will still show on duel scene.
				event.GUIEvent.Caller = ygo::mainGame->btnJoinHost;
				ygo::mainGame->device->postEventFromUser(event);
			} else if(!strcmp(argv[i], "-d")) {
				event.GUIEvent.Caller = ygo::mainGame->btnDeckEdit;
				ygo::mainGame->device->postEventFromUser(event);
			} else if(!strcmp(argv[i], "-r")) {
				event.GUIEvent.Caller = ygo::mainGame->btnReplayMode;
				ygo::mainGame->device->postEventFromUser(event);
				ygo::mainGame->lstReplayList->setSelected(0);
				event.GUIEvent.Caller = ygo::mainGame->btnLoadReplay;
				ygo::mainGame->device->postEventFromUser(event);
			} else if(!strcmp(argv[i], "-s")) {
				event.GUIEvent.Caller = ygo::mainGame->btnServerMode;
				ygo::mainGame->device->postEventFromUser(event);
				ygo::mainGame->lstSinglePlayList->setSelected(0);
				event.GUIEvent.Caller = ygo::mainGame->btnLoadSinglePlay;
				ygo::mainGame->device->postEventFromUser(event);
			}

		}
	}
#endif
#ifdef _IRR_ANDROID_PLATFORM_
	ygo::mainGame->externalSignal.Set();
	ygo::mainGame->externalSignal.SetNoWait(true);
#endif
	ygo::mainGame->MainLoop();
#ifdef _WIN32
	WSACleanup();
#else

#endif //_WIN32
#if defined _IRR_ANDROID_PLATFORM_
	return;
#else
	return EXIT_SUCCESS;
#endif
#endif
}
