#include "CoffeeMakerHardware.h"

CoffeeMakerHardware::CoffeeMakerHardware() : _errorCode(None), _coffeeMakerStatus(Available) {
	for (int i = 0; i < 3; i++) {
		setTrayStatus(Vacant, i);
	}
}

void CoffeeMakerHardware::begin() {
	Serial.begin(9600);
	Serial.println("Serial Begin.");
	//setup input and output pins here.

	//get initial ingredient status
	setIngredientsRemaining(20, 20, 20, 20);
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

	//check here if coffee maker is finished preparing.
	if (_coffeeMakerStatus == Preparing) {
		if (!false) {
			setCoffeeMakerStatus(Finished);
		}
	}

	//set tray status here. read sensors to check tray sensors.
	for (int i = 0; i < 3; i++) {
		if (true) {
			setTrayStatus(Vacant, i);
		}
		else {
			setTrayStatus(Occupied, i);
		}
	}

	//set error code here. implement your logic here.
	if (!false) {
		setCoffeeMakerErrorCode(None);
	}

	//check if work needs to be done.
	if (_coffeeMakerStatus == Available) {
		if (coffeeOrderList.size() > 0) {
			CoffeeOrder c = coffeeOrderList.get(0);
			_prepareCoffee(c);
		}
		//else no work will be done.
	}
}

void CoffeeMakerHardware::_prepareCoffee(const CoffeeOrder & order) {
	Serial.print("PREPARING: ");
	Serial.print(order.username);
	Serial.print(order.coffeeTsp);
	Serial.print(order.sugarTsp);
	Serial.print(order.creamTsp);

	_coffeeMakerStatus = Preparing;
}

void CoffeeMakerHardware::setCoffeeMakerToAvailableSlot() {
	//this function is called whenever coffeemaker is finished preparing coffee and ready to prepare next.

	_coffeeMakerStatus = Available;
}


//INTERNALS. DO NOT MODIFY BEYOND THIS POINT.



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

void CoffeeMakerHardware::setTrayStatus(CoffeeMakerHardware::TrayStatus trayStatus, int slot) {
	_trayStatus[slot] = trayStatus;
}

void CoffeeMakerHardware::setCoffeeMakerErrorCode(CoffeeMakerHardware::CoffeeMakerErrorCode errorCode) {
	_errorCode = errorCode;
}

void CoffeeMakerHardware::setCoffeeMakerStatus(CoffeeMakerHardware::CoffeeMakerStatus status) {
	_coffeeMakerStatus = status;
}

int CoffeeMakerHardware::getAvailableTraySlot() {
	int i = 0;
	for (i = 0; i < 3; i++) {
		if (_trayStatus[i] == Vacant) {
			break;
		}
	}

	if (i == 3) i = -1;
	return i;
}

int CoffeeMakerHardware::getTraySlotStatus(int slot) {
	switch (_trayStatus[slot]) {
	case Vacant:
		return 0;
	case Occupied:
		return 1;
	}
}

int CoffeeMakerHardware::getErrorCode() {
	switch (_errorCode) {
	case None:
		return 0;
	case TrayUnaligned:
		return 1;
	case IngredientShortSupply:
		return 2;
	}
}

int CoffeeMakerHardware::getCoffeeMakerStatus() {
	switch (_coffeeMakerStatus) {
	case Available:
		return 0;
	case Preparing:
		return 1;
	case Finished:
		return 2;
	}
}

CoffeeMakerHardware::~CoffeeMakerHardware() {

}