#include <Arduino.h>
#include <SoftwareSerial.h>

const long bluetoothBaudRate = 57600;

const int triggerPin = 11;

//color sensor related
const int sio = 10;
const int unused = 255; // Non-existant pin # for SoftwareSerial
const int sioBaud = 4800;
const int waitDelay = 100;
const int colorSampleNum = 15;
int sampleTurn = colorSampleNum;

SoftwareSerial serin(sio, unused);
SoftwareSerial serout(unused, sio);

//ultrasonic sensor related
const int us_trigPin = 9;
const int us_echoPin = 8;
unsigned long Time_Echo_us = 0;
unsigned long Len_mm  = 0;

void setup() {
  Serial.begin(bluetoothBaudRate);
  //Serial.begin(9600);
  
  // open reading == HIGH
  // closed reading == LOW
  pinMode(triggerPin, INPUT_PULLUP);
  
  //color sensor related settings
  reset();				  // Send reset to ColorPal
  serout.begin(sioBaud);
  pinMode(sio, OUTPUT);
  serout.print("= (00 $ m) !"); // Loop print values, see ColorPAL documentation
  serout.end();			  // Discontinue serial port for transmitting

  serin.begin(sioBaud);	        // Set up serial port for receiving
  pinMode(sio, INPUT); 
  
  //ultrasonic PWM mode
  pinMode(us_echoPin, INPUT);                    //Set EchoPin as input, to receive measure result from US-100
  pinMode(us_trigPin, OUTPUT);                   //Set TrigPin as output, used to send high pusle to trig measurement (>10us)
}

void loop() {
  if(digitalRead(triggerPin) == LOW) { //closed circuit
    readColorDataAndSample();
  }
}

int red[colorSampleNum] = {0};
int green[colorSampleNum] = {0};
int blue[colorSampleNum] = {0};

void readColorDataAndSample() {
  //color data
  sampleTurn = colorSampleNum;
  while(sampleTurn){
    readData();
  }
  
  //ultrasonic data
  while(true) {  
    digitalWrite(us_trigPin, HIGH);              //begin to send a high pulse, then US-100 begin to measure the distance
    delayMicroseconds(50);                    //set this high pulse width as 50us (>10us)
    digitalWrite(us_trigPin, LOW);               //end this high pulse
    Time_Echo_us = pulseIn(us_echoPin, HIGH);               //calculate the pulse width at EchoPin, 
    if((Time_Echo_us < 60000) && (Time_Echo_us > 1))     //a valid pulse width should be between (1, 60000).
    {
      //distance units: mm
      Len_mm = (Time_Echo_us*34/100)/2;      //calculate the distance by pulse width, Len_mm = (Time_Echo_us * 0.34mm/us) / 2 (mm)  
      break;
    }
  }
  
  Serial.print(getAverage(red));
  Serial.print(" ");
  Serial.print(getAverage(green));
  Serial.print(" ");
  Serial.print(getAverage(blue));
  Serial.print(" ");
  Serial.println(Len_mm);
  
}

float getAverage(int *values) {
  long sum = 0;
  for(int i=0;i < colorSampleNum;i++) {
    sum+=values[i];
  }
  return ((float)sum)/colorSampleNum;
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
  char buffer[32] = {0};
  
  if (serin.available() > 0) {
    // Wait for a $ character, then read three 3 digit hex numbers
    if (serin.read() == '$') {
      for(int i = 0; i < 9; i++) {
        while (serin.available() == 0);     // Wait for next input character
        buffer[i] = serin.read();
        if (buffer[i] == '$')               // Return early if $ character encountered
          return;
      }
      parseAndCollectData(buffer);
      delay(10);
    }
  }
}

void parseAndCollectData(char *data) {
  sampleTurn--;
  sscanf (data, "%3x%3x%3x"
  , red + sampleTurn
  , green + sampleTurn
  , blue + sampleTurn);
  
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
