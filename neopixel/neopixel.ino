#include <Adafruit_NeoPixel.h>
#include "EEPROM.h"
#include <CurieBLE.h>
#ifdef __AVR__
  #include <avr/power.h>
#endif

#define PIN 6

#define BRIGHTNESS 100

// Parameter 1 = number of pixels in strip
// Parameter 2 = Arduino pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
//   NEO_RGBW    Pixels are wired for RGBW bitstream (NeoPixel RGBW products)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(12, PIN, NEO_GRB + NEO_KHZ800);

// IMPORTANT: To reduce NeoPixel burnout risk, add 1000 uF capacitor across
// pixel power leads, add 300 - 500 Ohm resistor on first pixel's data input
// and minimize distance between Arduino and first pixel.  Avoid connecting
// on a live circuit...if you must, connect GND first.

BLEPeripheral blePeripheral; // create peripheral instance
BLEService uartService("19B1001-E8F2-537E-4F6C-D104768A1214"); // create service
BLECharacteristic RxChar("19B1001-E8F2-537E-4F6C-D104768A1210", BLEWrite, 20);
BLEUnsignedIntCharacteristic TxChar("19B1001-E8F2-537E-4F6C-D104768A1211", BLERead | BLENotify);

char nombreFc = 0;
int Fonction[256][5] = {
      {1, 255, 0, 0, 50},
      {1, 0, 255, 0, 50},
      {1, 0, 0, 255, 50},
      {2, 127, 127, 127, 50},
      {3, 0, 0, 0, 10}
    };
int RGBColor[3] = {0,0,0};
int inc = 0;
int inccolor =0;
bool ColorChoosen = false;
bool startEdit = false;
bool waitnextValue = false;
bool subscribe = false;

void setup() {
  Serial.begin(115200);
  readData();
  record();
  pinMode(13, OUTPUT); // use the LED on pin 13 as an output

  // set the local name peripheral advertises
  blePeripheral.setLocalName("LEDCB");
  blePeripheral.setDeviceName("NeoPixel");
  // set the UUID for the service this peripheral advertises
  blePeripheral.setAdvertisedServiceUuid(uartService.uuid());

  // add service and characteristic
  blePeripheral.addAttribute(uartService);
  blePeripheral.addAttribute(RxChar);
  blePeripheral.addAttribute(TxChar);

  TxChar.setValue(0);

  // assign event handlers for connected, disconnected to peripheral
  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  // assign event handlers for characteristic
  RxChar.setEventHandler(BLEWritten, switchCharacteristicWritten);
  TxChar.setEventHandler(BLESubscribed, txSub);
  TxChar.setEventHandler(BLEUnsubscribed, txUnsub);

  // advertise the service
  blePeripheral.begin();
  // This is for Trinket 5V 16MHz, you can remove these three lines if you are not using a Trinket
  #if defined (__AVR_ATtiny85__)
    if (F_CPU == 16000000) clock_prescale_set(clock_div_1);
  #endif
  // End of trinket special code
  strip.setBrightness(BRIGHTNESS);
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
}

void loop() {
  if(subscribe) {
    sendData();
    subscribe = false;
  }
  if (ColorChoosen) {
    colorWipe(strip.Color(RGBColor[0], RGBColor[1], RGBColor[2]), 50);
  } else {
    for(int i=0; i<256; i++) {
      if (ColorChoosen) {
        break;
      }
      if (Fonction[i][0] == 0) {
        break;
      } else {
        execute(Fonction[i][0], Fonction[i][1], Fonction[i][2], Fonction[i][3], Fonction[i][4]);
      }
    }
  }
}

void execute(int toExecute, int red, int green, int blue, int delai) {
  switch(toExecute) {
    case 1:
      colorWipe(strip.Color(red, green, blue), delai); // Red
      break;
    case 2:
      theaterChase(strip.Color(red, green, blue), delai); // White
      break;
    case 3:
      rainbowCycle(delai);
      break;
    default:
      break;
  }
}

void readData() {
  nombreFc = EEPROM.read(0);
  if (nombreFc == 0) {
    //If never write
    return; //We don't overwrite Fonction's array to keep some functions at the first lunch
  } else {
    for(int i=0; i<256; i++) {
      Fonction[i][0] = 0;
    }
  }
  inc = 1;
  for(int i=0; i<nombreFc; i++) {
    if (inc <= EEPROM.length()) {
      Fonction[i][0] = EEPROM.read(inc++);
      Serial.println(Fonction[i][0]);
      Fonction[i][1] = EEPROM.read(inc++);
      Serial.println(Fonction[i][1]);
      Fonction[i][2] = EEPROM.read(inc++);
      Serial.println(Fonction[i][2]);
      Fonction[i][3] = EEPROM.read(inc++);
      Serial.println(Fonction[i][3]);
      Fonction[i][4] = EEPROM.read(inc++);
      Serial.println(Fonction[i][4]);
    } else {
      //ERROR
    }
  }
  inc = 0;
}

