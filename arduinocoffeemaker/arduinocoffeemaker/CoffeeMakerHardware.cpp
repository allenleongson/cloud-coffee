#include "CoffeeMakerHardware.h"

CoffeeMakerHardware::CoffeeMakerHardware() {

}

void CoffeeMakerHardware::begin() {
	Serial.begin(9600);
	Serial.println("Serial Begin.");
}

void CoffeeMakerHardware::maintain() {

}

CoffeeMakerHardware::~CoffeeMakerHardware() {

}