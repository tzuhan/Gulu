const int analogInPin = A0; 
const int analogOutPin = 3;

int sensorValue = 0;        
int outputValue = 0;        

void clearAndHome() 
{ 
Serial.write(27); 
Serial.print("[2J"); // clear screen 
Serial.write(27); // ESC 
Serial.print("[H"); // cursor to home 
} 

void setup() {
  Serial.begin(9600); 
  pinMode(analogOutPin, OUTPUT); 
}

int maxValue = -1;
int minValue = 100000;
int num = 0;
long long sum = 0;
int printFlag = 0;
int sampleNum = 5000;

void loop() {
  sensorValue = analogRead(analogInPin);            

  while(Serial.available() > 0){
    int power = Serial.parseInt();
    //Serial.println(power);
    //maxValue = -1;
    //minValue = 100000;
    num = 0;
    sum = 0;
    printFlag = 1;
    analogWrite(analogOutPin, power);
    //clearAndHome();
  }
  /*
  if(sensorValue > maxValue){
    maxValue = sensorValue;
    //Serial.println("max:"); 
    Serial.println(maxValue);  
  }
  if(sensorValue < minValue){
    minValue = sensorValue;
    //Serial.println("min:"); 
    Serial.println(minValue); 
  }
  */
  if(num == 0){
    delay(10); 
  }
  
  if(num < sampleNum){
    sum+=sensorValue;
    num++; 
  }
  if(num == sampleNum && printFlag){
   Serial.println(sum/((float)sampleNum));
   printFlag = 0; 
  }
  //Serial.println(sensorValue); 
  delay(2);  
}

//  outputValue = map(sensorValue, 0, 1023, 0, 255);  
  // change the analog out value:
//  analogWrite(analogOutPin, outputValue);           

  // print the results to the serial monitor:
//  Serial.print("sensor = " );                       
//  Serial.println(sensorValue);      
//  Serial.print("\t output = ");      
//  Serial.println(outputValue);   

 
                    

