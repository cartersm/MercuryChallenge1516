#include <PID_v1.h>
#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <Servo.h>

//////// IR Sensors ///
int Left_Sensor = A2;
int Left_Sensor_Reading;
int Right_Sensor = A4;
int Right_Sensor_Reading;
int Front_Sensor = A3;
int Front_Sensor_Reading;

///////// States //////
#define STOP 0
#define DRIVING_STRAIGHT 1
#define TURNING 2
#define SERPINTINE_MODE 3
#define BACKANDSTOP 4

///// Motors /////////
int MOTOR1_PWM = 7;
int MOTOR1_PHASE = 6;

int MOTOR2_PWM = 11;
int MOTOR2_PHASE = 10;

int MOTOR3_PWM = 9;
int MOTOR3_PHASE = 8;

int MOTOR4_PWM = 5;
int MOTOR4_PHASE = 4;

int DRAWBRIDGE1 = 45;      // drawbridge
int DRAWBRIDGE2 = 47;

int DRAWBRIDGE_ENABLE = 2;
int DRAWBRIDGE_SWITCH = 48;

int CLAW1 = 39;
int CLAW2 = 41;
int CLAW_ENABLE = 3;

// boolean flags//
boolean isLaunched = false;
boolean isOpen = false;
boolean seesawMode = false;
///// Servos ///////////
int Servo1 = 12;

