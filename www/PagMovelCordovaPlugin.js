var exec = require('cordova/exec');

exports.generateQrCode = function(type, paymentUUID, senderUUID, deviceUUID, value, receiverUUID, paymentType, validDate, freeText, success, error) {
    exec(success, error, "PagMovelCordovaPlugin", "generateQrCode", [type, paymentUUID, senderUUID, deviceUUID, value, receiverUUID, paymentType, validDate]);
};

exports.generateSQrCode = function(keyPairId, pin, type, paymentUUID, senderUUID, deviceUUID, value, receiverUUID, paymentType, validDate, freeText, success, error) {
    exec(success, error, "PagMovelCordovaPlugin", "generateSQrCode", [keyPairId, pin, type, paymentUUID, senderUUID, deviceUUID, value, receiverUUID, paymentType, validDate, freeText]);
};

exports.generateKeyPair = function(keyPairId, pin, success, error) {
	exec(function() {
		exec(success, error, "PagMovelCordovaPlugin", "generateKeyPair", [keyPairId, pin]);
    }, error, "PagMovelCordovaPlugin", "removeKeyPair", [keyPairId]);
};

exports.keyPairExists = function(keyPairId, success, error) {
    exec(success, error, "PagMovelCordovaPlugin", "keyPairExists", [keyPairId]);
};

exports.removeKeyPair = function(keyPairId, success, error) {
    exec(success, error, "PagMovelCordovaPlugin", "removeKeyPair", [keyPairId]);
};

exports.isPinCorrect = function(keyPairId, pin) {
    exec(success, error, "PagMovelCordovaPlugin", "checkPinCorrect", [keyPairId, pin]);
};

exports.sign = function(data, keyPairId, pin) {
    exec(success, error, "PagMovelCordovaPlugin", "sign", [data, keyPairId, pin]);
};

exports.generatePaymentId = function() {
    exec(success, error, "PagMovelCordovaPlugin", "generatePaymentId");
};
