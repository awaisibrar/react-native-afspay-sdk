# react-native-afspay-sdk

React Native bridge for the HyperPay (OPPWA) payment SDK. Supports card payments and Apple Pay on iOS and Android.

## Installation

```sh
npm install react-native-afspay-sdk
# or
yarn add react-native-afspay-sdk
```

### iOS

After installing, run:

```sh
cd ios && pod install && cd ..
```

Then rebuild your app.

### Android

No extra steps. Rebuild your app after adding the package.

### Expo

This library uses native code and is **not** compatible with the Expo managed workflow. Use a development build or bare workflow.

## Usage

### 1. Initialize (required first step)

Call `init` with your configuration before any payment.

```js
import AfsPay from 'react-native-afspay-sdk';

AfsPay.init({
  shopperResultURL: 'myapp://payment/callback',
  countryCode: 'SA',
  merchantIdentifier: 'merchant.com.yourapp', // required for Apple Pay
  mode: 'TestMode', // or 'LiveMode'
  companyName: 'Your Store Name', // shown on Apple Pay sheet
  supportedNetworks: ['VISA', 'MASTERCARD'], // iOS Apple Pay only
});
```

| Option               | Type     | Required | Description                                                                 |
|----------------------|----------|----------|-----------------------------------------------------------------------------|
| `shopperResultURL`   | string   | Yes      | URL scheme to redirect to after payment (e.g. `yourapp://payment/done`).  |
| `countryCode`        | string   | No       | Country code (e.g. `SA`, `US`). Required for Apple Pay.                    |
| `merchantIdentifier` | string   | No       | Apple Pay merchant ID. Required for Apple Pay.                             |
| `mode`               | string   | No       | `'TestMode'` or `'LiveMode'`. Default: `'TestMode'`.                       |
| `companyName`        | string   | No       | Label on Apple Pay payment sheet.                                          |
| `supportedNetworks`  | string[] | No       | Card networks for Apple Pay (e.g. `['VISA','MASTERCARD']`). iOS only.       |

### 2. Apple Pay

```js
const result = await AfsPay.applePay(
  {
    checkoutID: 'your-checkout-id-from-backend',
    companyName: 'Your Store',
    amount: '10000', // amount in smallest unit (e.g. 100.00)
  },
  (isLoading) => {
    // optional: show/hide loading UI
  }
);

// result: { redirectURL?: string, resourcePath?: string }
// Use redirectURL or resourcePath + checkout ID to confirm status on your server
```

### 3. Google Pay (Android only)

On iOS, `googlePay` rejects with an error. Use only on Android.

```js
const result = await AfsPay.googlePay(
  {
    checkoutID: 'your-checkout-id-from-backend',
    amount: '100.00',
    currencyCode: 'USD',
    gatewayMerchantId: 'yourEntityId', // or entityId — from your HyperPay/OPPWA config
  },
  (isLoading) => {
    // optional: show/hide loading UI
  }
);

// result: { resourcePath?: string, transactionId?: string, checkoutId?: string }
// Use resourcePath or checkoutId to confirm status on your server
```

| Option               | Type   | Required | Description                                      |
|----------------------|--------|----------|--------------------------------------------------|
| `checkoutID`         | string | Yes      | Checkout ID from your backend.                   |
| `amount`             | string | No       | Total amount (e.g. `"100.00"`). Default: `"0"`. |
| `currencyCode`       | string | No       | Currency code (e.g. `"USD"`). Default: `"USD"`. |
| `gatewayMerchantId`  | string | No       | Entity ID / gateway merchant ID from HyperPay.   |
| `entityId`           | string | No       | Alias for `gatewayMerchantId`.                   |

### 4. Card payment (create transaction)

```js
const result = await AfsPay.createPaymentTransaction(
  {
    paymentBrand: 'VISA',
    holderName: 'John Doe',
    cardNumber: '4111111111111111',
    expiryYear: '2027',
    expiryMonth: '12',
    cvv: '123',
    checkoutID: 'checkout-id-from-backend',
    shopperResultURL: 'myapp://payment/callback', // optional override
  },
  (isLoading) => {
    // optional: show/hide loading UI
  }
);

// result: { status, checkoutId, redirectURL }
```

### 5. Get payment status

After redirect or when you have a transaction resource path or status code:

```js
const status = await AfsPay.getPaymentStatus('000.000.000');

// status: { code, description, status: 'successfully' | 'rejected' | ... }
```

