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

  async componentDidMount() {
    mounted++;
    KeepOn.activate();
    await Proximity.addListener(this._proximityListener);
  }

  async componentWillUnmount() {
    mounted--;
    if (!mounted) {
      KeepOn.deactivate();
    }
    await Proximity.removeListener(this._proximityListener);
  }

  _proximityListener(data) {
    console.log("proximity:", data.proximity, "distance:", data.distance);
    KeepOn.turnScreenOff();
  }

  render() {
    return null;
  }
}
