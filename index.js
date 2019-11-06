import React, { Component } from 'react';
import { NativeModules } from 'react-native';

let mounted = 0;

export default class KeepOn extends Component {
  static activate() {
    NativeModules.KeepOn.activate();
  }

  static deactivate() {
    NativeModules.KeepOn.deactivate();
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
    return this.props.children || null;
  }
}