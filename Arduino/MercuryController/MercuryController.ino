#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#define PIN_LED_1 64
#define PIN_LED_2 65
#define PIN_LED_3 66
#define PIN_LED_4 67
#define PIN_LED_5 68
#define PIN_LED_6 69
#define LED_ON 1
#define LED_OFF 0

char manufacturer[] = "Rose-Hulman.mercury";
char model[] = "Controller";
char versionStr[] = "1.0";

char onMessage[] = "I have an idea!";
char offMessage[] = "Nope. Lost it.";

AndroidAccessory acc(manufacturer, 
                     model, 
                     "Controlling the Robot",
                     versionStr, 
                     "https://sites.google.com/site/me435spring2015", 
                     "12345");

char rxBuf[255];
void setup() {
  delay (1500);
  acc.powerOn();
  pinMode(PIN_LED_1, OUTPUT);
  pinMode(PIN_LED_2, OUTPUT);
  pinMode(PIN_LED_3, OUTPUT);
  pinMode(PIN_LED_4, OUTPUT);
  pinMode(PIN_LED_5, OUTPUT);
  pinMode(PIN_LED_6, OUTPUT);

  // put your setup code here, to run once:

}

void loop() {
  if (acc.isConnected()) {
    int len = acc.read(rxBuf, sizeof(rxBuf), 1);
    if (len > 0) {
      rxBuf[len - 1] = '\0';
      String inputString = String(rxBuf);
      Serial.println("Received Command: " + inputString);
      if (inputString.startsWith("LED")) {
        int startIndex = inputString.indexOf(" ") + 1;
        int endIndex = inputString.indexOf(" ", startIndex);
        String ledNumberStr = inputString.substring(startIndex, endIndex);
        int ledNumber = ledNumberStr.toInt();
        String stateStr = inputString.substring(endIndex + 1);
        int state = (stateStr.equals("ON")) ? LED_ON : LED_OFF;
        switch (ledNumber) {
        case 1:
          digitalWrite(PIN_LED_1, state);
            break;
        case 2:
          digitalWrite(PIN_LED_2, state);
          break;
        case 3:
          digitalWrite(PIN_LED_3, state);
          break;
        case 4:
          digitalWrite(PIN_LED_4, state);
          break;
        case 5:
          digitalWrite(PIN_LED_5, state);
          break;
        case 6:
          digitalWrite(PIN_LED_6, state);
          break;
        }
        acc.write(onMessage, sizeof(onMessage));
      } 
    }
  }
  // put your main code here, to run repeatedly:

}
