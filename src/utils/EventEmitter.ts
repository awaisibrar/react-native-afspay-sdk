import { NativeEventEmitter } from 'react-native';
import { HyperPaySDK } from './NativeModules';

export const eventEmitter = new NativeEventEmitter(HyperPaySDK);
