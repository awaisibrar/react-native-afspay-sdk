package com.reactnativeafspay;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings;
import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.exception.PaymentException;
import com.oppwa.mobile.connect.payment.BrandsValidation;
import com.oppwa.mobile.connect.payment.CheckoutInfo;
import com.oppwa.mobile.connect.payment.ImagesRequest;
import com.oppwa.mobile.connect.payment.PaymentParams;
import com.oppwa.mobile.connect.payment.card.CardPaymentParams;
import com.oppwa.mobile.connect.payment.token.TokenPaymentParams;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.ITransactionListener;
import com.oppwa.mobile.connect.provider.OppPaymentProvider;
import com.oppwa.mobile.connect.provider.ThreeDSWorkflowListener;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;
import com.oppwa.mobile.connect.provider.threeds.v2.model.ThreeDSConfig;
import com.oppwa.mobile.connect.utils.googlepay.CardPaymentMethodJsonBuilder;
import com.oppwa.mobile.connect.utils.googlepay.PaymentDataRequestJsonBuilder;
import com.oppwa.mobile.connect.utils.googlepay.TransactionInfoJsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;

@ReactModule(name = HyperPayModule.NAME)
public class HyperPayModule extends ReactContextBaseJavaModule implements ITransactionListener, ActivityEventListener {
    public static final String NAME = "HyperPay";
    private static final int REQUEST_CODE_GOOGLE_PAY = 1001;

    private Context appContext;
    private Promise promisePaymentTransaction;
    private Promise promiseGooglePay;
    private String shopperResultURL;
    private String merchantIdentifier;
    private String countryCode;
    private String mode;

    public HyperPayModule(ReactApplicationContext reactContext) {
        super(reactContext);
        appContext = reactContext.getApplicationContext();
        reactContext.addActivityEventListener(this);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public WritableMap setup(ReadableMap params) {
        WritableMap config = Arguments.createMap();
        if (params.hasKey("shopperResultURL"))
            shopperResultURL = params.getString("shopperResultURL");
        if (params.hasKey("merchantIdentifier"))
            merchantIdentifier = params.getString("merchantIdentifier");
        if (params.hasKey("countryCode"))
            countryCode = params.getString("countryCode");
        if (params.hasKey("mode"))
            mode = params.getString("mode");
        config.putString("shopperResultURL", shopperResultURL);
        config.putString("merchantIdentifier", merchantIdentifier);
        config.putString("countryCode", countryCode);
        config.putString("mode", mode);
        return config;
    }

    @ReactMethod
    public void createPaymentTransaction(ReadableMap params, Promise promise) {
        promisePaymentTransaction = promise;
        this.emitListeners("onProgress", true);
        try {
            CardPaymentParams cardParams = new CardPaymentParams(
                    params.getString("checkoutID"),
                    params.getString("paymentBrand"),
                    params.getString("cardNumber"),
                    params.getString("holderName"),
                    params.getString("expiryMonth"),
                    params.getString("expiryYear"),
                    params.getString("cvv"));

            if (params.hasKey("tokenizationEnabled") && params.getBoolean("tokenizationEnabled")) {
                cardParams.setTokenizationEnabled(true);
            }
            if (params.hasKey("shopperResultURL")) {
                shopperResultURL = params.getString("shopperResultURL");
            }
            cardParams.setShopperResultUrl(shopperResultURL);

            try {
                OppPaymentProvider paymentProvider = buildProvider();
                Transaction transaction = new Transaction(cardParams);
                paymentProvider.submitTransaction(transaction, this);
            } catch (PaymentException e) {
                this.emitListeners("onProgress", false);
                promisePaymentTransaction.reject(e);
            }
        } catch (PaymentException e) {
            this.emitListeners("onProgress", false);
            promisePaymentTransaction.reject(e);
        }
    }

    @ReactMethod
    public void registerCard(ReadableMap params, Promise promise) {
        promisePaymentTransaction = promise;
        this.emitListeners("onProgress", true);
        try {
            CardPaymentParams cardParams = new CardPaymentParams(
                    params.getString("checkoutID"),
                    params.getString("paymentBrand"),
                    params.getString("cardNumber"),
                    params.getString("holderName"),
                    params.getString("expiryMonth"),
                    params.getString("expiryYear"),
                    params.getString("cvv"));

            if (params.hasKey("shopperResultURL")) {
                shopperResultURL = params.getString("shopperResultURL");
            }
            cardParams.setShopperResultUrl(shopperResultURL);

            try {
                OppPaymentProvider paymentProvider = buildProvider();
                Transaction transaction = new Transaction(cardParams);
                paymentProvider.registerTransaction(transaction, this);
            } catch (PaymentException e) {
                this.emitListeners("onProgress", false);
                promisePaymentTransaction.reject(e);
            }
        } catch (PaymentException e) {
            this.emitListeners("onProgress", false);
            promisePaymentTransaction.reject(e);
        }
    }

    @ReactMethod
    public void payWithToken(ReadableMap params, Promise promise) {
        promisePaymentTransaction = promise;
        this.emitListeners("onProgress", true);
        try {
            String checkoutID = params.getString("checkoutID");
            String tokenID = params.getString("tokenID");
            String paymentBrand = params.getString("paymentBrand");

            TokenPaymentParams tokenParams = new TokenPaymentParams(checkoutID, tokenID, paymentBrand);

            if (params.hasKey("shopperResultURL")) {
                shopperResultURL = params.getString("shopperResultURL");
            }
            tokenParams.setShopperResultUrl(shopperResultURL);

            try {
                OppPaymentProvider paymentProvider = buildProvider();
                Transaction transaction = new Transaction(tokenParams);
                paymentProvider.submitTransaction(transaction, this);
            } catch (PaymentException e) {
                this.emitListeners("onProgress", false);
                promisePaymentTransaction.reject(e);
            }
        } catch (PaymentException e) {
            this.emitListeners("onProgress", false);
            promisePaymentTransaction.reject(e);
        }
    }

    private OppPaymentProvider buildProvider() {
        OppPaymentProvider paymentProvider = new OppPaymentProvider(
                appContext,
                "LiveMode".equals(mode) ? Connect.ProviderMode.LIVE : Connect.ProviderMode.TEST);
        paymentProvider.setThreeDSWorkflowListener(new ThreeDSWorkflowListener() {
            @Override
            public Activity onThreeDSChallengeRequired() {
                return getCurrentActivity();
            }

            @Override
            public ThreeDSConfig onThreeDSConfigRequired() {
                return new ThreeDSConfig.Builder().build();
            }
        });
        return paymentProvider;
    }

    @ReactMethod
    public void checkThreeDS2Status(Promise promise) {
        boolean wasKilled = false;
        try {
            Class<?> clazz = Class.forName("com.oppwa.mobile.connect.provider.threeds.ThreeDS2Service");
            java.lang.reflect.Method method = clazz.getMethod("wasTransactionKilled");
            Object result = method.invoke(null);
            if (result instanceof Boolean) {
                wasKilled = (Boolean) result;
            }
        } catch (Exception e) {
            Log.e("HyperPayModule", "checkThreeDS2Status: ThreeDS2Service not available", e);
        }
        WritableMap result = Arguments.createMap();
        result.putBoolean("wasTransactionKilled", wasKilled);
        promise.resolve(result);
    }

    private void emitListeners(String eventName, boolean isLoading) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onProgress", isLoading);
    }

