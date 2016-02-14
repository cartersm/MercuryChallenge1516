#include <PID_v1.h>
#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <Servo.h>

////// IR Sensors ///
#define LEFT_INPUT 1    // NEED TO CHANGE
#define RIGHT_INPUT 2
#define FRONT_INPUT 3

//// Motors /////////
#define FR_MOTOR 1      // NEED TO CHANGE
#define FL_MOTOR 2
#define BR_MOTOR 3
#define BL_MOTOR 4

/// Servos ///////////
#define Servo1 1         // NEED TO CHANGE
#define Servo2 2
#define Servo3 3
#define Servo4 4

//// States  /////////
#define STOP_STATE 0
#define DRIVE_FORWARD_STATE 1
#define TURN_LEFT_STATE 2
#define TURN_RIGHT_STATE 3

///// Sensor Values //////
double Setpoint;
double Kp, Ki, Kd;
double Left_Sensor_Reading;
double Right_Sensor_Reading;
double Front_Sensor_Reading;


int State;
int Error;
int MotorSpeed;

char manufacturer[] = "Rose-Hulman.mercury";
char model[] = "Controller";
char versionStr[] = "1.0";

char onMessage[] = "I have an idea!";
char offMessage[] = "Nope. Lost it.";

AndroidAccessory acc(manufacturer, model, "Controlling the Robot",
                     versionStr, "https://sites.google.com/site/me435spring2013", "12345");

char rxBuf[255];

PID myPID(&Input, &Output, &Setpoint, Kp, Ki, Kd, DIRECT);
Servo myservo1;
Servo myservo2;
Servo myservo3;
Servo myservo4;

void setup() {
  
  myservo1.attach(Servo1);
  myservo2.attach(Servo2);
  myservo3.attach(Servo3);
  myservo4.attach(Servo4);
  
  myPID.SetMode(AUTOMATIC);
  delay (1500);
  acc.powerOn();
}
void loop() {
  updateSensorValues();
  motors();
}

void motors() {
  if (State == DRIVE_FORWARD_STATE) {

    if (Error < 50) {
      // drive forward
      MotorSpeed = 150;                        // NEED TO CHANGE
      analogWrite(FR_MOTOR, MotorSpeed);
      analogWrite(FL_MOTOR, MotorSpeed);
      analogWrite(BR_MOTOR, MotorSpeed);
      analogWrite(BL_MOTOR, MotorSpeed);

    }
    else {
      // run PID codes
      Input = Error;
      Setpoint = 0;
      myPID.Compute();
      analogWrite(FR_MOTOR, Output);            // NEED TO CHANGE
      analogWrite(FL_MOTOR, Output);
      analogWrite(BR_MOTOR, Output);
      analogWrite(BL_MOTOR, Output);
    }
    updateSensorValues();

  }

  else if (State == TURN_LEFT_STATE) {

  }

  else if (State == TURN_RIGHT_STATE) {

  }


  else {
    // stop state
    analogWrite(FR_MOTOR, 0);
    analogWrite(FL_MOTOR, 0);
    analogWrite(BR_MOTOR, 0);
    analogWrite(BL_MOTOR, 0);

  }
}

void stateTransition() {
  if ((Left_Sensor_Reading < 100) && (Right_Sensor_Reading < 100)) {
    State = DRIVE_FORWARD_STATE;
    updateSensorValues();
  }
  else if ((Left_Sensor_Reading < 100) && (Right_Sensor_Reading > 150) && (Front_Sensor_Reading < 100)) {
    State = TURN_RIGHT_STATE;
    updateSensorValues();
  }
  else if ((Left_Sensor_Reading < 100) && (Right_Sensor_Reading > 150) && (Front_Sensor_Reading < 100)) {
    State = TURN_LEFT_STATE;
    updateSensorValues();
  }
  else {
    State = STOP_STATE;
    updateSensorValues();
  }
}

void updateSensorValues() {
  Left_Sensor_Reading = analogRead(LEFT_INPUT);
  Right_Sensor_Reading = analogRead(RIGHT_INPUT);
  Front_Sensor_Reading = analogRead(FRONT_INPUT);
  Error = Left_Sensor_Reading - Right_Sensor_Reading;
}

