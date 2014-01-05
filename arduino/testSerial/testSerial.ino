const int red = 5;
const int blue = 9;
const int green = 6;
const int forcePin = A4;
int forceValue = 0;

void setup() {
  Serial.begin(9600);  


}

void loop(){
//  forceValue = analogRead(forcePin) % 1024;
//  Serial.println(forceValue);
  analogWrite(red, 0);
  delay(100);
  analogWrite(red, 255);
  analogWrite(blue, 0);
  delay(100);
  analogWrite(blue, 255);
//    analogWrite(blue, 0);
//  analogWrite(green, 0);
//  delay(100);
//  analogWrite(red, 200);
//  delay(100);
//  analogWrite(blue, 200);
//  delay(100);
//  analogWrite(green, 200);
//  delay(100);
  
//  int switchStatus = digitalRead(2);
//  if(switchStatus == HIGH)
//    Serial.println("ON");
//  else
//    Serial.println("OFF");
  
  delay(2);
}
