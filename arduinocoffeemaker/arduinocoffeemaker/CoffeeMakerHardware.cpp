#include "CoffeeMakerHardware.h"

CoffeeMakerHardware::CoffeeMakerHardware() {

}

void CoffeeMakerHardware::begin() {
	Serial.begin(9600);
	Serial.println("Serial Begin.");
}

void CoffeeMakerHardware::maintain() {
	Serial.println("TEST");
	delay(1000);
}

CoffeeMakerHardware::~CoffeeMakerHardware() {

}