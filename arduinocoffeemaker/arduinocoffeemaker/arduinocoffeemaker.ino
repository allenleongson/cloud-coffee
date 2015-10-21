#include <Time.h>
#include <UIPUdp.h>
#include <UIPEthernet.h>
#include "Operators.h"
#include "CoffeeMakerHardware.h"
#include "CloudCoffeeMaker.h"

uint8_t macAddress[] = { 00, 11, 22, 33, 44, 55 }; //mac address of Ethernet Shield
CloudCoffeeMaker cloudCoffeeMaker(macAddress);

void setup() {
	cloudCoffeeMaker.begin();
}

void loop() {
	cloudCoffeeMaker.maintain();
}
