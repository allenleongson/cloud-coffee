#include <LinkedList.h>
#include "CoffeeOrder.h"
#include <ArduinoJson.h>
#include <Time.h>
#include <UIPUdp.h>
#include <UIPEthernet.h>
#include "Operators.h"
#include "CoffeeMakerHardware.h"
#include "CloudCoffeeMaker.h"

uint8_t macAddress[] = { 00, 11, 22, 33, 44, 57 }; //mac address of Ethernet Shield
char apiKey[] = "08r1JQ206c7qJkVrw7aSN4hHoXeLjUQ2jYzcPQQ4WCoYksqO";
char feedId[] = "1931973311";
CloudCoffeeMaker cloudCoffeeMaker(macAddress, feedId, apiKey);

void setup() {
	cloudCoffeeMaker.begin();
}

void loop() {
	cloudCoffeeMaker.maintain();
}