///////// PID ////////////////////////////
double Setpoint, FrontSetpoint, Discrepancy, Input, Output;
double Kp = 0.3;
double Ki = 0.0005;
double Kd = 0.05;          // do some changes with these parameters
double IR_BIAS = 150.0;
PID myPID(&Input, &Output, &Setpoint, Kp, Ki, Kd, DIRECT);

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
Servo myservo1;
int Motor_Speed = 200;
void setup() {
  pinMode(DRAWBRIDGE_ENABLE, OUTPUT);
  analogWrite(DRAWBRIDGE_ENABLE, 0);
  pinMode(MOTOR1_PWM, OUTPUT);
  pinMode(MOTOR1_PHASE, OUTPUT);
  pinMode(MOTOR2_PWM, OUTPUT);
  pinMode(MOTOR2_PHASE, OUTPUT);
  pinMode(MOTOR3_PWM, OUTPUT);
  pinMode(MOTOR3_PHASE, OUTPUT);
  pinMode(MOTOR4_PWM, OUTPUT);
  pinMode(MOTOR4_PHASE, OUTPUT);
  pinMode(DRAWBRIDGE1, OUTPUT);
  pinMode(DRAWBRIDGE2, OUTPUT);
  pinMode(DRAWBRIDGE_SWITCH, INPUT_PULLUP);

  //  pinMode(encoder1PinB, INPUT);
  //  pinMode(encoder2PinB, INPUT);
  //  pinMode(encoder3PinB, INPUT);
  //  pinMode(encoder4PinB, INPUT);
  myservo1.attach(Servo1);

  attachInterrupt(2, doEncoder4, CHANGE); // PIN 21 Motor4
  attachInterrupt(3, doEncoder3, CHANGE); // PIN 20 Motor3
  attachInterrupt(4, doEncoder2, CHANGE); // PIN 19 Motor2
  attachInterrupt(5, doEncoder1, CHANGE); // PIN 18 Motor1

  Serial.begin(9600);
  Setpoint = (getLeftSensorValue() + getRightSensorValue()) / 2.0;
  FrontSetpoint = getFrontSensorValue();
  Discrepancy = getLeftSensorValue() - getRightSensorValue();
  Serial.print("Setpoint: ");
  Serial.println(Setpoint);
  Serial.print("Front Setpoint: ");
  Serial.println(FrontSetpoint);
  Serial.print("Discrepancy: ");
  Serial.println(Discrepancy);

  delay (1500);
  acc.powerOn();
}
void loop() {
  if (acc.isConnected()) {
    int len = acc.read(rxBuf, sizeof(rxBuf), 1);
    if (len > 0) { // we've received a message
      rxBuf[len - 1] = '\0';
      inputString = String(rxBuf);
      Serial.println(inputString);
      if (inputString.equals("MOTORS 0 0 false false")) {
        CurrentState = STOP;
      }
      else if (inputString.equals("MOTORS 0 0 false true")) {
        seesawMode = true;
      }
      else if (inputString.equals("MOTORS 0 0 true false")) {
        CurrentState = SERPINTINE_MODE;
        seesawMode = false;
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

      else if (inputString.startsWith("GRIPPER")) {
        Serial.println(inputString);
        int launchStartIndex = inputString.indexOf(" ") + 1;
        int launchEndIndex =  inputString.indexOf(" ", launchStartIndex);

        int locationStartIndex = launchEndIndex + 1;
        int locationEndIndex = inputString.indexOf(" ", locationStartIndex);

        int positionStartIndex = locationEndIndex + 1;
        int positionEndIndex = inputString.indexOf(" ", positionStartIndex);

        String launchStr = inputString.substring(launchStartIndex, launchEndIndex);
        String locationStr = inputString.substring(locationStartIndex, locationEndIndex);
        String positionStr = inputString.substring(positionStartIndex, positionEndIndex);

        if (launchStr.equals("true") && isLaunched == false) {   //////***************////////
          // motor command
          analogWrite(CLAW_ENABLE, 255);
          digitalWrite(CLAW1, HIGH);     //need to change
          digitalWrite(CLAW2, LOW);
          delay(50);
          isLaunched = true;
        }
        // drawbridge
        else if (locationStr.equalsIgnoreCase("raised") && digitalRead(DRAWBRIDGE_SWITCH) == HIGH) {
          digitalWrite(DRAWBRIDGE1, HIGH);
          digitalWrite(DRAWBRIDGE2, LOW);
          analogWrite(DRAWBRIDGE_ENABLE, 255);
          while (digitalRead(DRAWBRIDGE_SWITCH) == HIGH) {
            delay(100);
          }
          analogWrite(DRAWBRIDGE_ENABLE, 0);
        }
        else if (locationStr.equalsIgnoreCase("lowered") && digitalRead(DRAWBRIDGE_SWITCH) == LOW) {
          digitalWrite(DRAWBRIDGE1, LOW);
          digitalWrite(DRAWBRIDGE2, HIGH);
          analogWrite(DRAWBRIDGE_ENABLE, 255);
          delay(4000);
          analogWrite(DRAWBRIDGE_ENABLE, 0);

        }
        /////////////////// gripper  ///////////////////////////
        if (positionStr.equalsIgnoreCase("open")) {
          myservo1.write(30);
          delay(1000);
        }
        else if (positionStr.equalsIgnoreCase("closed")) {
          myservo1.write(120);
          delay(1000);
        }
      }
    }

    double frontValue = getFrontSensorValue();
    //    Serial.print("Front value: ");
    //    Serial.println(frontValue);

    if (!seesawMode && 
         frontValue > FrontSetpoint + 200 && digitalRead(DRAWBRIDGE_SWITCH) == LOW) {
      CurrentState = BACKANDSTOP;
    }
    switch (CurrentState) {
      case STOP:
        stopAllMotors();
        seesawMode = false;
        break;
      case BACKANDSTOP:
        stopAllMotors();
        delay(250);
        digitalWrite(MOTOR1_PHASE, 0);
        digitalWrite(MOTOR2_PHASE, 0);
        digitalWrite(MOTOR3_PHASE, 0);
        digitalWrite(MOTOR4_PHASE, 0);
        analogWrite(MOTOR1_PWM, 200);
        analogWrite(MOTOR2_PWM, 200);
        analogWrite(MOTOR3_PWM, 200);
        analogWrite(MOTOR4_PWM, 200);
        delay(500);
        CurrentState = STOP;
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
        if (abs(encoder1Pos) > abs(counts1) &&
            abs(encoder2Pos) > abs(counts2) &&
            abs(encoder3Pos) > abs(counts3) &&
            abs(encoder4Pos) > abs(counts4)) {
          CurrentState = STOP;
        }
        break;
      case TURNING:
        countsangle1 = angleToCounts1(angle);
        countsangle2 = angleToCounts2(angle);
        countsangle3 = angleToCounts3(angle);
        countsangle4 = angleToCounts4(angle);
        turnRobot(angle);
        break;
      case SERPINTINE_MODE:
        myPID.SetMode(AUTOMATIC);
        int L = getLeftSensorValue();
        int R = getRightSensorValue();
        digitalWrite(MOTOR1_PHASE, 1);  // forward
        digitalWrite(MOTOR2_PHASE, 1);  // forward
        digitalWrite(MOTOR3_PHASE, 1);  // forward
        digitalWrite(MOTOR4_PHASE, 1);  // forward
        int left_spd = 200;
        int right_spd = 200;
        Input = -abs(((L + IR_BIAS) - R) - Discrepancy);
        myPID.Compute();
        Serial.print("PID output: ");
        Serial.println(Output);
        Output = abs(Output);
        if (Output < 30) {
          Output = 0;
        }
        if (R > L) {
          Output = -Output;
        }
        //Same speed for 1 and 3  (R)
        analogWrite(MOTOR1_PWM, constrain(left_spd - Output, 0, 255));
        analogWrite(MOTOR3_PWM, constrain(left_spd - Output, 0, 255));
        // Same speed for 2 and 4 (L)
        analogWrite(MOTOR2_PWM, constrain(right_spd + Output, 0, 255));
        analogWrite(MOTOR4_PWM, constrain(right_spd + Output, 0, 255));
        break;
    }
  }
}
////////////////////// Angle degrees to encoder counts //////////////////////
int angleToCounts1(int angle) {
  int counts = (angle / 360.0) * 8893;
  return (int) counts;
}
int angleToCounts2(int angle) {
  int counts = (angle / 360.0) * 7449;
  return (int) counts;
}
int angleToCounts3(int angle) {
  int counts = (angle / 360.0) * 7293;
  return (int) counts;
}
int angleToCounts4(int angle) {
  int counts = (angle / 360.0) * 9270;
  return (int) counts;
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
  isTurning = true;
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
  if (abs(countsangle1) > abs (encoder1Pos) &&
      abs(countsangle2) > abs (encoder2Pos) &&
      abs(countsangle3) > abs (encoder3Pos) &&
      abs(countsangle4) > abs (encoder4Pos)) {
    analogWrite(MOTOR1_PWM, 200);
    analogWrite(MOTOR2_PWM, 200);
    analogWrite(MOTOR3_PWM, 200);
    analogWrite(MOTOR4_PWM, 200);
  }
  else {
    CurrentState = STOP;
  }
}
//////////////////// Stop motors ///////////////////////////////////////////
void stopAllMotors() {
  analogWrite(MOTOR1_PWM, 0);
  analogWrite(MOTOR2_PWM, 0);
  analogWrite(MOTOR3_PWM, 0);
  analogWrite(MOTOR4_PWM, 0);
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


int getRightSensorValue() {
  Right_Sensor_Reading = analogRead(Right_Sensor);
  return Right_Sensor_Reading;

}

int getLeftSensorValue() {
  Left_Sensor_Reading = analogRead(Left_Sensor);
  return Left_Sensor_Reading;
}

int getFrontSensorValue() {
  Front_Sensor_Reading = analogRead(Front_Sensor);
  return Front_Sensor_Reading;
}
