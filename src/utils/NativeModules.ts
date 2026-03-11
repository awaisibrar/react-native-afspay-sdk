import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-afspay-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

// Native module is registered as "HyperPay" on both iOS and Android
const getNativeModule = () => NativeModules?.HyperPay;

export const HyperPaySDK = getNativeModule()
  ? getNativeModule()
  : new Proxy({} as any, {
      get() {
        throw new Error(LINKING_ERROR);
      },
    });
