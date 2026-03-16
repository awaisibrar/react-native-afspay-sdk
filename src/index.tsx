import type {
  CreateTransactionResponseType,
  CreateTransactionParams,
  Config,
  ApplyPayParams,
  ApplePayCallback,
  GooglePayParams,
  GooglePayCallback,
  RegisterCardParams,
  RegisterCardResponse,
  TokenPaymentParams,
  TokenPaymentResponse,
} from '../lib/typescript'
import {
  getPaymentStatus
} from './paymentStatus'
import { HyperPaySDK, eventEmitter } from './utils';

export function init(params: Config): Config {
  return HyperPaySDK.setup(params);
}

export function createPaymentTransaction(params: CreateTransactionParams, onProgress?: (isProgress: boolean) => void):
  Promise<CreateTransactionResponseType> {
  if (onProgress) {
    eventEmitter.removeAllListeners("onProgress")
    const _event = eventEmitter.addListener('onProgress', (isLoading: boolean) => {
      onProgress(isLoading)
      if (!isLoading) _event.remove()
    });
  }
  return HyperPaySDK.createPaymentTransaction(params);
}



export function applePay(params: ApplyPayParams,
  onProgress?: (isProgress: boolean) => void): Promise<ApplePayCallback> {

  if (onProgress) {
    const _event = eventEmitter.addListener('onProgress', (isLoading: boolean) => {
      onProgress(isLoading)
      if (!isLoading) _event.remove()
    });
  }

  return HyperPaySDK.applePay(params);
}

export function googlePay(
  params: GooglePayParams,
  onProgress?: (isProgress: boolean) => void
): Promise<GooglePayCallback> {
  if (onProgress) {
    const _event = eventEmitter.addListener('onProgress', (isLoading: boolean) => {
      onProgress(isLoading);
      if (!isLoading) _event.remove();
    });
  }
  return HyperPaySDK.googlePay(params);
}

export function registerCard(
  params: RegisterCardParams,
  onProgress?: (isProgress: boolean) => void
): Promise<RegisterCardResponse> {
  if (onProgress) {
    eventEmitter.removeAllListeners('onProgress');
    const _event = eventEmitter.addListener('onProgress', (isLoading: boolean) => {
      onProgress(isLoading);
      if (!isLoading) _event.remove();
    });
  }
  return HyperPaySDK.registerCard(params);
}

export function payWithToken(
  params: TokenPaymentParams,
  onProgress?: (isProgress: boolean) => void
): Promise<TokenPaymentResponse> {
  if (onProgress) {
    eventEmitter.removeAllListeners('onProgress');
    const _event = eventEmitter.addListener('onProgress', (isLoading: boolean) => {
      onProgress(isLoading);
      if (!isLoading) _event.remove();
    });
  }
  return HyperPaySDK.payWithToken(params);
}

const Hyperpay = {
  init,
  applePay,
  googlePay,
  createPaymentTransaction,
  registerCard,
  payWithToken,
  getPaymentStatus,
}
export {
  useTransactionLoading
} from './hooks'


export default Hyperpay