    @Override
    public void transactionCompleted(@NonNull Transaction transaction) {
        this.emitListeners("onProgress", false);

        WritableMap paymentResponse = Arguments.createMap();
        paymentResponse.putString("checkoutId", transaction.getPaymentParams().getCheckoutId());

        if (transaction.getTransactionType() == TransactionType.SYNC) {
            paymentResponse.putString("status", "completed");
        } else {
            paymentResponse.putString("status", "pending");
            paymentResponse.putString("redirectURL", transaction.getRedirectUrl());
        }

        promisePaymentTransaction.resolve(paymentResponse);
    }

    @Override
    public void transactionFailed(@NonNull Transaction transaction, @NonNull PaymentError paymentError) {
        this.emitListeners("onProgress", false);
        promisePaymentTransaction.reject(paymentError.getErrorInfo());
    }

    @Override
    public void brandsValidationRequestSucceeded(@NonNull BrandsValidation brandsValidation) {
        ITransactionListener.super.brandsValidationRequestSucceeded(brandsValidation);
    }

    @Override
    public void brandsValidationRequestFailed(@NonNull PaymentError paymentError) {
        ITransactionListener.super.brandsValidationRequestFailed(paymentError);
    }

    @Override
    public void paymentConfigRequestSucceeded(@NonNull CheckoutInfo checkoutInfo) {
        Log.d("paymentCond", checkoutInfo.getResourcePath());
        ITransactionListener.super.paymentConfigRequestSucceeded(checkoutInfo);
    }

    @Override
    public void paymentConfigRequestFailed(@NonNull PaymentError paymentError) {
        ITransactionListener.super.paymentConfigRequestFailed(paymentError);
    }

    @Override
    public void imagesRequestSucceeded(@NonNull ImagesRequest imagesRequest) {
        ITransactionListener.super.imagesRequestSucceeded(imagesRequest);
    }

    @Override
    public void imagesRequestFailed() {
        ITransactionListener.super.imagesRequestFailed();
    }

    @Override
    public void binRequestSucceeded(@NonNull String[] strings) {
        ITransactionListener.super.binRequestSucceeded(strings);
    }

