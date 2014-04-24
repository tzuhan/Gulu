#include <Arduino.h>
#include <SoftwareSerial.h>

//#include "TCS3200.h"
//#include "HX711.h"
#include "ColorPAL.h"

const byte HX711_DT = A1;
const byte HX711_SCK = A0;

const int ColorPAL_sio = 10;
const int waitDelay = 200;

ColorPAL colorSensor(ColorPAL_sio,waitDelay);
/*
TCS3200 colorSensor(
TCS3200_S0,TCS3200_S1,
TCS3200_S2,TCS3200_S3,
TCS3200_taosOutPin,TCS3200_LED);
*/
//HX711 scale(HX711_DT,HX711_SCK);

void setup() {
  Serial.begin(9600);
  
  /*
  scale.set_scale(scale.get_units(10)/50);  // this value is obtained by calibrating the scale with known weights; see the README for details
  scale.tare();
  */
  //scale.set_scale(2280.f);                      // this value is obtained by calibrating the scale with known weights; see the README for details
  //scale.tare();	
  //scale.set_scale(scale.get_units(10)/0.05);
  
}

void loop() {
  Serial.println("test");
  //colorSensor.detectColor();
  colorSensor.readData();
  /*
  Serial.print("avg_weight:");
  Serial.println(scale.get_units(10));
  Serial.println("");
  scale.power_down();			        // put the ADC in sleep mode
  delay(2000);
  scale.power_up();
  */ 
}
