#include "CoffeeMakerHardware.h"

CoffeeMakerHardware::CoffeeMakerHardware() : _errorCode(None) {
	for (int i = 0; i < 3; i++) {
		_traySlotStatus[i] = Available;
	}
}

void CoffeeMakerHardware::begin() {
	Serial.begin(9600);
	Serial.println("Serial Begin.");
	//setup input and output pins here.
}

void CoffeeMakerHardware::maintain() {
	//set ingredients remaining here. read sensors to check remaining ingredients. CONVERT VALUE TO TEASPOON measurement.
	//example
	//int coffeeTsp = analogread(A0) / 20; *if /20 is the conversion factor from reading to teaspoon.
	
	int coffeeTsp = 20;
	int creamTsp = 20;
	int sugarTsp = 20;
	int waterCup = 20;

	setIngredientsRemaining(coffeeTsp, creamTsp, sugarTsp, waterCup);

	//set tray status here. read sensors to check tray sensors.
	for (int i = 0; i < 3; i++) {
		setTrayStatus(Available, i);
	}

	//set error code here. implement your logic here.
	if (!false) {
		setCoffeeMakerErrorCode(None);
	}
}

//getters
int CoffeeMakerHardware::getCoffeeTspRemaining() { return _coffeeTsp; }
int CoffeeMakerHardware::getCreamTspRemaining() { return _creamTsp; }
int CoffeeMakerHardware::getSugarTspRemaining() { return _sugarTsp; }
int CoffeeMakerHardware::getWaterCupRemaining() { return _waterCup; }

//setters
void CoffeeMakerHardware::setIngredientsRemaining(int coffeeTsp, int creamTsp, int sugarTsp, int waterCup) {
	_coffeeTsp = coffeeTsp;
	_creamTsp = creamTsp;
	_sugarTsp = sugarTsp;
	_waterCup = waterCup;
}

void CoffeeMakerHardware::setTrayStatus(CoffeeMakerHardware::CoffeeMakerTraySlotStatus trayStatus, int slot) {
	_traySlotStatus[slot] = trayStatus;
}

void CoffeeMakerHardware::setCoffeeMakerErrorCode(CoffeeMakerHardware::CoffeeMakerErrorCode errorCode) {
	_errorCode = errorCode;
}

CoffeeMakerHardware::~CoffeeMakerHardware() {

}