    @Override
    public void binRequestFailed() {
        ITransactionListener.super.binRequestFailed();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_GOOGLE_PAY || promiseGooglePay == null) {
            return;
        }
        this.emitListeners("onProgress", false);
        Promise promise = promiseGooglePay;
        promiseGooglePay = null;

        if (resultCode == CheckoutActivity.RESULT_OK && data != null) {
            try {
                WritableMap result = Arguments.createMap();
                if (data.hasExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH)) {
                    result.putString("resourcePath", data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH));
                }
                if (data.hasExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION)) {
                    Transaction transaction = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);
                    if (transaction != null && transaction.getPaymentParams() != null) {
                        result.putString("transactionId", transaction.getPaymentParams().getCheckoutId());
                        result.putString("status", transaction.getTransactionType() == TransactionType.SYNC ? "completed" : "pending");
                    }
                }
                if (data.hasExtra(CheckoutActivity.EXTRA_CHECKOUT_ID)) {
                    result.putString("checkoutId", data.getStringExtra(CheckoutActivity.EXTRA_CHECKOUT_ID));
                }
                promise.resolve(result);
            } catch (Exception e) {
                promise.reject("GOOGLE_PAY_ERROR", e.getMessage());
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            promise.reject("GOOGLE_PAY_CANCELED", "User canceled Google Pay");
        } else {
            String errorMsg = "Google Pay failed";
            if (data != null && data.hasExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR)) {
                errorMsg = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR);
            }
            promise.reject("GOOGLE_PAY_ERROR",resultCode + errorMsg);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // No-op for checkout
    }

    @ReactMethod
    public void googlePay(ReadableMap params, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("GOOGLE_PAY_ERROR", "Activity not available");
            return;
        }

        String checkoutID = params.hasKey("checkoutID") ? params.getString("checkoutID") : null;
        String amount = params.hasKey("amount") ? params.getString("amount") : "0";
        String currencyCode = params.hasKey("currencyCode") ? params.getString("currencyCode") : "USD";
        String gatewayMerchantId = params.hasKey("gatewayMerchantId") ? params.getString("gatewayMerchantId") : (params.hasKey("entityId") ? params.getString("entityId") : null);

        if (checkoutID == null || checkoutID.isEmpty()) {
            promise.reject("GOOGLE_PAY_ERROR", "checkoutID is required");
            return;
        }

        try {
            Connect.ProviderMode providerMode = "LiveMode".equals(mode) ? Connect.ProviderMode.LIVE : Connect.ProviderMode.TEST;

            Set<String> paymentBrands = new HashSet<>();
            paymentBrands.add("GOOGLEPAY");

            CheckoutSettings checkoutSettings = new CheckoutSettings(checkoutID, paymentBrands, providerMode);

            // Build Google Pay PaymentDataRequest JSON
            JSONArray allowedAuthMethods = new JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS");
            JSONArray allowedCardNetworks = new JSONArray().put("VISA").put("MASTERCARD").put("AMEX").put("DISCOVER").put("JCB");

            CardPaymentMethodJsonBuilder cardMethodBuilder = new CardPaymentMethodJsonBuilder()
                    .setAllowedAuthMethods(allowedAuthMethods)
                    .setAllowedCardNetworks(allowedCardNetworks);
            if (gatewayMerchantId != null && !gatewayMerchantId.isEmpty()) {
                cardMethodBuilder.setGatewayMerchantId(gatewayMerchantId);
            }

            JSONArray allowedPaymentMethodsJson = new JSONArray().put(cardMethodBuilder.toJson());
            JSONObject transactionInfoJson = new TransactionInfoJsonBuilder()
                    .setCurrencyCode(currencyCode)
                    .setTotalPriceStatus("FINAL")
                    .setTotalPrice(amount)
                    .toJson();

            JSONObject paymentDataRequestJson = new PaymentDataRequestJsonBuilder()
                    .setAllowedPaymentMethods(allowedPaymentMethodsJson)
                    .setTransactionInfo(transactionInfoJson)
                    .toJson();

            checkoutSettings.setGooglePayPaymentDataRequestJson(paymentDataRequestJson.toString());

            Intent checkoutIntent = new Intent(getReactApplicationContext(), CheckoutActivity.class);
            checkoutIntent.putExtra(CheckoutActivity.CHECKOUT_SETTINGS, checkoutSettings);

            promiseGooglePay = promise;
            this.emitListeners("onProgress", true);
            currentActivity.startActivityForResult(checkoutIntent, REQUEST_CODE_GOOGLE_PAY);
        } catch (Exception e) {
            Log.e("HyperPayModule", "Google Pay setup failed", e);
            promise.reject("GOOGLE_PAY_ERROR", e.getMessage());
        }
    }

}
