int sortValues[13] = { 2, 7, 4, 6, 5, 3, 8, 10, 9, 11, 14, 12, 13 }; 
 
void setup(){
  Serial.begin(9600);
}
 
void loop() {
  int mid = middle(sortValues,13);
  Serial.println(mid);
  //Serial.print("Sorted Array: ");
  for(int i=0; i<13; i++) {
     Serial.print(sortValues[i]); 
     Serial.print(",");
  }
  Serial.println("");
  delay(1000);
}

int middle(int a[],int aSize) {
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
