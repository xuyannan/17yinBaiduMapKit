var { View, PropTypes, requireNativeComponent } = require('react-native');

var iface = {
  name: 'RCTBaiduMap',
  propTypes: {
    ...View.propTypes,
    mode: PropTypes.number,
    trafficEnabled: PropTypes.bool,
    heatMapEnabled: PropTypes.bool,
    marker:PropTypes.array,
    locationEnabled:PropTypes.bool,
    startRequestLocation: PropTypes.bool,
    showZoomControls:PropTypes.bool,
    onMarkerDragEnd: PropTypes.func,
    onGetMyLocation: PropTypes.func
  }
}

module.exports = requireNativeComponent('RCTBaiduMap', iface);
