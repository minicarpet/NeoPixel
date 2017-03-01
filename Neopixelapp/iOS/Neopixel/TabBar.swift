//
//  TabBar.swift
//  Neopixel
//
//  Created by Logan Gallois on 19/06/2016.
//  Copyright © 2016 Logan Gallois. All rights reserved.
//

import UIKit

class TabBar: UITabBarController {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        let Couleur = self.tabBar.items?.first
        let Prog = self.tabBar.items?.last
        
        let WheelImage = UIImage(named: "WheelColor")?.withRenderingMode(UIImageRenderingMode.alwaysOriginal)
        Couleur?.image = WheelImage
        Couleur?.selectedImage = WheelImage
        let IconProg = #imageLiteral(resourceName: "icon_programmation_30").withRenderingMode(UIImageRenderingMode.alwaysOriginal)
        Prog?.image = IconProg
        Prog?.selectedImage = IconProg
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
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
