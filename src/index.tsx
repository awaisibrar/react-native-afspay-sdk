import type {
  CreateTransactionResponseType,
  CreateTransactionParams,
  Config,
  ApplyPayParams,
  ApplePayCallback,
  GooglePayParams,
  GooglePayCallback,
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

<<<<<<< Updated upstream
=======
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

export function checkThreeDS2Status(): Promise<{ wasTransactionKilled: boolean }> {
  return HyperPaySDK.checkThreeDS2Status();
}

>>>>>>> Stashed changes
const Hyperpay = {
  init,
  applePay,
  googlePay,
  createPaymentTransaction,
  getPaymentStatus,
  checkThreeDS2Status,
}
export {
  useTransactionLoading
} from './hooks'


export default Hyperpay