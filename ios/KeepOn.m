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

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)sensorStateChange:(NSNotificationCenter *)notification
{
    BOOL proximityState = [[UIDevice currentDevice] proximityState];
    [_bridge.eventDispatcher sendDeviceEventWithName:@"proximityStateDidChange"
                                                body:@{@"proximity": @(proximityState)}];
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

RCT_EXPORT_METHOD(startProximitySensor) {
  [[UIDevice currentDevice] setProximityMonitoringEnabled:true];
}

RCT_EXPORT_METHOD(stopProximitySensor) {
  [[UIDevice currentDevice] setProximityMonitoringEnabled:false];
}

@end
