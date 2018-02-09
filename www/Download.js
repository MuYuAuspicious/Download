var exec = require('cordova/exec');

exports.file = function (arg0, success, error) {
    exec(success, error, 'Download', 'file', arg0);
};

exports.unpack = function (arg0, success, error) {
    exec(success, error, 'Download', 'unpack', arg0);
};
