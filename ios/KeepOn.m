#import "KeepOn.h"
#import "UIKit/UIKit.h"


@implementation KeepOn

- (instancetype)init
{
    if ((self = [super init])) {
        [[UIDevice currentDevice] setProximityMonitoringEnabled:YES];
        
    }
    return self;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(activate)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    });
}

RCT_EXPORT_METHOD(deactivate)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
    });
}

RCT_EXPORT_METHOD(turnScreenOn)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    });
}

RCT_EXPORT_METHOD(turnScreenOff)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(sensorStateChange:) name:@"UIDeviceProximityStateDidChangeNotification" object:nil];
    });
}

@end
