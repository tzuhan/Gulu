const int lightPin = A6;
const int forcePin = A5;
const int redPin = 5;
const int bluePin = 9;
const int greenPin =6;
const int whitePin = 10;
const int togglePin = 13;
const char colorList[] = {'R', 'G', 'B', 'W'};
const int tensityLest[] = {30,255};
const int colorNum = 4;
const int tensityNum = 2;


const int sample = 50;

int lightValue = 0;
int forceValue = 0;

int counter = 0;
int sum = 0;
int flag = 0;
int index = 0;
int tensity = 0;

int result[ colorNum*tensityNum + 1 ] = {0};
int resultLength = colorNum*tensityNum+1;

void setup() {
  //NORMAL
  //Serial.begin(9600);
  //BLUETOOTH
  Serial.begin(57600);
  pinMode(togglePin, INPUT);
  pinMode(redPin, OUTPUT);
  pinMode(bluePin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(whitePin, OUTPUT);
}

void loop() {
  int toggle = digitalRead(togglePin);
//  Serial.print("switch ");
//  Serial.print( toggle );
  if( toggle == HIGH ){
    if( index < colorNum ){
      if( tensity < tensityNum ){
//        Serial.print("counter ");
//        Serial.println(counter);
        if( counter < sample ){
          if( counter == 0 ){
            turnLED( colorList[index], tensityLest[tensity]);
          }
          lightValue = analogRead(lightPin);
          sum+=lightValue;
          counter++;
        }else{
          result[ index*tensityNum + tensity ] = sum/(float)sample;
          counter = 0;
          sum = 0;
          tensity++;
        }
        
      }else{
        tensity=0;
        index++;
      }
    }else if( index == colorNum ){
 
      if( counter < sample ){
        forceValue = analogRead(forcePin);  
        sum+=forceValue;
        counter++;
        
      }else{
        result[resultLength - 1] = sum/(float)sample;
        counter = 0;
        sum = 0;
        index++;
      }
      
    }else{
      for(int i = 0; i < resultLength; i++ ){
       Serial.println( result[i] );
      }
      Serial.println( "----" );
      index = 0;
    }
    
  }else{
    reset();
  }
  
      
  
  delay(10);                     
}

void turnLED(char color, int strength){
  if( color == 'R' ){
    analogWrite(redPin, 255-strength);
    analogWrite(greenPin, 255-0);
    analogWrite(bluePin, 255-0);
    analogWrite(whitePin, 0);
  }else if( color == 'G' ){
    analogWrite(redPin, 255-0);
    analogWrite(greenPin, 255-strength);
    analogWrite(bluePin, 255-0);
    analogWrite(whitePin, 0);
  }else if( color == 'B' ){
    analogWrite(redPin, 255-0);
    analogWrite(greenPin, 255-0);
    analogWrite(bluePin, 255-strength);
    analogWrite(whitePin, 0);
  }else if( color == 'W'){
    analogWrite(redPin, 255-0);
    analogWrite(greenPin, 255-0);
    analogWrite(bluePin, 255-0);
    analogWrite(whitePin, strength);
  }
}

void reset(){
  turnLED('R', 0);
  for(int i = 0; i < resultLength; i++ ){
    result[i] = 0;
  }
  
  index = 0;
  counter = 0;
  tensity = 0;
  sum = 0;
}
