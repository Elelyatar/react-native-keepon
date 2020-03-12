// @flow

import React, { Component } from "react";
import { NativeModules } from "react-native";
import Proximity from "react-native-proximity";

let mounted = 0;

export default class KeepOn extends Component<{}> {
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

  componentDidMount() {
    mounted++;
    KeepOn.activate();
    Proximity.addListener(this._proximityListener);
  }

  componentWillUnmount() {
    mounted--;
    if (!mounted) {
      KeepOn.deactivate();
      KeepOn.turnScreenOn();
    }
    Proximity.removeListener(this._proximityListener);
  }

  _proximityListener(data) {
    console.log("proximity:", data.proximity, "distance:", data.distance);
    KeepOn.turnScreenOff();
  }

  render() {
    return null;
  }
}
