package com.matera.payments;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.matera.commons.sqrcode.api.SQRCode;
import com.matera.commons.sqrcode.api.SQRCodeApiException;
import com.matera.payments.qrcode.api.KeyPair;
import com.matera.payments.qrcode.api.PagMovelQRCodeInput;
import com.matera.payments.qrcode.api.PagMovelQRCodeInput.QRCodeType;
import com.matera.payments.qrcode.api.PagMovelQrCodeApi;
import com.matera.payments.qrcode.api.PagMovelQrCodeApiImpl;

import android.app.Activity;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class PagMovelCordovaPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("generateQrCode".equals(action)) {
            int i = 0;
            String qrType = args.getString(i++);
            String paymentId = args.getString(i++);
            String senderId = args.getString(i++);
            String deviceId = args.getString(i++);
            String value = args.getString(i++);
            String receiverId = args.getString(i++);
            String shortId = args.getString(i++);
            String date = args.getString(i++);
            this.generateQrCode(qrType, paymentId, senderId, deviceId, value, receiverId, shortId, date,
                    callbackContext);
            return true;
        } else if ("generateSQrCode".equals(action)) {
            int i = 0;
            String qrType = args.getString(i++);
            String keyId = args.getString(i++);
            String pin = args.getString(i++);
            String paymentId = args.getString(i++);
            String senderId = args.getString(i++);
            String deviceId = args.getString(i++);
            String value = args.getString(i++);
            String receiverId = args.getString(i++);
            String shortId = args.getString(i++);
            String date = args.getString(i++);
            String freeText = args.getString(i++);
            this.generateQrCode(keyId, pin, qrType, paymentId, senderId, deviceId, value, receiverId, shortId, date, freeText,
                    callbackContext);
            return true;
        } else if ("generateKeyPair".equals(action)) {
            String keyPairId = args.getString(0);
            String pin = args.getString(1);
            this.generateKeyPair(keyPairId, pin, callbackContext);
            return true;
        } else if ("keyPairExists".equals(action)) {
            String keyId = args.getString(0);
            this.keyPairExists(keyId, callbackContext);
            return true;
        } else if ("removeKeyPair".equals(action)) {
            String keyId = args.getString(0);
            this.removeKeyPair(keyId, callbackContext);
            return true;
        } else if ("decodeQrData".equals(action)) {
            String qrdata = args.getString(0);
            this.decodeQrData(qrdata, callbackContext);
            return true;
        } else if ("checkPinCorrect".equals(action)) {
            String keyId = args.getString(0);
            String pin = args.getString(1);
            this.checkPinCorrect(keyId, pin, callbackContext);
            return true;
        } else if ("sign".equals(action)) {
            int i = 0;
            String data = args.getString(i++);
            String keyId = args.getString(i++);
            String pin = args.getString(i++);
            this.sign(data, keyId, pin, callbackContext);
            return true;
        } else if ("generatePaymentId".equals(action)) {
            this.generatePaymentId(callbackContext);
            return true;
        }
        return false;
    }

    private void checkPinCorrect(String keyId, String pin, CallbackContext callbackContext) {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            boolean ok = manager.isPinCorrect(keyId, pin);
            JSONObject obj = new JSONObject();
            obj.put("result", ok);
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when checking pin", e);
            callbackContext.error(e.getMessage());
        }

    }

    private void keyPairExists(String keyId, CallbackContext callbackContext) {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            boolean ok = manager.keyPairExists(keyId);
            JSONObject obj = new JSONObject();
            obj.put("result", ok);
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when checking if keyPair exists", e);
            callbackContext.error(e.getMessage());
        }

    }

    private void sign(String data, String keyId, String pin, CallbackContext callbackContext) {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            String signature = manager.sign(data, keyId, pin);
            JSONObject obj = new JSONObject();
            obj.put("signature", signature);
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when signing data", e);
            callbackContext.error(e.getMessage());
        }
    }

    private void decodeQrData(String qrdata, CallbackContext callbackContext) {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            com.matera.payments.qrcode.api.QRDecodedData data = manager.decodeQrData(qrdata);
            JSONObject obj = new JSONObject();
            obj.put("data", data.getData());
            obj.put("rawData", data.getRawData());
            if (data.getSignature() != null) {
                obj.put("signature", data.getSignature());
            }
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when decoding qr", e);
            callbackContext.error(e.getMessage());
        }
    }

    private void removeKeyPair(String keyId, CallbackContext callbackContext) {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            manager.destroyKey(keyId);
            callbackContext.success();
        } catch (SQRCodeApiException e) {
            Log.e("com.matera.payments", "Error when generating qr", e);
            callbackContext.error(e.getMessage());
        }
    }

    private void generateQrCode(final String qrType, final String paymentUUID, final String senderUUID,
            final String deviceUUID, final String value, final String receiverUUID, final String shortId,
            final String validDate, final CallbackContext callbackContext) throws JSONException {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            final PagMovelQRCodeInput input = new PagMovelQRCodeInput().setDeviceId(deviceUUID)
                    .setPaymentId(UUID.fromString(paymentUUID)).setReceiverId(toUUID(receiverUUID))
                    .setSenderId(UUID.fromString(senderUUID)).setShortId(shortId)
                    .setValidThru(new Date(Long.parseLong(validDate))).setValue(new BigDecimal(value))
                    .setType(QRCodeType.valueOf(qrType));
            SQRCode qrCode = manager.generateQRCode(input);
            Log.i("com.matera.payments", "QRCODE is " + qrCode);
            JSONObject obj = new JSONObject();
            obj.put("qrCode", qrCode.getBase64EncodedImage());
            obj.put("text", qrCode.getText());
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when generating qr", e);
            callbackContext.error(e.getMessage());
        }
    }

    private void generateQrCode(String keyId, String pin, final String qrType, final String paymentUUID,
            final String senderUUID, final String deviceUUID, final String value, final String receiverUUID,
            final String shortId, final String validDate, final String freeText, final CallbackContext callbackContext)
                    throws JSONException {
        try {
            final PagMovelQrCodeApi manager = getQRManager();
            final PagMovelQRCodeInput input = new PagMovelQRCodeInput().setDeviceId(deviceUUID)
                    .setPaymentId(UUID.fromString(paymentUUID)).setReceiverId(toUUID(receiverUUID))
                    .setSenderId(UUID.fromString(senderUUID)).setShortId(shortId)
                    .setValidThru(new Date(Long.parseLong(validDate))).setValue(new BigDecimal(value))
                    .setFreeText(freeText)
                    .setType(QRCodeType.valueOf(qrType));
            SQRCode qrCode = manager.generateQRCode(input, keyId, pin);
            Log.i("com.matera.payments", "QRCODE is " + qrCode);
            JSONObject obj = new JSONObject();
            obj.put("qrCode", qrCode.getBase64EncodedImage());
            obj.put("text", qrCode.getText());
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when generating qr", e);
            callbackContext.error(e.getMessage());
        }
    }

    public static void main(String[] args) throws SQRCodeApiException {
        final PagMovelQrCodeApi manager = new PagMovelQrCodeApiImpl(null);
        final PagMovelQRCodeInput input = new PagMovelQRCodeInput().setDeviceId("e1eca23ae50477fa")
                .setPaymentId(UUID.fromString("92991000-d6a0-422b-9b30-3c087477543c")).setReceiverId(toUUID("92991000-d6a0-422b-9b30-3c087477543c"))
                .setSenderId(UUID.fromString("92991000-d6a0-422b-9b30-3c087477543c")).setShortId("")
                .setValidThru(new Date(Long.parseLong("1469639941942"))).setValue(new BigDecimal("12000"))
                .setFreeText("")
                .setType(QRCodeType.valueOf("PAYMENT"));
        SQRCode qrCode = manager.generateQRCode(input);
        System.err.println(qrCode.getBase64EncodedImage());
        System.err.println(qrCode.getText());
    }

    private void generateKeyPair(String keyPairId, String pin, CallbackContext callbackContext) {
        final PagMovelQrCodeApi manager = getQRManager();
        try {
            KeyPair result = manager.generateKeyPair(keyPairId, pin);
            JSONObject obj = new JSONObject();
            obj.put("keyId", result.getKeyPairId());
            obj.put("publicKey", result.getPublicKey());
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when generating key pair", e);
            callbackContext.error(e.getMessage());
        }
    }

    private void generatePaymentId(CallbackContext callbackContext) {
        final PagMovelQrCodeApi manager = getQRManager();
        try {
            UUID result = manager.generatePaymentID();
            JSONObject obj = new JSONObject();
            obj.put("paymentId", result.toString());
            callbackContext.success(obj);
        } catch (Exception e) {
            Log.e("com.matera.payments", "Error when generating key pair", e);
            callbackContext.error(e.getMessage());
        }
    }

    private static UUID toUUID(String receiverUUID) {
        if (receiverUUID == null || receiverUUID.isEmpty()) {
            return null;
        }
        return UUID.fromString(receiverUUID);
    }

    private PagMovelQrCodeApi getQRManager() {
        final Activity activity = cordova.getActivity();
        final PagMovelQrCodeApi manager = new PagMovelQrCodeApiImpl(activity);
        return manager;
    }

}
