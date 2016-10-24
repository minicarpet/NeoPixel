//
//  Couleur.swift
//  Neopixel
//
//  Created by Logan Gallois on 17/06/2016.
//  Copyright © 2016 Logan Gallois. All rights reserved.
//

import UIKit
import CoreBluetooth
import AVFoundation

var numFunction = 0
var Programs = [[UInt32]]()
var buffer: [UInt32] = [0, 0, 0, 0, 0]
var numData = 0
var CM = CBCentralManager()
var myPeripheral: CBPeripheral?
var RxCarac: CBCharacteristic?
var TxCarac: CBCharacteristic?
var synchro = false


let Names = [
    "ColorWipe",
    "TheaterChase",
    "rainbowCycle"
]

class Couleur: UIViewController, CBCentralManagerDelegate, CBPeripheralDelegate {
    
    @IBOutlet var subView: UIView!
    @IBOutlet var LoadLabel: UILabel!
    @IBOutlet var LoadActivity: UIActivityIndicatorView!
    @IBOutlet var LoadProgress: UIProgressView!
    
    var AlertBLE = UIAlertController()
    var wheelColor: SwiftHSVColorPicker? = nil
    var grayViewEffect = UIVisualEffectView()
    
    var soundFileURL: NSURL!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        subView.backgroundColor = UIColor.white
        LoadActivity.startAnimating()
        LoadLabel.isHidden = false
        LoadActivity.isHidden = false
        LoadProgress.isHidden = false
        CM = CBCentralManager(delegate: self, queue: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        CM = CBCentralManager(delegate: self, queue: nil)
        myPeripheral?.delegate = self
        if synchro {
            LoadActivity.startAnimating()
            LoadLabel.isHidden = true
            LoadActivity.isHidden = true
            LoadProgress.isHidden = true
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if myPeripheral == nil {
            wheelColor?.removeFromSuperview()
            
            wheelColor = SwiftHSVColorPicker(frame: CGRect(origin: CGPoint(x: self.view.frame.origin.x + 20, y: self.view.frame.origin.y + self.topLayoutGuide.length), size: CGSize(width: self.view.frame.size.width - 40, height: 500)))
            subView.addSubview(wheelColor!)
            
            wheelColor?.setViewColor(color: UIColor(hue: 0.0, saturation: 0.0, brightness: 1, alpha: 1.0))
            
            grayViewEffect.effect = UIBlurEffect(style: .dark)
            grayViewEffect.frame = subView.frame
            self.view.addSubview(grayViewEffect)
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        print("delegate")
        if central.state.rawValue == 4 {
            // BLE IS OFF
            let AlertMessage = UIAlertController(title: "Bluetooth désactivé", message: "Merci d'activer le Bluetooth", preferredStyle: .alert)
            let ActionAlert = UIAlertAction(title: "Ok", style: .default) { (action) in }
            AlertMessage.addAction(ActionAlert)
            self.present(AlertMessage, animated: true, completion: nil)
        }
        if central.state.rawValue == 5 {
            // BLE IS ON
            if myPeripheral != nil {
                if CM.retrieveConnectedPeripherals(withServices: [(myPeripheral?.services?.first?.uuid)!]).count == 0 {
                    CM.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
                }
            } else {
                CM.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
            }
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print(peripheral.name ?? "no Name")
        if peripheral.name == "NeoPixel" {
            myPeripheral = peripheral
            CM.connect(myPeripheral!, options: nil)
            CM.stopScan()
        }
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        myPeripheral = peripheral
        myPeripheral?.delegate = self
        myPeripheral?.discoverServices(nil)
        AlertBLE = UIAlertController()
        AlertBLE.title = "Arduino trouvée"
        AlertBLE.message = "Nous avons réussi a nous connecter"
        AlertBLE.addAction(UIAlertAction(title: "Ok", style: .default, handler: {(alert: UIAlertAction!) in
            self.grayViewEffect.removeFromSuperview()
        }))
        self.present(AlertBLE, animated: true, completion: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        AlertBLE = UIAlertController()
        AlertBLE.title = "Arduino trouvée"
        AlertBLE.message = "Mais nous n'avons pas réussi a nous connecter.\n Merci de relancer l'application"
        if AlertBLE.actions.count == 0 {
            AlertBLE.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
        }
        self.present(AlertBLE, animated: true, completion: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        myPeripheral = nil
        myPeripheral?.delegate = nil
        AlertBLE = UIAlertController()
        AlertBLE.title = "Arduino perdue"
        AlertBLE.message = "Nous avons perdu la connexion avec l'Arduino"
        AlertBLE.addAction(UIAlertAction(title: "Ok", style: .default, handler: {(alert: UIAlertAction!) in
            synchro = false
            self.LoadProgress.setProgress(0, animated: false)
            self.LoadActivity.stopAnimating()
            self.LoadLabel.isHidden = false
            self.LoadActivity.isHidden = false
            self.LoadProgress.isHidden = false
            self.view.addSubview(self.grayViewEffect)
            CM.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        }))
        self.present(AlertBLE, animated: true, completion: nil)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if error == nil {
            for service in peripheral.services! {
                peripheral.discoverCharacteristics(nil, for: service)
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if error == nil {
            for carac in service.characteristics! {
                if String(describing: carac.uuid) == "009B1001-E8F2-537E-4F6C-D104768A1210" {
                    RxCarac = carac
                } else {
                    TxCarac = carac
                    peripheral.setNotifyValue(true, for: TxCarac!)
                }
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if error == nil {
            let data : Data! = TxCarac?.value
            let count = data.count / MemoryLayout<UInt8>.size
            var array = [UInt8](repeating: 0, count: count)
            TxCarac?.value?.copyBytes(to: &array, count: MemoryLayout<UInt32>.size)
            var value: UInt32 = 0
            memcpy(&value, array, 4)
            buffer[numData] = value
            numData += 1
            if numData == 5 {
                numData = 0
                if buffer[0] == 0 && buffer[1] == 1 && buffer[2] == 0 && buffer[3] == 0 && buffer[4] != 0 {
                    numFunction = Int(buffer[4])
                    LoadLabel.isHidden = false
                    LoadActivity.isHidden = false
                    LoadProgress.isHidden = false
                    LoadLabel.isHidden = false
                    LoadActivity.startAnimating()
                    LoadProgress.setProgress(0, animated: true)
                    Programs.removeAll(keepingCapacity: false)
                } else {
                    Programs.append(buffer)
                    LoadProgress.setProgress(Float(Programs.count/numFunction), animated: true)
                    if Programs.count == numFunction {
                        LoadActivity.stopAnimating()
                        synchro = true
                        LoadLabel.isHidden = true
                        LoadActivity.isHidden = true
                        LoadProgress.isHidden = true
                    }
                }
            }
        } else {
            print("error : \(error)")
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        print(error ?? 0)
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        let hsv = HSV((wheelColor?.hue)!, (wheelColor?.saturation)!, (wheelColor?.brightness)!, (wheelColor?.alpha)!)
        var rgb = hsv2rgb(hsv: hsv)
        if rgb.red < 0 {
            rgb.red = 0
        }
        if rgb.green < 0 {
            rgb.green = 0
        }
        if rgb.blue < 0 {
            rgb.blue = 0
        }
        myPeripheral?.writeValue("StartColor".data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
        myPeripheral?.writeValue(String(Int(rgb.red*255)).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
        myPeripheral?.writeValue(String(Int(rgb.green*255)).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
        myPeripheral?.writeValue(String(Int(rgb.blue*255)).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
        myPeripheral?.writeValue(String(Int((wheelColor?.brightness)!*200)).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
        print("viewDidDiasappear1")
        CM.stopScan()
        CM = CBCentralManager()
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
