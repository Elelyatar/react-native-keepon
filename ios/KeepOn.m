#import "KeepOn.h"
#import "UIKit/UIKit.h"


@implementation KeepOn

- (instancetype)init
{
    if ((self = [super init])) {
        [[UIDevice currentDevice] setProximityMonitoringEnabled:YES];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(sensorStateChange:) name:@"UIDeviceProximityStateDidChangeNotification" object:nil];
    }
    return self;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(keepON)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    });
}

RCT_EXPORT_METHOD(keepOFF)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    });
}

RCT_EXPORT_METHOD(unkeepON)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        beginIgnoringInteractionEvents()
    });
}

RCT_EXPORT_METHOD(unkeepOFF)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        endIgnoringInteractionEvents()
    });
}

@end
