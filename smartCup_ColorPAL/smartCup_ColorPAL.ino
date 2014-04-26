#include <Arduino.h>
#include <SoftwareSerial.h>

//#include "TCS3200.h"
#include "HX711.h"
//#include "ColorPAL.h"

const long bluetoothBaudRate = 57600;

const byte HX711_DT = A1;
const byte HX711_SCK = A0;

const int sio = 10;
const int unused = 255; 		// Non-existant pin # for SoftwareSerial
const int sioBaud = 4800;
const int waitDelay = 200;
const int sampleNum = 10;
int sampleTurn = sampleNum;

//ColorPAL colorSensor(ColorPAL_sio,waitDelay);
/*
TCS3200 colorSensor(
TCS3200_S0,TCS3200_S1,
TCS3200_S2,TCS3200_S3,
TCS3200_taosOutPin,TCS3200_LED);
*/
HX711 scale(HX711_DT,HX711_SCK);

SoftwareSerial serin(sio, unused);
SoftwareSerial serout(unused, sio);

void setup() {
  //Serial.begin(bluetoothBaudRate);
  Serial.begin(9600);
  reset();				  // Send reset to ColorPal
  serout.begin(sioBaud);
  pinMode(sio, OUTPUT);
  serout.print("= (00 $ m) !"); // Loop print values, see ColorPAL documentation
  serout.end();			  // Discontinue serial port for transmitting

  serin.begin(sioBaud);	        // Set up serial port for receiving
  pinMode(sio, INPUT);
  
  //scale.set_scale(scale.get_units(10)/50);  // this value is obtained by calibrating the scale with known weights; see the README for details
  //scale.tare();
  
  //scale.set_scale(2280.f);                      // this value is obtained by calibrating the scale with known weights; see the README for details
  //scale.tare();	
  //scale.set_scale(scale.get_units(10)/0.05);
  
}

void loop() {
  
  while(sampleTurn){
    readData();
  }
  sampleTurn = sampleNum;
  
  /*
  Serial.print("avg_weight:");
  Serial.println(scale.get_units(10));
  Serial.println("");
  scale.power_down();			        // put the ADC in sleep mode
  delay(2000);
  scale.power_up();
  */
}

// Reset ColorPAL; see ColorPAL documentation for sequence
void reset() {
  delay(200);
  pinMode(sio, OUTPUT);
  digitalWrite(sio, LOW);
  pinMode(sio, INPUT);
  while (digitalRead(sio) != HIGH);
  pinMode(sio, OUTPUT);
  digitalWrite(sio, LOW);
  delay(80);
  pinMode(sio, INPUT);
  delay(waitDelay);
}

void readData() {
  char buffer[32];
  
  if (serin.available() > 0) {
    // Wait for a $ character, then read three 3 digit hex numbers
    buffer[0] = serin.read();
    if (buffer[0] == '$') {
      for(int i = 0; i < 9; i++) {
        while (serin.available() == 0);     // Wait for next input character
        buffer[i] = serin.read();
        if (buffer[i] == '$')               // Return early if $ character encountered
          return;
      }
      parseAndPrint(buffer);
      delay(10);
    }
  }
}

// Parse the hex data into integers
void parseAndPrint(char * data) {
  int rgb[3] = {0};
  sscanf (data, "%3x%3x%3x", rgb, rgb+1, rgb+2);
  char buffer[32];
  sprintf(buffer, "RGB:%d %d %d", rgb[0], rgb[1], rgb[2]);
  Serial.println(buffer);
  sampleTurn--;
  return;
}
