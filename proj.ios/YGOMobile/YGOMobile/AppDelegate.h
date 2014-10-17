//
//  AppDelegate.h
//  YGOMobile
//
//  Created by 马彬 on 14-4-14.
//  Copyright (c) 2014年 garymabin@gmail.com. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>
{
    UIWindow* window;
    IrrlichtDevice* device;
}

@property (strong, nonatomic) IBOutlet UIWindow *window;

@end
