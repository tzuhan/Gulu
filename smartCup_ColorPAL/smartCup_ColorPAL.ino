#include <Arduino.h>
#include <SoftwareSerial.h>

const long bluetoothBaudRate = 57600;

const int triggerPin = 11;

const int sio = 10;
const int unused = 255; // Non-existant pin # for SoftwareSerial
const int sioBaud = 4800;
const int waitDelay = 100;
const int colorSampleNum = 15;
int sampleTurn = colorSampleNum;

SoftwareSerial serin(sio, unused);
SoftwareSerial serout(unused, sio);

void setup() {
  Serial.begin(bluetoothBaudRate);
  //Serial.begin(9600);
  
  // open reading == HIGH
  // closed reading == LOW
  pinMode(triggerPin, INPUT_PULLUP);
  
  reset();				  // Send reset to ColorPal
  serout.begin(sioBaud);
  pinMode(sio, OUTPUT);
  serout.print("= (00 $ m) !"); // Loop print values, see ColorPAL documentation
  serout.end();			  // Discontinue serial port for transmitting

  serin.begin(sioBaud);	        // Set up serial port for receiving
  pinMode(sio, INPUT); 
  
}

int previousWeight = 0;
int currentWeight = 0;
const int WeightNoise = 5;

void loop() {
  if(digitalRead(triggerPin) == LOW) { //closed circuit
    
    readDataAndSample();
    
  }
}

int red[colorSampleNum] = {0};
int green[colorSampleNum] = {0};
int blue[colorSampleNum] = {0};

void readDataAndSample() {
  sampleTurn = colorSampleNum;
  while(sampleTurn){
    readData();
  }
  
  Serial.print(getAverage(red));
  Serial.print(" ");
  Serial.print(getAverage(green));
  Serial.print(" ");
  Serial.print(getAverage(blue));
  Serial.print(" ");
  Serial.println("0");
  
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
