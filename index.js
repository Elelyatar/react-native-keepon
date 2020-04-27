// @flow

import React, { Component } from "react";
import { NativeModules, Platform } from "react-native";
import Proximity from "react-native-proximity";

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

  static turnScreenOn() {
    NativeModules.KeepOn.turnScreenOn();
  }

  static turnScreenOff() {
    NativeModules.KeepOn.turnScreenOff();
  }

  static isOnSpeakerMode(value){
    if (value) {
      // Proximity.removeListener(this._proximityListener);
      KeepOn.turnScreenOn();
    } else {
      // Proximity.addListener(this._proximityListener);
    }
  }

  componentDidMount() {
    mounted++;
    KeepOn.activate();
    // Proximity.addListener(this._proximityListener);
  }

  componentWillUnmount() {
    mounted--;
    if (!mounted) {
      KeepOn.deactivate();
      KeepOn.turnScreenOn();
    }
    // Proximity.removeListener(this._proximityListener);
  }

  // _proximityListener(data) {
  //   if(Platform.OS == "android")KeepOn.turnScreenOff();
  // }

  render() {
    return null;
  }
}