void record() {
  inc = 1;
  for(int i=0; i<nombreFc; i++) {
    if (Fonction[i][0] != 0) {
      if (inc+5 <= EEPROM.length()) {
        EEPROM.update(inc++, Fonction[i][0]);
        EEPROM.update(inc++, Fonction[i][1]);
        EEPROM.update(inc++, Fonction[i][2]);
        EEPROM.update(inc++, Fonction[i][3]);
        EEPROM.update(inc++, Fonction[i][4]);
      } else {
        //ERROR
      }
    } else {
      EEPROM.update(0, i);
      break;
    }
  }
  inc = 0;
}

// Fill the dots one after the other with a color
void colorWipe(uint32_t c, uint8_t wait) {
  for(uint16_t i=0; i<strip.numPixels(); i++) {
    strip.setPixelColor(i, c);
    strip.show();
    delay(wait);
  }
}

void rainbow(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel((i+j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void rainbowCycle(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256*5; j++) { // 5 cycles of all colors on wheel
    for(i=0; i< strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

//Theatre-style crawling lights.
void theaterChase(uint32_t c, uint8_t wait) {
  for (int j=0; j<10; j++) {  //do 10 cycles of chasing
    for (int q=0; q < 3; q++) {
      for (uint16_t i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, c);    //turn every third pixel on
      }
      strip.show();

      delay(wait);

      for (uint16_t i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, 0);        //turn every third pixel off
      }
    }
  }
}

//Theatre-style crawling lights with rainbow effect
void theaterChaseRainbow(uint8_t wait) {
  for (int j=0; j < 256; j++) {     // cycle all 256 colors in the wheel
    for (int q=0; q < 3; q++) {
      for (uint16_t i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, Wheel( (i+j) % 255));    //turn every third pixel on
      }
      strip.show();

      delay(wait);

      for (uint16_t i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, 0);        //turn every third pixel off
      }
    }
  }
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  WheelPos = 255 - WheelPos;
  if(WheelPos < 85) {
    return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  }
  if(WheelPos < 170) {
    WheelPos -= 85;
    return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
  WheelPos -= 170;
  return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
}

void blePeripheralConnectHandler(BLECentral& central) {
  // central connected event handler
  digitalWrite(13, HIGH);
}

void blePeripheralDisconnectHandler(BLECentral& central) {
  // central disconnected event handler
  digitalWrite(13, LOW);
}

String toString(const unsigned char* entry, unsigned char len) {
  char Temp[len];
  for (int i=0;i<len;i++) {
    Temp[i] = char(entry[i]);
  }
  String Out = Temp;
  Out.remove(len);
  return Out;
}

const unsigned char* tocstChar(String entry) {
  unsigned int len = entry.length();
  unsigned char Temp[len];
  for (int i=0;i<len;i++) {
    Temp[i] = entry.charAt(i);
  }
  const unsigned char* Out = Temp;
  return Out;
}

void switchCharacteristicWritten(BLECentral& central, BLECharacteristic& characteristic) {
  // central wrote new value to characteristic, update LED
  String In = toString(RxChar.value(), RxChar.valueLength());
  Serial.println(In);
  if(In == "Start") {
    startEdit = true;
    inc = 0;
    inccolor = 0;
    for(int i=0; i<256; i++) {
      Fonction[i][0] = 0;
    }
  } else if (In == "End") {
    startEdit = false;
    ColorChoosen = false;
    nombreFc = inc;
    record();
  } else if (startEdit) {
    Fonction[inc][inccolor] = In.toInt();
    Serial.print("inc : ");
    Serial.println(inc);
    Serial.print("inccolor : ");
    Serial.println(inccolor);
    Serial.print("Fonction : ");
    Serial.println(Fonction[inc][inccolor]);
    inccolor++;
    if(inccolor == 5) {
      inc++;
      inccolor = 0;
    }
  } else if (In == "StartColor") {
    inc = 0;
  } else if (In == "Brightness") {
    waitnextValue = true;
  } else if (waitnextValue) {
    strip.setBrightness(In.toInt());
    waitnextValue = false;
  } else {
    if (inc < 3) {
      RGBColor[inc] = In.toInt();
    }
    inc++;
    if (inc == 4) {
      strip.setBrightness(In.toInt());
      ColorChoosen = true;
    }
  }
}

void sendData() {
  TxChar.setValue(0); //Send some protocol to detect that Arduino will send datas
  TxChar.setValue(1);
  TxChar.setValue(0);
  TxChar.setValue(0);
  TxChar.setValue(int(nombreFc));
  for(int i=0; i<nombreFc; i++) {
    if (Fonction[i][0] != 0) {
      Serial.print("Send data of function : ");
      Serial.println(i);
      TxChar.setValue(Fonction[i][0]);
      Serial.println(Fonction[i][0]);
      TxChar.setValue(Fonction[i][1]);
      TxChar.setValue(Fonction[i][2]);
      TxChar.setValue(Fonction[i][3]);
      TxChar.setValue(Fonction[i][4]);
    } else {
      //ERROR function is programmed but undefined
    }
  }
}

void txSub(BLECentral& central, BLECharacteristic& characteristic) {
  subscribe = true;
}

void txUnsub(BLECentral& central, BLECharacteristic& characteristic) {
  subscribe = false;
}

