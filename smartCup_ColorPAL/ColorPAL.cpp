#include "ColorPAL.h"

const int unused = 255; 		// Non-existant pin # for SoftwareSerial
const int sioBaud = 4800;

ColorPAL::ColorPAL(int p_sio,int p_waitDelay) {
    sio = p_sio;
    
    delay(200);
    pinMode(sio, OUTPUT);
    digitalWrite(sio, LOW);
    pinMode(sio, INPUT);
    while (digitalRead(sio) != HIGH);
    pinMode(sio, OUTPUT);
    digitalWrite(sio, LOW);
    delay(80);
    pinMode(sio, INPUT);
    delay(p_waitDelay);
    
    serin = new SoftwareSerial(sio, unused);
    serout = new SoftwareSerial(unused, sio);
    
    serout->begin(sioBaud);
    pinMode(sio, OUTPUT);
    serout->print("= (00 $ m) !"); // Loop print values, see ColorPAL documentation
    serout->end();			  // Discontinue serial port for transmitting
    pinMode(sio, INPUT);
    serin->begin(sioBaud);	        // Set up serial port for receiving
    
    //Serial.println("initial ColorPAL done");
}

ColorPAL::~ColorPAL() {
    delete serin;
    delete serout;
}

void ColorPAL::readData() {
    char buffer[32];
    if (serin->available() > 0) {
        // Wait for a $ character, then read three 3 digit hex numbers
        buffer[0] = serin->read();
        if (buffer[0] == '$') {
            for(int i = 0; i < 9; i++) {
                while (serin->available() == 0);     // Wait for next input character
                buffer[i] = serin->read();
                if (buffer[i] == '$')               // Return early if $ character encountered
                    return;
            }
            parseAndPrint(buffer);
            delay(10);
        }
    }
}

// Parse the hex data into integers
void ColorPAL::parseAndPrint(char * data) {
    int rgb[3] = {0};
    sscanf (data, "%3x%3x%3x", rgb, rgb+1, rgb+2);
    char buffer[32];
    sprintf(buffer, "RGB:%d %d %d", rgb[0], rgb[1], rgb[2]);
    Serial.println(buffer);
}
