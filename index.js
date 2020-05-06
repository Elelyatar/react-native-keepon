// @flow

import React, { Component } from "react";
import { NativeModules, Platform } from "react-native";

let mounted = 0;

export default class KeepOn extends Component<{}> {
  constructor(props){
    super(props)

  }

  static activate() {
    NativeModules.KeepOn.activate();
  }

  static deactivate() {
    NativeModules.KeepOn.deactivate();
  }

  static startProximitySensor() {
    NativeModules.KeepOn.startProximitySensor();
  }
  static stopProximitySensor() {
    NativeModules.KeepOn.stopProximitySensor();
  }

  static isOnSpeakerMode(value){
    if (value) {
      KeepOn.stopProximitySensor();
    } else {
      KeepOn.startProximitySensor();
    }
  }

  componentDidMount() {
    mounted++;
    KeepOn.activate();
    KeepOn.startProximitySensor();
  }

  componentWillUnmount() {
    mounted--;
    if (!mounted) {
     KeepOn.deactivate();
    }
    KeepOn.stopProximitySensor();
  }

  render() {
    return null;
  }
}