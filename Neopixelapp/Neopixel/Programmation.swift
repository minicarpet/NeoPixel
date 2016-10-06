//
//  Programmation.swift
//  Neopixel
//
//  Created by Logan Gallois on 17/06/2016.
//  Copyright © 2016 Logan Gallois. All rights reserved.
//

import UIKit
import CoreBluetooth

class Programmation: UIViewController, CBCentralManagerDelegate, CBPeripheralDelegate, UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet var EditButton: UIBarButtonItem!
    @IBOutlet var NavigationBar: UINavigationBar!
    @IBOutlet var TableView: UITableView!
    
    let refreshControl = UIRefreshControl()
    var grayViewEffect = UIVisualEffectView()
    var AlertBLE = UIAlertController()
    
    let CM = CBCentralManager()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        CM.delegate = self
        refreshControl.attributedTitle = NSAttributedString(string: "Tirer pour changer la luminosité")
        refreshControl.addTarget(self, action: #selector(self.changeBrightness), for: UIControlEvents.valueChanged)
        
        self.TableView.addSubview(refreshControl)
        myPeripheral?.delegate = self
        TableView.dataSource = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        CM.delegate = self
        myPeripheral?.delegate = self
        TableView.reloadData()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if myPeripheral == nil {
            grayViewEffect.effect = UIBlurEffect(style: .dark)
            let origin = CGPoint(x: 0, y: UIApplication.shared.statusBarFrame.height)
            let size = CGSize(width: self.view.frame.width, height: self.view.frame.height - UIApplication.shared.statusBarFrame.height - NavigationBar.frame.size.height)
            grayViewEffect.frame = CGRect(origin: origin, size: size)
            self.view.addSubview(grayViewEffect)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
        
    }
    
    @IBAction func AddFunction(_ sender: UIBarButtonItem) {
        if Programs.count == 256 {
            let toMuchAlert = UIAlertController(title: "Wow", message: "Tu as mis trop de fonction", preferredStyle: UIAlertControllerStyle.alert)
            toMuchAlert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
            self.present(toMuchAlert, animated: true, completion: nil)
        } else {
            let ViewToShow = self.storyboard!.instantiateViewController(withIdentifier: "fonctionview") as! DefineFonctionView
            self.present(ViewToShow, animated: true, completion: nil)
        }
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
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
    
    private func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : AnyObject], rssi RSSI: NSNumber) {
        if peripheral.name == "NeoPixel" {
            myPeripheral = peripheral
            CM.connect(myPeripheral!, options: nil)
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
            AlertBLE.addAction(UIAlertAction(title: "Ok", style: .default, handler: {(alert: UIAlertAction!) in
                self.view.addSubview(self.grayViewEffect)
            }))
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
            self.view.addSubview(self.grayViewEffect)
            self.CM.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
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
            RxLabel.text = String(value)
            buffer[numData] = value
            numData += 1
            if numData == 5 {
                numData = 0
                if buffer[0] == 0 && buffer[1] == 1 && buffer[2] == 0 && buffer[3] == 0 && buffer[4] != 0 {
                    numFunction = buffer[4]
                    Programs.removeAll(keepingCapacity: false)
                } else {
                    Programs.append(buffer)
                    if Programs.count == numFunction {
                        synchro = true
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
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Programs.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = self.TableView.dequeueReusableCell(withIdentifier: "FonctionName", for: indexPath)
        cell.textLabel!.text = getName(Index: Int(Programs[indexPath.row][0])-1) + " avec un delai de : " + String(Programs[indexPath.row][4])
        let red = CGFloat(Programs[indexPath.row][1]) / 255.0
        let green = CGFloat(Programs[indexPath.row][2]) / 255.0
        let blue = CGFloat(Programs[indexPath.row][3]) / 255.0
        cell.backgroundColor = UIColor(red: red, green: green, blue: blue, alpha: 1.0)
        if cell.backgroundColor == UIColor(red: 0, green: 0, blue: 0, alpha: 1.0) {
            cell.textLabel?.textColor = UIColor.white
        }
        cell.showsReorderControl = true
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        return cell
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            Programs.remove(at: indexPath.row)
            tableView.deleteRows(at: [indexPath], with: .fade)
        }
    }
    
    func tableView(_ tableView: UITableView, moveRowAt sourceIndexPath: IndexPath, to destinationIndexPath: IndexPath) {
        if (sourceIndexPath.row - destinationIndexPath.row) > 0 {
            for i in 1...(sourceIndexPath.row - destinationIndexPath.row) {
                swap(&Programs[sourceIndexPath.row - i], &Programs[sourceIndexPath.row - i + 1])
            }
        } else {
            for i in 1...(destinationIndexPath.row - sourceIndexPath.row) {
                swap(&Programs[sourceIndexPath.row + i], &Programs[sourceIndexPath.row + i - 1])
            }
        }
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //Future code to change setting of function
    }
    
    func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    @IBAction func EditAction(_ sender: UIBarButtonItem) {
        if sender.title == "Terminer" {
            if synchro {
                sender.title = "Editer"
                TableView.setEditing(false, animated: true)
                sendData()
            } else {
                let waitSync = UIAlertController(title: "Synchronisation non terminée", message: "Merci d'attendre la fin de la synchronisation et de réessayer plus tard...", preferredStyle: .alert)
                waitSync.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
                self.present(waitSync, animated: true, completion: nil)
            }
        } else {
            sender.title = "Terminer"
            TableView.setEditing(true, animated: true)
        }
    }
    
    func getName(Index: Int) -> String {
        return Names[Index]
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
    }
    
    func sendData() {
        numFunction = Programs.count
        myPeripheral?.writeValue("Start".data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
        for program in Programs {
            myPeripheral?.writeValue(String(program[0]).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
            myPeripheral?.writeValue(String(program[1]).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
            myPeripheral?.writeValue(String(program[2]).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
            myPeripheral?.writeValue(String(program[3]).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
            myPeripheral?.writeValue(String(program[4]).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
        }
        myPeripheral?.writeValue("End".data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
    }
    
    func changeBrightness() {
        grayViewEffect.effect = UIBlurEffect(style: .dark)
        grayViewEffect.frame = self.view.frame
        self.view.addSubview(grayViewEffect)
        let alertBrightness = UIAlertController(title: "Controle de luminosité", message: "Changer le slide pour changer la luminosité\n\n", preferredStyle: .alert)
        let slider = UISlider()
        alertBrightness.addAction(UIAlertAction(title: "Valider", style: .default, handler: {(alert: UIAlertAction!) in
            self.refreshControl.endRefreshing()
            myPeripheral?.writeValue("Brightness".data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
            myPeripheral?.writeValue(String(Int(slider.value * 200)).data(using: String.Encoding.ascii)!, for: RxCarac!, type: CBCharacteristicWriteType.withResponse)
            self.grayViewEffect.removeFromSuperview()
        }))
        alertBrightness.addAction(UIAlertAction(title: "Annuler", style: .destructive, handler: {(alert: UIAlertAction!) in
            self.refreshControl.endRefreshing()
            self.grayViewEffect.removeFromSuperview()
        }))
        self.present(alertBrightness, animated: true, completion: {
            slider.frame = CGRect(x: 20, y: 80, width: (alertBrightness.view.subviews.first?.frame.width)! - 40, height: 50)
            slider.value = 0.5
            alertBrightness.view.addSubview(slider)
        })
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
