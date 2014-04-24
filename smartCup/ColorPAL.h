#ifndef COLORPAL_h
#define COLORPAL_h

#include <Arduino.h>
#include <SoftwareSerial.h>

class ColorPAL {

private:
    int sio;
    
    SoftwareSerial *serin;
    SoftwareSerial *serout;
    
    // Parse the hex data into integers
    void parseAndPrint(char * data);
    
public:
    ColorPAL(int p_sio,int p_waitDelay); //sio is ColorPAL signal pin
    virtual ~ColorPAL();
    void readData();


};
#endif /* COLORPAL_h */