### 6. Loading hook (optional)

```js
import AfsPay, { useTransactionLoading } from 'react-native-afspay-sdk';

function CheckoutScreen() {
  const loading = useTransactionLoading();
  // use loading to show a spinner during payment requests
}
```

### 8. 3D Secure 2 (3DS2)

3DS2 works **automatically** — no extra JS code is needed for the challenge flow. The SDK handles it natively inside `createPaymentTransaction`, `registerCard`, and `payWithToken`.

**What happens under the hood:**

- **iOS**: `HyperPay` conforms to `OPPThreeDSWorkflowListener`. When the issuer requires a challenge, the SDK calls `onThreeDSChallengeRequired` and presents the native challenge screen in the app's navigation controller. When it needs config, `onThreeDSConfigRequired` returns a default `OPPThreeDSConfig`.

- **Android**: `buildProvider()` sets a `ThreeDSWorkflowListener` that returns the current `Activity` for the challenge screen and a default `ThreeDSConfig`.

**Handle app killed during 3DS (Android only):**

If the app goes to the background during a 3DS challenge, Android may kill it. On restart, call `checkThreeDS2Status` to detect this and request the payment status from your server.

```js
import { useEffect } from 'react';
import AfsPay from 'react-native-afspay-sdk';

// In your app entry / payment screen on mount:
useEffect(() => {
  AfsPay.checkThreeDS2Status().then(({ wasTransactionKilled }) => {
    if (wasTransactionKilled) {
      // The 3DS flow was interrupted — ask your server for the payment result
      fetchPaymentStatusFromServer();
    }
  });
}, []);
```

> **Note:** 3DS2 must first be enabled for the specific card brands in the HyperPay Administration Portal.

## API summary

<<<<<<< Updated upstream
| Method                     | Description                                  |
|----------------------------|----------------------------------------------|
| `AfsPay.init(config)`      | Set global config. Call once before payments. |
| `AfsPay.applePay(params, onProgress?)` | Start Apple Pay flow (iOS).             |
| `AfsPay.googlePay(params, onProgress?)` | Start Google Pay flow (Android only).  |
| `AfsPay.createPaymentTransaction(params, onProgress?)` | Start card payment.              |
| `AfsPay.getPaymentStatus(statusCode)`  | Get transaction status.                 |
| `useTransactionLoading()`  | Hook that returns loading state.             |
=======
| Method                                                  | Description                                             |
|---------------------------------------------------------|---------------------------------------------------------|
| `AfsPay.init(config)`                                   | Set global config. Call once before payments.           |
| `AfsPay.applePay(params, onProgress?)`                  | Start Apple Pay flow (iOS).                             |
| `AfsPay.googlePay(params, onProgress?)`                 | Start Google Pay flow (Android only).                   |
| `AfsPay.createPaymentTransaction(params, onProgress?)`  | Charge a card. Pass `tokenizationEnabled: true` to also store a token. |
| `AfsPay.registerCard(params, onProgress?)`              | Store a card as a token without charging (stand-alone). |
| `AfsPay.payWithToken(params, onProgress?)`              | One-click payment using a stored token.                 |
| `AfsPay.getPaymentStatus(statusCode)`                   | Get transaction status from a result code.              |
| `AfsPay.checkThreeDS2Status()`                          | Android only — check if 3DS was killed in background.   |
| `useTransactionLoading()`                               | Hook that returns loading state.                        |
>>>>>>> Stashed changes

## Troubleshooting

### "The package 'react-native-afspay-sdk' doesn't seem to be linked"

- **iOS:** Run `cd ios && pod install && cd ..` and rebuild.
- **Android:** Clean and rebuild (`cd android && ./gradlew clean && cd ..`, then run the app).
- Do not use Expo managed workflow; use a dev build or bare project.

### "Cannot read property 'HyperPaySDK' of undefined" / "runtime not ready"

- Ensure you have rebuilt the app after installing the package.
- Clear Metro cache and rebuild:  
  `npx react-native start --reset-cache` then run the app again.
- If you switched from an older package name, do a clean install:  
  remove `node_modules`, run `yarn` or `npm install`, then `pod install` in `ios`, and rebuild.

## Contributing

See the [contributing guide](CONTRIBUTING.md) for development setup and workflow.

## License

MIT

## Contributor

Awais Ibrar
