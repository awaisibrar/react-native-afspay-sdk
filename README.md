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

### 5. Tokenization

Tokenization replaces sensitive card data with a reusable token so you can charge the shopper later without re-entering card details.

#### 5a. Tokenize during payment (shopper-determined)

Add `tokenizationEnabled: true` to a normal card payment. The server's payment status response will include a `registrationId` — store that token on your server.

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
    tokenizationEnabled: true, // <-- store card as token
  },
  (isLoading) => { /* show/hide loader */ }
);
// After payment, query your server with result.checkoutId
// Server response includes registrationId — save it for future payments
```

#### 5b. Register card without payment (stand-alone)

Saves the card as a token **without** charging it. Useful for a "saved cards" section.

> **Server requirement:** Your backend must send `createRegistration=true` and omit `paymentType` when preparing the checkout.

```js
const result = await AfsPay.registerCard(
  {
    paymentBrand: 'VISA',
    holderName: 'John Doe',
    cardNumber: '4111111111111111',
    expiryYear: '2027',
    expiryMonth: '12',
    cvv: '123',
    checkoutID: 'registration-checkout-id-from-backend',
  },
  (isLoading) => { /* show/hide loader */ }
);
// result: { checkoutId }
// Query your server with checkoutId to get the registrationId (token) — save it
```

#### 5c. Pay with a stored token (one-click payment)

Use a `registrationId` from a previous payment or registration to charge without re-entering card details.

> **Server requirement:** Your backend must include `registrations[0].id = <tokenID>` when preparing the checkout.

```js
const result = await AfsPay.payWithToken(
  {
    checkoutID: 'checkout-id-from-backend',
    tokenID: '8a82944a580a782101581f3a0b4b5ab9', // registrationId from your server
    paymentBrand: 'VISA',
    cvv: '123', // optional — required by some acquirers
  },
  (isLoading) => { /* show/hide loader */ }
);
// result: { status: 'pending' | 'completed', checkoutId, redirectURL? }
```

| Option        | Type   | Required | Description                                           |
|---------------|--------|----------|-------------------------------------------------------|
| `checkoutID`  | string | Yes      | Checkout ID from your backend.                        |
| `tokenID`     | string | Yes      | The `registrationId` (token) stored on your server.   |
| `paymentBrand`| string | Yes      | Card brand (e.g. `'VISA'`, `'MASTERCARD'`).           |
| `cvv`         | string | No       | Required by some acquirers for token payments.        |

### 6. Get payment status

After redirect or when you have a transaction resource path or status code:

```js
const status = await AfsPay.getPaymentStatus('000.000.000');

// status: { code, description, status: 'successfully' | 'rejected' | ... }
```

### 7. Loading hook (optional)

```js
import AfsPay, { useTransactionLoading } from 'react-native-afspay-sdk';

function CheckoutScreen() {
  const loading = useTransactionLoading();
  // use loading to show a spinner during payment requests
}
```

## API summary

| Method                                                  | Description                                             |
|---------------------------------------------------------|---------------------------------------------------------|
| `AfsPay.init(config)`                                   | Set global config. Call once before payments.           |
| `AfsPay.applePay(params, onProgress?)`                  | Start Apple Pay flow (iOS).                             |
| `AfsPay.googlePay(params, onProgress?)`                 | Start Google Pay flow (Android only).                   |
| `AfsPay.createPaymentTransaction(params, onProgress?)`  | Charge a card. Pass `tokenizationEnabled: true` to also store a token. |
| `AfsPay.registerCard(params, onProgress?)`              | Store a card as a token without charging (stand-alone). |
| `AfsPay.payWithToken(params, onProgress?)`              | One-click payment using a stored token.                 |
| `AfsPay.getPaymentStatus(statusCode)`                   | Get transaction status from a result code.              |
| `useTransactionLoading()`                               | Hook that returns loading state.                        |

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
