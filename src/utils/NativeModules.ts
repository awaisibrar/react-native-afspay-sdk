import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-afspay-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

// Native module is registered as "HyperPay" on both iOS and Android.
// Resolve lazily so we don't access NativeModules before the bridge is ready.
let _nativeModule: any;
function getNativeModule() {
  if (_nativeModule === undefined) {
    _nativeModule = NativeModules?.HyperPay ?? null;
  }
  return _nativeModule;
}

export const HyperPaySDK = new Proxy({} as any, {
  get(_, prop) {
    const module = getNativeModule();
    if (module) return module[prop];
    throw new Error(LINKING_ERROR);
  },
});
