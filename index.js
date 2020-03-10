// @flow

import React, { Component } from "react";
import { NativeModules } from "react-native";
import Proximity from "react-native-proximity";

let mounted = 0;

export default class KeepOn extends Component<{}> {
  static activate() {
    NativeModules.KCKeepOn.activate();
  }

  static deactivate() {
    NativeModules.KCKeepOn.deactivate();
  }

  static lightOn() {
    NativeModules.KCKeepOn.RemoveLightOut();
  }

  static lightOut() {
    NativeModules.KCKeepOn.addLightOut();
  }

  componentDidMount() {
    mounted++;
    KeepOn.activate();
    Proximity.addListener(this._proximityListener);
  }

  componentWillUnmount() {
    mounted--;
    if (!mounted) {
      NativeModules.KCKeepOn.RemoveLightOut();
      KeepOn.deactivate();
    }
  }

  _proximityListener(data) {
    console.log("proximity:", data.proximity, "distance:", data.distance);
  }

  render() {
    return null;
  }
}
