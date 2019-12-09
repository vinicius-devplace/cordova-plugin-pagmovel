import Foundation
import PaymentQRCode
import SwiftKeychainWrapper

@objc(PagMovelCordovaPlugin) open class PagMovelCordovaPlugin: CDVPlugin {
    private let LOG_TAG = "BePay"
    private let DEBUG = true

    func log(_ message: String) {
        if !DEBUG { return }
        NSLog("\(LOG_TAG) :: PagMovelCordovaPlugin :: \(message)")
    }

    open override func pluginInitialize() {
        super.pluginInitialize()
        log("pluginInitialize")
    }

    func initialize(_ command: CDVInvokedUrlCommand) {
        log("initialize")

        let config = command.argument(at:0)
        log("config \(config)")

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }

    @objc open func generateQrCode(_ command:CDVInvokedUrlCommand) {
        log("generateQrCode")

        let type = command.arguments[0] as! String
        log("type \(type)")

        let paymentUUID = command.arguments[1] as! String
        log("paymentUUID \(paymentUUID)")

        let senderUUID = command.arguments[2] as! String
        log("senderUUID \(senderUUID)")

        let deviceUUID = command.arguments[3] as! String
        log("deviceUUID \(deviceUUID)")

        let value = command.arguments[4] as! String
        log("value \(value)")

        let receiverUUID = command.arguments[5] as! String
        log("receiverUUID \(receiverUUID)")

        let shortUUID = command.arguments[6] as! String
        log("shortUUID \(shortUUID)")

        let validDate = command.arguments[7] as! String
        log("validDate \(validDate)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            if let apiResult = api.generateQRCode(type, paymentUUID, senderUUID, receiverUUID, deviceUUID, shortUUID, value, validDate, "") {
                self.log("apiResult \(apiResult)")

                let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
                self.commandDelegate!.send(result, callbackId: command.callbackId)

                return
            }

            self.error(command)
        }
    }

    @objc open func generateSQrCode(_ command:CDVInvokedUrlCommand) {
        log("generateSQrCode")

        var type: String {
            switch (command.arguments[0] as? String) ?? "" {
            case "PAYMENT":
                return "000"
            case "TRANSFER":
                return "001"
            case "MONEY_WITHDRAW":
                return "011"
            case "DEPOSIT":
                return "100"
            default:
                return ""
            }
        }
        log("type \(type)")

        let keyId = (command.arguments[1] as? String) ?? ""
        log("keyId \(keyId)")

        let pin = (command.arguments[2] as? String) ?? ""
        log("pin \(pin)")

        let paymentUUID = (command.arguments[3] as? String) ?? ""
        log("paymentUUID \(paymentUUID)")

        let senderUUID = (command.arguments[4] as? String) ?? ""
        log("senderUUID \(senderUUID)")

        let deviceUUID = (command.arguments[5] as? String) ?? ""
        log("deviceUUID \(deviceUUID)")

        let value = String(describing: command.arguments[6])
        log("value \(value)")

        let receiverUUID = (command.arguments[7] as? String) ?? ""
        log("receiverUUID \(receiverUUID)")

        let shortUUID = (command.arguments[8] as? String) ?? ""
        log("shortUUID \(shortUUID)")

        let validDate = String(describing: command.arguments[9])
        log("validDate \(validDate)")

        let freeText = (command.arguments[10] as? String) ?? ""
        log("freeText \(freeText)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            if let qrCode = api.generateQRCode(type, paymentUUID, senderUUID, receiverUUID, deviceUUID, shortUUID, value, validDate, freeText, keyId, pin) {
                let apiResult: [NSObject: AnyObject] = [
                    "qrCode" as NSObject: qrCode as AnyObject
                ]
                self.log("apiResult \(apiResult)")

                let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
                self.commandDelegate!.send(result, callbackId: command.callbackId)

                return
            }

            self.error(command)
        }
    }

    @objc open func generateKeyPair(_ command:CDVInvokedUrlCommand) {
        log("generateKeyPair")

        for i in command.arguments {
            log("commands: \(i)")
        }

        let keyPairId = command.arguments[0] as! String
        log("keyPairId: \(keyPairId)")

        let pin = command.arguments[1] as! String
        log("pin: \(pin)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            let keypair = api.generateKeyPair(keyPairId, pin)
            let keyId = keypair?.key.replacingOccurrences(of: "com.BePay.", with: "")
            let apiResult: [NSObject: AnyObject] = [
                "keyId" as NSObject: keyId! as AnyObject,
                "publicKey" as NSObject: (keypair?.publicKey)! as AnyObject
            ]
            self.log("apiResult \(apiResult)")

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
            self.commandDelegate!.send(result, callbackId: command.callbackId)
        }
    }

    @objc open func keyPairExists(_ command:CDVInvokedUrlCommand) {
        log("keyPairExists")

        guard let keyId = command.arguments[0] as? String else {
            self.commandDelegate.run {
                let apiResult: [NSObject: AnyObject] = [
                    "result" as NSObject: false as AnyObject
                ]
                self.log("apiResult \(apiResult)")

                let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
                self.commandDelegate!.send(result, callbackId: command.callbackId)
            }
            return
        }

        log("keyId: \(keyId)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            let exists = api.keyPairExists(keyId)
            let apiResult: [NSObject: AnyObject] = [
                "result" as NSObject: exists as AnyObject? ?? false as AnyObject
            ]
            self.log("apiResult \(apiResult)")

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
            self.commandDelegate!.send(result, callbackId: command.callbackId)
        }
    }

    @objc open func removeKeyPair(_ command:CDVInvokedUrlCommand) {
        log("removeKeyPair")

        let keyId = command.arguments[0] as! String
        log("keyId: \(keyId)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            api.destroyKey(keyId)

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: true)
            self.commandDelegate!.send(result, callbackId: command.callbackId)
        }
    }

    @objc open func checkPinCorrect(_ command:CDVInvokedUrlCommand) {
        log("checkPinCorrect")

        let keyId = command.arguments[0] as! String
        log("keyId: \(keyId)")

        let pin = command.arguments[1] as! String
        log("pin: \(pin)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            let exists = api.isPinCorrect(keyId, pin)
            let apiResult: [NSObject: AnyObject] = [
                "result" as NSObject: exists as AnyObject? ?? false as AnyObject
            ]
            self.log("apiResult \(apiResult)")

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
            self.commandDelegate!.send(result, callbackId: command.callbackId)
        }
    }

    @objc open func sign(_ command:CDVInvokedUrlCommand) {
        log("sign")

        let data = command.arguments[0] as! String
        log("data: \(data)")

        let keyId = command.arguments[1] as! String
        log("keyId: \(keyId)")

        let pin = command.arguments[2] as! String
        log("pin: \(pin)")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            let apiResult = api.sign(data, keyId, pin)
            self.log("apiResult \(apiResult)")

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
            self.commandDelegate!.send(result, callbackId: command.callbackId)
        }
    }

    @objc open func generatePaymentId(_ command:CDVInvokedUrlCommand) {
        log("generatePaymentId")

        self.commandDelegate.run {
            let api = PaymentQRCode()

            let uuid = api.generatePaymentID()
            let apiResult: [NSObject: AnyObject] = [
                "paymentId" as NSObject: uuid as AnyObject? ?? "" as AnyObject
            ]
            self.log("apiResult \(apiResult)")

            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: apiResult)
            self.commandDelegate!.send(result, callbackId: command.callbackId)
        }
    }

    func error(_ command:CDVInvokedUrlCommand) {
        log("error \(command)")
    }
}
