"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.HyperPay = { init, createPaymentTransaction, applePay, googlePay, registerCard, payWithToken };

var _reactNative = require("react-native");

const LINKING_ERROR = `The package 'react-native-afspay-sdk' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n' + '- You are not using Expo managed workflow\n';
const HyperPaySDK = _reactNative.NativeModules?.HyperPay ? _reactNative.NativeModules.HyperPay : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }

});

function init(params) {
  return HyperPaySDK.setup(params);
}

function createPaymentTransaction(params) {
  return HyperPaySDK.createPaymentTransaction(params);
}

function applePay(params) {
  return HyperPaySDK.applePay(params);
}

function googlePay(params) {
  return HyperPaySDK.googlePay(params);
}

function registerCard(params) {
  return HyperPaySDK.registerCard(params);
}

function payWithToken(params) {
  return HyperPaySDK.payWithToken(params);
}