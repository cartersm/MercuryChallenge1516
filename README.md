# MercuryChallenge1516
![alt-text](https://mercury.okstate.edu/sites/default/files/pictures/mercury_logo.png)
![alt-text](http://rosestem.rose-hulman.edu/s/1554/images/Rose-Hulman-footer-logo.png)

---

## Rose-Hulman's 2016 Mercury Robotics Challenge repo
### _Property of **Rose-Hulman Institute of Technology**_
##### _Author:_ Sean Carter, with assistance from Dawei Sun, Mohith Kumar, and Nicholas Gerber: 
##### Questions should be directed to {cartersm, sund, kumarms, gerbernd}@rose-hulman.edu

---

### Introduction

This repository holds the software components for Rose-Hulman's 2016 entry in the Oklahoma State University Mercury Remote Robot Challenge. 

This software is in partial fulfillment of the requirements of the courses: _ME470/471/472 Capstone Design_

---

### Components
This system is divided into 4 primary components:

1. A Firebase database to act as an intermediary between the Web Application and the Android Application
2. A Web Application to provide a control interface for the robot's driver
3. An Android Application to listen for commands from the Web Application and forward those commands to an Arduino microcontroller
4. An Arduino firmware program to listen for commands from the Android Application, parse them, and control the robot accordingly

This system also leverages Skype for video feedback between the Android Application and the Web Application.

#### Firebase
This component is a dedicated Firebase cloud database (https://www.firebase.com/), which stores commands made from the Web Application and allows them to be read by the Android Application.

#### Web Application
This component is designed as a simple form- and button-based control system for the robot. The primary libraries and framweorks in this system include:

* AngularJS
* Firebase
* Angularfire (the Firebase AngularJS library)
* Bootstrap

#### Android Application
This component is a relay between the Web Application/Firebase and the Arduino firmware. It launches a persistent service in the background to receive updates from the Firebase and forward them to the Arduino firmware as formatted Strings. Libraries and frameworks include:
 
 * Android min API level 17
 * The [Firebase Android API](https://www.firebase.com/docs/android/api/)
 * Android's [USBAccessory API](http://developer.android.com/guide/topics/connectivity/usb/accessory.html) to communicate with the Arduino microcontroller

#### Arduino Firmware
This firmware is designed to connect to the Android Application, receive command strings from it, parse those commands, and act on them. It is designed to:

* Drive four Brushed DC motors by distance or by angle, using the motors' attached encoders to judge distance
* Drive forward autonomously, using PID control with 3 IR Distance sensors to avoid walls
* Open and close a gripper claw using a servo motor
* Raise and lower a drawbridge mechanism with a Brushed DC motor
* launch a 2oz bean bag by releasing a hook with a Brushed DC motor

---