// @flow

import React, { Component } from 'react';
import { NativeModules } from 'react-native';

let mounted = 0;

export default class KeepOn extends Component<{}> {
  static activate() {
    NativeModules.KCKeepOn.activate();
  }

  static deactivate() {
    NativeModules.KCKeepOn.deactivate();
  }

  componentDidMount() {
    mounted++;
    KeepOn.activate();
  }

  componentWillUnmount() {
    mounted--;
    if (!mounted) {
      KeepOn.deactivate();
    }
  }

  render() {
    return null;
  }
}
