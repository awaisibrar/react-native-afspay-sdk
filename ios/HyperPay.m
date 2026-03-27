
#import <Foundation/Foundation.h>
#import "HyperPay.h"
#import <React/RCTLog.h>

@implementation HyperPay

OPPPaymentProvider *provider;
NSString *shopperResultURL = @"";
NSString *merchantIdentifier = @"";
NSString *countryCode = @"";
NSString *mode=@"TestMode";
NSArray *supportedNetworks;
NSString *companyName=@"";

RCT_EXPORT_MODULE(HyperPay)

-(instancetype)init
{
    self = [super init];
    if (self) {
        provider = [OPPPaymentProvider paymentProviderWithMode:OPPProviderModeTest];
        provider.threeDSEventListener = self;
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onTransactionComplete",@"onProgress"];
}

/**
 React Native functions
 */


RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(setup: (NSDictionary*)options) {
    shopperResultURL=[options valueForKey:@"shopperResultURL"];
    if ([options valueForKey:@"merchantIdentifier"])
        merchantIdentifier=[options valueForKey:@"merchantIdentifier"];
    if ([options valueForKey:@"companyName"])
        companyName=[options valueForKey:@"companyName"];
    if ([options valueForKey:@"countryCode"])
       countryCode=[options valueForKey:@"countryCode"];
    if ([options valueForKey:@"supportedNetworks"])
        supportedNetworks=[options valueForKey:@"supportedNetworks"];
    if ([[options valueForKey:@"mode"] isEqual:@"LiveMode"])
      provider = [OPPPaymentProvider paymentProviderWithMode:OPPProviderModeLive];
    else
      provider = [OPPPaymentProvider paymentProviderWithMode:OPPProviderModeTest];

    provider.threeDSEventListener = self;
    return options;
}


RCT_EXPORT_METHOD(createPaymentTransaction: (NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  NSError * _Nullable error;

    OPPCardPaymentParams *params = [OPPCardPaymentParams cardPaymentParamsWithCheckoutID:[options valueForKey:@"checkoutID"]
                                                                        paymentBrand:[options valueForKey:@"paymentBrand"]
                                                                              holder:[options valueForKey:@"holderName"]
                                                                              number:[options valueForKey:@"cardNumber"]
                                                                         expiryMonth:[options valueForKey:@"expiryMonth"]
                                                                          expiryYear:[options valueForKey:@"expiryYear"]
                                                                                 CVV:[options valueForKey:@"cvv"]
                                                                               error:&error];

    if (error) {
      NSLog(@"%s", "error");
      reject(@"createTransaction",error.localizedDescription, error);

    } else {
       params.shopperResultURL =shopperResultURL;
       
      OPPTransaction *transaction = [OPPTransaction transactionWithPaymentParams:params];

      [provider submitTransaction:transaction completionHandler:^(OPPTransaction * _Nonnull transaction, NSError * _Nullable error) {
        NSDictionary *transactionResult;
        if (transaction.type == OPPTransactionTypeAsynchronous) {
            
           transactionResult = @{
          @"redirectURL":transaction.redirectURL.absoluteString,
          @"status":@"pending",
          @"checkoutId":transaction.paymentParams.checkoutID
          };
          resolve(transactionResult);

        }  else if (transaction.type == OPPTransactionTypeSynchronous) {

          transactionResult = @{
          @"status":@"completed",
          @"checkoutId":transaction.paymentParams.checkoutID
          };
          resolve(transactionResult);
        } else {
          reject(@"createTransaction",error.localizedDescription, error);
        }
      }];
    }
}



RCT_EXPORT_METHOD(registerCard:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  NSError *error;
  OPPCardPaymentParams *params = [OPPCardPaymentParams cardPaymentParamsWithCheckoutID:[options valueForKey:@"checkoutID"]
                                                                          paymentBrand:[options valueForKey:@"paymentBrand"]
                                                                                holder:[options valueForKey:@"holderName"]
                                                                                number:[options valueForKey:@"cardNumber"]
                                                                           expiryMonth:[options valueForKey:@"expiryMonth"]
                                                                            expiryYear:[options valueForKey:@"expiryYear"]
                                                                                   CVV:[options valueForKey:@"cvv"]
                                                                                 error:&error];
  if (error) {
    reject(@"registerCard", error.localizedDescription, error);
    return;
  }

  if ([options valueForKey:@"shopperResultURL"])
    params.shopperResultURL = [options valueForKey:@"shopperResultURL"];
  else
    params.shopperResultURL = shopperResultURL;

  OPPTransaction *transaction = [OPPTransaction transactionWithPaymentParams:params];
  [provider registerTransaction:transaction completionHandler:^(OPPTransaction * _Nonnull transaction, NSError * _Nullable error) {
    if (error) {
      reject(@"registerCard", error.localizedDescription, error);
    } else {
      resolve(@{
        @"status": @"completed",
        @"checkoutId": transaction.paymentParams.checkoutID
      });
    }
  }];
}


RCT_EXPORT_METHOD(payWithToken:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  NSError *error;
  OPPTokenPaymentParams *params = [OPPTokenPaymentParams tokenPaymentParamsWithCheckoutID:[options valueForKey:@"checkoutID"]
                                                                                  tokenID:[options valueForKey:@"tokenID"]
                                                                             paymentBrand:[options valueForKey:@"paymentBrand"]
                                                                                    error:&error];
  if (error) {
    reject(@"payWithToken", error.localizedDescription, error);
    return;
  }

  // if ([options valueForKey:@"cvv"])
  //   params.cvv = [options valueForKey:@"cvv"];

  if ([options valueForKey:@"shopperResultURL"])
    params.shopperResultURL = [options valueForKey:@"shopperResultURL"];
  else
    params.shopperResultURL = shopperResultURL;

  OPPTransaction *transaction = [OPPTransaction transactionWithPaymentParams:params];
  [provider submitTransaction:transaction completionHandler:^(OPPTransaction * _Nonnull transaction, NSError * _Nullable error) {
    if (transaction.type == OPPTransactionTypeAsynchronous) {
      resolve(@{
        @"redirectURL": transaction.redirectURL.absoluteString,
        @"status": @"pending",
        @"checkoutId": transaction.paymentParams.checkoutID
      });
    } else if (transaction.type == OPPTransactionTypeSynchronous) {
      resolve(@{
        @"status": @"completed",
        @"checkoutId": transaction.paymentParams.checkoutID
      });
    } else {
      reject(@"payWithToken", error.localizedDescription, error);
    }
  }];
}


RCT_EXPORT_METHOD(applePay:(NSDictionary*)params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  
  OPPCheckoutSettings *checkoutSettings = [[OPPCheckoutSettings alloc] init];
  PKPaymentRequest *paymentRequest = [OPPPaymentProvider paymentRequestWithMerchantIdentifier:merchantIdentifier countryCode:countryCode];
  paymentRequest.supportedNetworks = supportedNetworks;
  

    if ([params valueForKey:@"companyName"]){
        companyName=[params valueForKey:@"companyName"];
       }
        NSDecimalNumber *amount = [NSDecimalNumber decimalNumberWithMantissa:[[params valueForKey:@"amount"] intValue] exponent:-2 isNegative:NO];
        paymentRequest.paymentSummaryItems = @[[PKPaymentSummaryItem summaryItemWithLabel:companyName amount:amount]];
 
    
  checkoutSettings.shopperResultURL=shopperResultURL;
  checkoutSettings.applePayPaymentRequest = paymentRequest;
  OPPCheckoutProvider *checkoutProvider = [OPPCheckoutProvider checkoutProviderWithPaymentProvider:provider
                                                                                        checkoutID:[params valueForKey:@"checkoutID"]
                                                                                          settings:checkoutSettings];

  [checkoutProvider presentCheckoutWithPaymentBrand:@"APPLEPAY"
    loadingHandler:^(BOOL inProgress) {
      [self sendEventWithName:@"onProgress" body:@(inProgress)];
      // Executed whenever SDK sends request to the server or receives the response.
      // You can start or stop loading animation based on inProgress parameter.
  } completionHandler:^(OPPTransaction * _Nullable transaction, NSError * _Nullable error) {
      if (error) {
//          reject(@"applePay",checkoutID,error);
        reject(@"applePay",error.localizedDescription, error);
          // See code attribute (OPPErrorCode) and NSLocalizedDescription to identify the reason of failure.
      } else {
          if (transaction.redirectURL)
              resolve(@{@"redirectURL": transaction.redirectURL.absoluteString});
          else
              resolve(@{@"resourcePath": transaction.resourcePath});
      }
  } cancelHandler:^{
       reject(@"applePay",@"cancel",NULL);
      // Executed if the shopper closes the payment page prematurely.
  }];

}

RCT_EXPORT_METHOD(googlePay:(NSDictionary*)params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  reject(@"GOOGLE_PAY_UNSUPPORTED", @"Google Pay is only supported on Android", nil);
}

#pragma mark - OPPThreeDSWorkflowListener

- (void)onThreeDSChallengeRequiredWithCompletion:(void (^)(UINavigationController * _Nonnull))completion {
  dispatch_async(dispatch_get_main_queue(), ^{
    
    UIWindow *window = UIApplication.sharedApplication.windows.firstObject;
    UIViewController *rootVC = window.rootViewController;

    // Get top-most presented VC
    while (rootVC.presentedViewController) {
      rootVC = rootVC.presentedViewController;
    }

    // 🚨 This is the correct approach for Expo
    completion((UINavigationController *)rootVC);
  });
}

- (void)onThreeDSConfigRequiredWithCompletion:(void (^)(OPPThreeDSConfig * _Nonnull))completion {
  OPPThreeDSConfig *config = [[OPPThreeDSConfig alloc] init];
  completion(config);
}

@end


