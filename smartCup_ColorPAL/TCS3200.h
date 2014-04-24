#if ARDUINO >= 100
#include "Arduino.h"
#else
#include "WProgram.h"
#endif

#ifndef TCS3200_h
#define TCS3200_h

#define POWER_OFF_MODE 0
#define LOW_SENSE_MODE 3
#define MID_SENSE_MODE 2
#define HIGH_SENSE_MODE 1

#define COLOR_WHITE 0
#define COLOR_RED 1
#define COLOR_GREEN 3
#define COLOR_BLUE 2

#define LED_ON 1
#define LED_OFF 0

// Operation modes area, controlled by hi/lo settings on S0 and S1 pins.
//setting mode to zero will put taos into low power mode. taosMode(0);

class TCS3200 {
    private:
        int S0;
        int S1;
        int S2;
        int S3;
        int taosOutPin;
        int LED;
    
        void taosMode(int mode);
    
    public:
        TCS3200
        (int p_SO,int p_S1,int p_S2,int p_S3,
         int p_taosOutPin,int p_LED);
    
        virtual ~TCS3200();
        unsigned long colorRead(int color, int LEDstate);
        int detectColor();
    
};


#endif /* TCS3200_h */
