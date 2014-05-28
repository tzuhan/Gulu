#include <Ultrasonic.h>

#include <Arduino.h>
#include <SoftwareSerial.h>

const long bluetoothBaudRate = 57600;

const int triggerPin = 7; //digital

//color sensor related
const int sio = 10; //digital
const int unused = 255; // Non-existant pin # for SoftwareSerial
const int sioBaud = 4800;
const int waitDelay = 100;

const int colorSampleNum = 30;
int sampleTurn = colorSampleNum;

SoftwareSerial serin(sio, unused);
SoftwareSerial serout(unused, sio);

//ultrasonic sensor related
const int us_trigPin = 9; //digital
const int us_echoPin = 8; //digital
const float emptyDistance = 12.60; //units:cm
const float noise = 1; //units:cm

//HC-SR04
Ultrasonic ultrasonic(us_trigPin, us_echoPin);

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
  
}

void loop() {
  if(digitalRead(triggerPin) == LOW) { //closed circuit
    readDataAndSample();
  }
}

int red[colorSampleNum] = {0};
int green[colorSampleNum] = {0};
int blue[colorSampleNum] = {0};

float pre_len_cm = -1;

void readDataAndSample() {
  //color data
  sampleTurn = colorSampleNum;
  while(sampleTurn){
    readData();
  }
  
  unsigned long microsec = ultrasonic.timing();
  float cmMsec = ultrasonic.convert(microsec, Ultrasonic::CM);
  
  //Serial.println(cmMsec);
  if(cmMsec > 0 && cmMsec < 13){ //make sure distance has been measured
    //Serial.print(getAverage(red));
    Serial.print(getMiddleValue(red,colorSampleNum));
    Serial.print(" ");
    Serial.print(getMiddleValue(green,colorSampleNum));
    Serial.print(" ");
    Serial.print(getMiddleValue(blue,colorSampleNum));
    Serial.print(" ");
    
    if(fabs(cmMsec - emptyDistance) < 0.6) { //empty
      Serial.println(-1); // -1 means empty
      pre_len_cm = -1; //reset
    }
    else if(pre_len_cm < 0 || pre_len_cm < cmMsec) {
      Serial.println(cmMsec);
      pre_len_cm = cmMsec;
    }
    else { //pre_len_cm >= cmMsec, treat it as noise and print the same number
      Serial.println(pre_len_cm);//
    }
  }
  
}

int getMiddleValue(int a[],int aSize) {
    //insertion sort
    for(int i = 1;i < aSize; i++) {
      int comparedOne = a[i];
      int j;
      for(j = i-1;j >= 0 && a[j] > comparedOne;j--) {
        a[j+1] = a[j];
      }
      a[j+1] = comparedOne;
    }
    if(aSize%2 == 0)
      return ( a[aSize/2] + a[aSize/2-1] )/2;
    else
      return a[aSize/2];
}

float getAverage(int *values) {
  unsigned long sum = 0;
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

//read one sample
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
      parseData(buffer);
      //delay(10);
    }
  }
}

void parseData(char *data) {
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
