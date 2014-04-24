#include <Arduino.h>
#include "TCS3200.h"

TCS3200::TCS3200
(int p_SO,int p_S1,int p_S2,int p_S3,
 int p_taosOutPin,int p_LED) {
    S0 = p_SO;
    S1 = p_S1;
    S2 = p_S2;
    S3 = p_S3;
    LED = p_LED;
    taosOutPin = p_taosOutPin;
    
    //initialize pins
    pinMode(LED,OUTPUT); //LED pinD
    
    //color mode selection
    pinMode(S2,OUTPUT);
    pinMode(S3,OUTPUT);
    
    //communication freq (sensitivity) selection
    pinMode(S0,OUTPUT);
    pinMode(S1,OUTPUT);
    
    //color response pin (only actual input from taos)
    pinMode(taosOutPin, INPUT); //taosOutPin pinC
    
    return;
    
}

TCS3200::~TCS3200() {
    
}

//private

void TCS3200::taosMode(int mode){
    
    if(mode == POWER_OFF_MODE){
        //power OFF mode-  LED off and both channels "low"
        digitalWrite(LED, LOW);
        digitalWrite(S0, LOW); //S0
        digitalWrite(S1, LOW); //S1
        //  Serial.println("mOFFm");
        
    }else if(mode == HIGH_SENSE_MODE){
        //this will put in 1:1, highest sensitivity
        digitalWrite(S0, HIGH); //S0
        digitalWrite(S1, HIGH); //S1
        // Serial.println("m1:1m");
        
    }else if(mode == MID_SENSE_MODE){
        //this will put in 1:5, 20% highest sensitivity
        digitalWrite(S0, HIGH); //S0
        digitalWrite(S1, LOW); //S1
        //Serial.println("m1:5m");
        
    }else if(mode == LOW_SENSE_MODE){
        //this will put in 1:50, 2% highest sensitivity
        digitalWrite(S0, LOW); //S0
        digitalWrite(S1, HIGH); //S1
        //Serial.println("m1:50m");
    }
    
    return;
    
}

//public

/*
 This section will return the pulseIn reading of the selected color.
 It will turn on the sensor at the start taosMode(1), and it will power off the sensor at the end taosMode(0)
 color codes: 0=white, 1=red, 2=blue, 3=green
 if LEDstate is 0, LED will be off. 1 and the LED will be on.
 taosOutPin is the ouput pin of the TCS3200.
 */

unsigned long TCS3200::colorRead(int color, int LEDstate)
{
    //turn on sensor and use highest frequency/sensitivity setting
    taosMode(HIGH_SENSE_MODE);
    
    //setting for a delay to let the sensor sit for a moment before taking a reading.
    int sensorDelay = 100;
    
    //set the S2 and S3 pins to select the color to be sensed
    if(color == COLOR_WHITE)  //white
    {
        digitalWrite(S3, LOW); //S3
        digitalWrite(S2, HIGH); //S2
        // Serial.print(" w");
    }
    else if(color == COLOR_RED) //red
    {
        digitalWrite(S3, LOW); //S3
        digitalWrite(S2, LOW); //S2
        // Serial.print(" r");
    }
    else if(color == COLOR_BLUE) //blue
    {
        digitalWrite(S3, HIGH); //S3
        digitalWrite(S2, LOW); //S2
        // Serial.print(" b");
    }
    else if(color == COLOR_GREEN) //green
    {
        digitalWrite(S3, HIGH); //S3
        digitalWrite(S2, HIGH); //S2
        // Serial.print(" g");
    }
      
    //  turn LEDs on or off, as directed by the LEDstate var (IKKE Frydenlund)
    if(LEDstate == LED_OFF){
        digitalWrite(LED, LOW);
    }
    if(LEDstate == LED_ON){
        digitalWrite(LED, HIGH);
    }
    
    // wait a bit for LEDs to actually turn on, as directed by sensorDelay var
    delay(sensorDelay);
    
    // create a var where the pulse reading from sensor will go
    // now take a measurement from the sensor, timing a low pulse on the sensor's "out" pin
    unsigned long readPulse = pulseIn(taosOutPin, LOW, 80000);
    
    //if the pulseIn times out, it returns 0 and that throws off numbers. just cap it at 80k if it happens
    if(readPulse < .1){
        readPulse = 80000;
    }
    
    //turn off color sensor and LEDs to save power
    taosMode(POWER_OFF_MODE);
    
    // return the pulse value back to whatever called for it...
    return readPulse;
    
}

int TCS3200::detectColor()
{
    Serial.print("RGB:");
    Serial.print(colorRead(COLOR_RED,LED_ON));
    Serial.print(",");
    Serial.print(colorRead(COLOR_GREEN,LED_ON));
    Serial.print(",");
    Serial.println(colorRead(COLOR_BLUE,LED_ON));
    
    
    /*
    String redValueInString = String(colorRead(COLOR_RED,LED_ON));
    String blueValueInString = String(colorRead(COLOR_BLUE,LED_ON));
    String greenValueInString = String(colorRead(COLOR_GREEN,LED_ON));
    Serial.println("RGB:" + redValueInString + "," + greenValueInString + "," + blueValueInString);
    */
}
