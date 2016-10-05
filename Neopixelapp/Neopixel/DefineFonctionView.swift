//
//  DefineFonctionView.swift
//  Neopixel
//
//  Created by Logan Gallois on 20/06/2016.
//  Copyright © 2016 Logan Gallois. All rights reserved.
//

import UIKit

class DefineFonctionView: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {

    @IBOutlet var textField: UITextField!
    @IBOutlet var delaiField: UITextField!
    @IBOutlet var subView: UIView!
    
    var wheelColor: SwiftHSVColorPicker? = nil
    var rgb: RGB?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let PickerView = UIPickerView()
        
        let toolBar = UIToolbar(frame: CGRect(origin: CGPoint(x: 0, y: 0), size: CGSize(width: self.view.frame.width, height: 50)))
        let Button = UIBarButtonItem(title: "Ok", style: .done, target: self, action: #selector(DefineFonctionView.validate))
        let flexibleSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: self, action: nil)
        toolBar.setItems([flexibleSpace, Button, flexibleSpace], animated: false)
        toolBar.barStyle = .default
        toolBar.isUserInteractionEnabled = true
        toolBar.isTranslucent = true
        toolBar.sizeToFit()

        PickerView.dataSource = self
        PickerView.delegate = self
        textField.inputView = PickerView
        textField.inputAccessoryView = toolBar
        delaiField.keyboardType = UIKeyboardType.numberPad
        delaiField.inputAccessoryView = toolBar
        
        wheelColor = SwiftHSVColorPicker(frame: CGRect(origin: CGPoint(x: self.view.frame.origin.x + 20, y: self.view.frame.origin.y + self.topLayoutGuide.length + 20), size: CGSize(width: self.view.frame.size.width - 40, height: 500)))
        subView.addSubview(wheelColor!)
        
        wheelColor?.setViewColor(color: UIColor(hue: 0.0, saturation: 0.0, brightness: 1.0, alpha: 1.0))
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return Names.count
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return Names[row]
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        textField.text = Names[row]
        if row == 2 {
            wheelColor?.isUserInteractionEnabled = false
            wheelColor?.setViewColor(color: UIColor(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0))
        } else {
            wheelColor?.isUserInteractionEnabled = true
        }
    }
    
    @IBAction func beginEdit(_ sender: UITextField) {
        sender.text = ""
    }
    
    @IBAction func endEdit(_ sender: UITextField) {
        if sender.text != nil {
            if Int(sender.text!) == nil {
                let AlertView = UIAlertController(title: "Erreur", message: "Merci d'indiquer un delai pour le delai de la fonction", preferredStyle: .alert)
                AlertView.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
                self.present(AlertView, animated: true, completion: nil)
            }
        }
    }
    
    
    func validate() {
        if textField.isEditing {
            textField.resignFirstResponder()
        } else {
            delaiField.resignFirstResponder()
        }
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
    }
    
    @IBAction func Finish(_ sender: UIButton) {
        if delaiField.text != nil {
            if Int(delaiField.text!) == nil {
                let AlertView = UIAlertController(title: "Erreur", message: "Merci d'indiquer un delai pour le delai de la fonction", preferredStyle: .alert)
                AlertView.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
                self.present(AlertView, animated: true, completion: nil)
            } else {
                let hsv = HSV((wheelColor?.hue)!, (wheelColor?.saturation)!, (wheelColor?.brightness)!, (wheelColor?.alpha)!)
                rgb = hsv2rgb(hsv: hsv)
                var red = UInt32((rgb?.red)! * 255)
                var green = UInt32((rgb?.green)! * 255)
                var blue = UInt32((rgb?.blue)! * 255)
                if red < 0 {
                    red = 0
                }
                if green < 0 {
                    green = 0
                }
                if blue < 0 {
                    blue = 0
                }
                Programs.append([UInt32(Names.index(of: textField.text!)!) + 1, red, green, blue, UInt32(delaiField.text!)!])
                self.dismiss(animated: true, completion: nil)
            }
        }
    }
    
    @IBAction func Cancel(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
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
