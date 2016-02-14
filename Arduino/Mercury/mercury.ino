#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#include <LiquidCrystal.h>
#include <Servo.h>

// initialize the library with the numbers of the interface pins
//LiquidCrystal lcd(31, 33, 35, 37, 39, 41);

//LiquidCrystal lcd(12, 11, 5, 4, 3, 2);

//////// IR Sensors ///
#define LEFT_INPUT A3    // NEED TO CHANGE
#define RIGHT_INPUT A2
#define FRONT_INPUT A1

#define STOP 0
#define DRIVING_STRAIGHT 1
#define TURNING 2
#define SERPINTINE_MODE 3


///// Motors /////////
int MOTOR1_PWM = 7;
int MOTOR1_PHASE = 6;

int MOTOR2_PWM = 11;
int MOTOR2_PHASE = 10;

int MOTOR3_PWM = 9;
int MOTOR3_PHASE = 8;

int MOTOR4_PWM = 5;
int MOTOR4_PHASE = 4;

//int MOTOR5_PWM = 2;      // PIN 2 and 3 are for interrupts
//int MOTOR5_PHASE = 3;
//int MOTOR6_PWM = 4;
//int MOTOR6_PHASE = 5;
//
///// Servos ///////////
//int Servo1 = 5;        // NEED TO ADD

/////// Sensor Values //////
//double Setpoint;
//double Kp, Ki, Kd;

/////// Encoders Interrupts ///////////

// Pin A is the signal, which should be attached to interrupt

int encoder1PinA = 18;
int encoder1PinB = 22;

int encoder2PinA = 19;
int encoder2PinB = 24;

int encoder3PinA = 20;
int encoder3PinB = 26;

int encoder4PinA = 21;
int encoder4PinB = 28;

int CurrentState;

int Left_Sensor_Reading;
int Right_Sensor_Reading;
int Front_Sensor_Reading;
int Error;

int angle;
int distance;

// Encoder values
int encoder1Pos = 0;
int encoder2Pos = 0;
int encoder3Pos = 0;
int encoder4Pos = 0;
int counts1 = 0;
int counts2 = 0;
int counts3 = 0;
int counts4 = 0;

int countsangle1 = 0;
int countsangle2 = 0;
int countsangle3 = 0;
int countsangle4 = 0;

bool motor1Driving = false;
bool motor2Driving = false;
bool motor3Driving = false;
bool motor4Driving = false;
bool isTurning = false;
bool serpentineMode = false;

char manufacturer[] = "Rose-Hulman.mercury";
char model[] = "Controller";
char versionStr[] = "1.0";

char onMessage[] = "I have an idea!";
char offMessage[] = "Nope. Lost it.";
String inputString;
AndroidAccessory acc(manufacturer,
                     model,
                     "Controlling the Robot",
                     versionStr,
                     "https://sites.google.com/site/me435spring2015",
                     "12345");
char rxBuf[255];

//PID myPID(&Input, &Output, &Setpoint, Kp, Ki, Kd, DIRECT);
Servo myservo1;

int Motor_Speed = 200;

