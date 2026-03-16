import type { CountryCodes } from './CountryCodes';
import { PaymentStatus } from './PaymentStatus';
import type { SupportedNetworks } from './SupportedNetworks';
import type { CardAccountBrands } from './CardAccountBrands';
export type CreateTransactionResponseType = {
    status: 'pending' | 'rejected' | 'risk' | 'chargeback' | 'declines' | 'successfully',
    checkoutId: string,
    redirectURL: string
}
export interface Config {
    shopperResultURL: string;
    /**
    * required for apple pay
    */
    countryCode?: keyof CountryCodes;
    /**
     * required for apple pay
     */
    merchantIdentifier?: string;

    mode?: "TestMode" | "LiveMode";

    /**
     * It will be prepended with the word "Pay" (i.e. "Pay Sportswear $100.00"), Apply pay only
     */
    companyName?:string;

    /**
     *  set up supported payment networks for apple pay
    * @Platform IOS Only 
    */
    supportedNetworks?: Array<SupportedNetworks>

}

export type ApplyPayParams = {
    checkoutID:string;
    companyName?:string;
    amount?:string;
}

export type CreateTransactionParams = {
    paymentBrand: CardAccountBrands,
    holderName: string,
    cardNumber: string,
    expiryYear: string,
    expiryMonth: string,
    cvv: string,
    checkoutID: string,
    shopperResultURL?: string,
    /** Set to true to store card details as a token during payment (shopper-determined tokenization). */
    tokenizationEnabled?: boolean,
}
/**
 * Params for stand-alone card registration (no payment charged).
 * Your server must send createRegistration=true and omit paymentType when preparing the checkout.
 */
export type RegisterCardParams = {
    paymentBrand: CardAccountBrands,
    holderName: string,
    cardNumber: string,
    expiryYear: string,
    expiryMonth: string,
    cvv: string,
    checkoutID: string,
    shopperResultURL?: string,
}
export type RegisterCardResponse = {
    /** Use this checkoutId to request registration status from your server and retrieve the token (registrationId). */
    checkoutId: string,
}
/**
 * Params for paying with a stored token (one-click payment).
 * Your server must include registrations[n].id in the prepare checkout request.
 */
export type TokenPaymentParams = {
    checkoutID: string,
    /** The token / registrationId returned from a previous payment or registration. */
    tokenID: string,
    paymentBrand: CardAccountBrands,
    /** Optional CVV — required by some acquirers for token payments. */
    cvv?: string,
    shopperResultURL?: string,
}
export type TokenPaymentResponse = {
    status: 'pending' | 'completed',
    checkoutId: string,
    redirectURL?: string,
}
export type ApplePayCallback = {
    /** Shopper was redirected to the issuer web page.
     Request payment status when shopper returns to the app using transaction.resourcePath or just checkout id. 
     */
    redirectURL?: string;
    /**  Request payment status for the synchronous transaction from your server using transactionPath.resourcePath or just checkout id.*/
    resourcePath?: string;
}
/** @platform Android */
export type GooglePayParams = {
    checkoutID: string;
    amount?: string;
    currencyCode?: string;
    gatewayMerchantId?: string;
    entityId?: string;
}
/** @platform Android */
export type GooglePayCallback = {
    resourcePath?: string;
    transactionId?: string;
    checkoutId?: string;
}
export default class HyperPay {
    /**
       * @param  {string} shopperResultURL
       * @param  {CountryCodes} countryCode 
       * @param  {string} merchantIdentifier
       * @param  {string} mode
       * @param  {SupportedNetworks[]} supportedNetworks 
       * @returns Config
       */

    static init(params: Config): Config

    /**
       * @param  {string} checkoutID
       * @param  {string} companyName
       * @param  {number} amount 
       * @param  {(isProgress: boolean) => void} onProgress
       * @returns ```Promise<{ redirectURL?: string,
        resourcePath?: string}>```
       */
    static applePay(checkoutID: ApplyPayParams, onProgress?: (isProgress: boolean) => void): Promise<ApplePayCallback>;
    /** Google Pay — Android only. Rejects on iOS. */
    static googlePay(params: GooglePayParams, onProgress?: (isProgress: boolean) => void): Promise<GooglePayCallback>;

    /**
     * @param  {string} paymentBrand
     * @param  {string} holderName
     * @param  {string} cardNumber
     * @param  {string} expiryYear
     * @param  {string} expiryMonth
     * @param  {string} cvv
     * @param  {string} checkoutID
     * @param  {string} shopperResultURL
     * 
     * @returns  ```Promise<{ status: 'pending' | 'rejected' | 'risk' | 'chargeback' | 'declines' | 'successfully',
         checkoutId: string,
         redirectURL: string }>``` 
     */

    static createPaymentTransaction(params: CreateTransactionParams, onProgress?: (isProgress: boolean) => void): Promise<CreateTransactionResponseType>;
    /**
     * Stand-alone card registration — stores card as a token without charging.
     * Your server must send createRegistration=true and omit paymentType in the prepare checkout request.
     * Use the returned checkoutId to fetch the registrationId (token) from your server.
     */
    static registerCard(params: RegisterCardParams, onProgress?: (isProgress: boolean) => void): Promise<RegisterCardResponse>;
    /**
     * Pay using a previously stored token (one-click payment).
     * Your server must include registrations[n].id in the prepare checkout request.
     */
    static payWithToken(params: TokenPaymentParams, onProgress?: (isProgress: boolean) => void): Promise<TokenPaymentResponse>;



    /**
       * @param  {string} statusCode
       * @returns```{ code: string, description: string,   status: 'successfully' | 
       * 'rejected' | 'Chargeback' | 'pending' | 'error' }``` 
    */

    static getPaymentStatus(status: string): PaymentStatus

}
export type { PaymentStatus }

export declare function useTransactionLoading(): void