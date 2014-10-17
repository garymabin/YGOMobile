#include "config.h"
#include "game.h"
#include "data_manager.h"
#include <event2/thread.h>

#ifdef _IRR_IPHONE_PLATFORM_
#import "AppDelegate.h"
#import <Foundation/Foundation.h>

@implementation AppDelegate

@synthesize window;

static ygo::Game _game;

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
    
    [self performSelectorOnMainThread:@selector(applicationUpdate) withObject:nil waitUntilDone:NO];
}

- (void) applicationUpdate
{
	ygo::mainGame->MainLoop();
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