void setup() {
  Serial.begin(9600);
  delay (1500);

  pinMode(MOTOR1_PWM, OUTPUT);
  pinMode(MOTOR1_PHASE, OUTPUT);
  pinMode(MOTOR2_PWM, OUTPUT);
  pinMode(MOTOR2_PHASE, OUTPUT);
  pinMode(MOTOR3_PWM, OUTPUT);
  pinMode(MOTOR3_PHASE, OUTPUT);
  pinMode(MOTOR4_PWM, OUTPUT);
  pinMode(MOTOR4_PHASE, OUTPUT);
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  //  pinMode(encoder1PinB, INPUT);
  //  pinMode(encoder2PinB, INPUT);
  //  pinMode(encoder3PinB, INPUT);
  //  pinMode(encoder4PinB, INPUT);

  // set up the LCD's number of columns and rows:
  //  lcd.begin(16, 2);

  //  myservo1.attach(Servo1);

  //  attachInterrupt(0, doEncoder1, CHANGE); // PIN 2
  //  attachInterrupt(1, doEncoder2, CHANGE); // PIN 3

  attachInterrupt(2, doEncoder4, CHANGE); // PIN 21 Motor4
  attachInterrupt(3, doEncoder3, CHANGE); // PIN 20 Motor3

  attachInterrupt(4, doEncoder2, CHANGE); // PIN 19 Motor2
  attachInterrupt(5, doEncoder1, CHANGE); // PIN 18 Motor1
  //  myPID.SetMode(AUTOMATIC);
  acc.powerOn();
}
void loop() {
  if (acc.isConnected()) {
//    digitalWrite(2, HIGH);
//    delay(500);
//    digitalWrite(2, LOW);
//    delay(500);
    int len = acc.read(rxBuf, sizeof(rxBuf), 1);
    if (len > 0) { // we've recevied a message
      // LED will flash
//      digitalWrite(3, HIGH);
//      delay(500);
//      digitalWrite(3, LOW);
//      delay(500);
      ///////////////////
      rxBuf[len - 1] = '\n';
      inputString = String(rxBuf);

      if (inputString.equals("MOTORS 0 0 false")) {
        CurrentState = STOP;
      } 
      else if (inputString.startsWith("MOTORS")) {           // Obtain distance the robot needs to travel
        int distanceStartIndex =  inputString.indexOf(" ") + 1;
        int distanceEndIndex = inputString.indexOf(" ", distanceStartIndex);
        String distanceStr = inputString.substring(distanceStartIndex, distanceEndIndex);
        distance = distanceStr.toInt();
        encoder1Pos = 0;
        encoder2Pos = 0;
        encoder3Pos = 0;
        encoder4Pos = 0;

        int angleStartIndex = distanceEndIndex + 1;
        int angleEndIndex = inputString.indexOf(" ", angleStartIndex);
        String angleStr = inputString.substring(angleStartIndex, angleEndIndex);
        angle = angleStr.toInt();

        if (angle != 0) {
          CurrentState = TURNING;
        }
        else if (distance != 0) {
          CurrentState = DRIVING_STRAIGHT;
        }
        else {
          CurrentState = STOP;
        }
      }
      // FIXME: this should be right after the stop command check
      else if (inputString.equals("MOTORS 0 0 true")) {
        CurrentState = SERPINTINE_MODE;
      }

    }

    switch (CurrentState) {
      case STOP:
        analogWrite(MOTOR1_PWM, 0);
        analogWrite(MOTOR2_PWM, 0);
        analogWrite(MOTOR3_PWM, 0);
        analogWrite(MOTOR4_PWM, 0);
        break;
      case DRIVING_STRAIGHT:
        counts1 = DistanceToCounts1(distance);
        counts2 = DistanceToCounts2(distance);
        counts3 = DistanceToCounts3(distance);
        counts4 = DistanceToCounts4(distance);

        driveMotor1();
        driveMotor2();
        driveMotor3();
        driveMotor4();

        Serial.print("Encoder 1: ");
        Serial.println(encoder1Pos);

        if (abs(encoder1Pos) > abs(counts1) &&
            abs(encoder2Pos) > abs(counts2) &&
            abs(encoder3Pos) > abs(counts3) &&
            abs(encoder4Pos) > abs(counts4)) {
          CurrentState = STOP;
        }
        break;
      case TURNING:

        break;
      case SERPINTINE_MODE:

        break;
    }






    //        if (motor1Driving && motor2Driving && motor3Driving && motor4Driving) {
    //          driveMotor1();
    //          driveMotor2();
    //          driveMotor3();
    //          driveMotor4();
    //        } else {
    //          stopAllMotors();
    //        }


    //
    //        countsangle1 = angleToCounts1(angle);
    //        countsangle2 = angleToCounts2(angle);
    //        countsangle3 = angleToCounts3(angle);
    //        countsangle4 = angleToCounts4(angle);
    //
    //        encoder1Pos = 0;
    //        encoder2Pos = 0;
    //        encoder3Pos = 0;
    //        encoder4Pos = 0;
    //
    //        if (angle != 0) {
    //          turnRobot(angle);
    //          if (isTurning) {
    //            turnRobot(angle);
    //          } else {
    //            stopAllMotors();
    //          }
    //        }

    // Decide if it is serpentine mode
    //      int modeIndex = angleStartIndex + 1;
    //      int angleEndIndex = inputString.indexOf(" ", angleStartIndex);
    //      String angleStr = inputString.substring(angleStartIndex, angleEndIndex);
    //      angle = angleStr.toInt();

  }
}
////////////////////// Angle degrees to encoder counts //////////////////////
int angleToCounts1(int angle) {
  int counts = (angle / 360) * 8893;
  return counts;
}
int angleToCounts2(int angle) {
  int counts = (angle / 360) * 7449;
  return counts;
}
int angleToCounts3(int angle) {
  int counts = (angle / 360) * 7293;
  return counts;
}
int angleToCounts4(int angle) {
  int counts = (angle / 360) * 9270;
  return counts;
}
////////////////////// Distance inch to encoder counts ///////////////////////
int DistanceToCounts1(int distance) {
  int counts = (distance / 7.225) * 1250;
  return counts;
}
int DistanceToCounts2(int distance) {
  int counts = (distance / 7.225) * 1250;
  return counts;
}
int DistanceToCounts3(int distance) {
  int counts = (distance / 7.225) * 1265;
  return counts;
}
int DistanceToCounts4(int distance) {
  int counts = (distance / 7.225) * 1270;
  return counts;
}
///////////////////////// Drive Motors ////////////////////////////////////////
void determineDrivingMotor1() {
  if (counts1 == 0 || abs(encoder1Pos) > abs(counts1)) {
    motor1Driving = false;
    analogWrite(MOTOR1_PWM, 0);

  }
  else {
    motor1Driving = true;
  }
}
void determineDrivingMotor2() {
  if (counts2 == 0 || abs(encoder2Pos) > abs(counts2)) {
    motor2Driving = false;
    analogWrite(MOTOR2_PWM, 0);

  }
  else {
    motor2Driving = true;
  }
}
void determineDrivingMotor3() {
  if (counts3 == 0 || abs(encoder3Pos) > abs(counts3)) {
    motor3Driving = false;
    analogWrite(MOTOR3_PWM, 0);

  }
  else {
    motor3Driving = true;
  }
}
void determineDrivingMotor4() {
  if (counts4 == 0 || abs(encoder4Pos) > abs(counts4)) {
    motor4Driving = false;
    analogWrite(MOTOR4_PWM, 0);

  }
  else {
    motor4Driving = true;
  }
}
//////////////////////////////////////////////////
void driveMotor1() {
  if (counts1 > 0) {
    digitalWrite(MOTOR1_PHASE, 1);  // forward
  }
  else {
    digitalWrite(MOTOR1_PHASE, 0);  // backward
  }
  analogWrite(MOTOR1_PWM, 255);

}
void driveMotor2() {
  if (counts2 > 0) {
    digitalWrite(MOTOR2_PHASE, 1);  // forward
  }
  else {
    digitalWrite(MOTOR2_PHASE, 0);  // backward
  }
  analogWrite(MOTOR2_PWM, 255);

}
void driveMotor3() {
  if (counts3 > 0) {
    digitalWrite(MOTOR3_PHASE, 1);  // forward
  }
  else {
    digitalWrite(MOTOR3_PHASE, 0);  // backward
  }
  analogWrite(MOTOR3_PWM, 255);

}
void driveMotor4() {
  if (counts4 > 0) {
    digitalWrite(MOTOR4_PHASE, 1);  // forward
  }
  else {
    digitalWrite(MOTOR4_PHASE, 0);  // backward
  }
  analogWrite(MOTOR4_PWM, 255);
}
/////////////////////// Turn robot ////////////////////////////////////
void turnRobot(int angle) {
  isTurning = true ;
  if (angle > 0) {                     // Counter -- Clockwise
    digitalWrite(MOTOR1_PHASE, 1);  // forward
    digitalWrite(MOTOR3_PHASE, 1);  // forward
    digitalWrite(MOTOR2_PHASE, 0);  // backward
    digitalWrite(MOTOR4_PHASE, 0);  // backward
  }
  else {                                 // Clockwise
    digitalWrite(MOTOR1_PHASE, 0);  // backward
    digitalWrite(MOTOR3_PHASE, 0);  // backward
    digitalWrite(MOTOR2_PHASE, 1);  // forward
    digitalWrite(MOTOR4_PHASE, 1);  // forward
  }

  if ( (abs(countsangle1) < abs (encoder1Pos)) && (abs(countsangle2) < abs (encoder2Pos)) && (abs(countsangle3) < abs (encoder3Pos)) && (abs(countsangle4) < abs (encoder4Pos))) {
    analogWrite(MOTOR1_PWM, 200);
    analogWrite(MOTOR2_PWM, 200);
    analogWrite(MOTOR3_PWM, 200);
    analogWrite(MOTOR4_PWM, 200);
  }
  else {
    stopAllMotors();
  }
}
//////////////////// Stop motors ///////////////////////////////////////////
void stopAllMotors() {
  analogWrite(MOTOR1_PWM, 0);
  analogWrite(MOTOR2_PWM, 0);
  analogWrite(MOTOR3_PWM, 0);
  analogWrite(MOTOR4_PWM, 0);
  motor1Driving = false;
  motor2Driving = false;
  motor3Driving = false;
  motor4Driving = false;
}
/////////////////////////////  Encoders/////////////////////////////////
void doEncoder1() {
  if (digitalRead(encoder1PinA) == digitalRead(encoder1PinB)) {
    encoder1Pos++;
  } else {
    encoder1Pos--;
  }
}
void doEncoder2() {
  if (digitalRead(encoder2PinA) == digitalRead(encoder2PinB)) {
    encoder2Pos++;
  } else {
    encoder2Pos--;
  }
}
void doEncoder3() {
  if (digitalRead(encoder3PinA) == digitalRead(encoder3PinB)) {
    encoder3Pos++;
  } else {
    encoder3Pos--;
  }
}
void doEncoder4() {
  if (digitalRead(encoder4PinA) == digitalRead(encoder4PinB)) {
    encoder4Pos++;
  } else {
    encoder4Pos--;
  }
}
//////////////////////////Sensors /////////////////////////////////


//void updateSensorValues() {
//  Left_Sensor_Reading = analogRead(LEFT_INPUT);
//  Right_Sensor_Reading = analogRead(RIGHT_INPUT);
//  //  Front_Sensor_Reading = analogRead(FRONT_INPUT);
//  Error = Left_Sensor_Reading - Right_Sensor_Reading;
//  lcd.setCursor(0, 1);
//  lcd.print(Left_Sensor_Reading, DEC);
//  delay(200);
//}

