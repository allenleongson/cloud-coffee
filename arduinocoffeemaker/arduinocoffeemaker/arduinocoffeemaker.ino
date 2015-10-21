#include "CoffeeMakerHardware.h"
#include "CloudCoffeeMaker.h"

CloudCoffeeMaker cloudCoffeeMaker;

void setup() {
	cloudCoffeeMaker.begin();
}

void loop() {
	cloudCoffeeMaker.maintain();
}
