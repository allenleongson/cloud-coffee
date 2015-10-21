#ifndef _COFFEEMAKERHARDWARE_h
#define _COFFEEMAKERHARDWARE_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

class CoffeeMakerHardware {
public:
	CoffeeMakerHardware();
	~CoffeeMakerHardware();

	enum CoffeeMakerTraySlotStatus {
		Available,
		Reserved,
		Preparing,
		Finished
	};

	enum CoffeeMakerErrorCode {
		None,
		TrayUnaligned,
		IngredientShortSupply
	};

	//getters
	int getCoffeeTspRemaining();
	int getCreamTspRemaining();
	int getSugarTspRemaining();
	int getWaterCupRemaining();

	//setters
	void setIngredientsRemaining(int coffeeTsp, int creamTsp, int sugarTsp, int waterCup);
	void setTrayStatus(CoffeeMakerHardware::CoffeeMakerTraySlotStatus status, int slot);
	void setCoffeeMakerErrorCode(CoffeeMakerHardware::CoffeeMakerErrorCode errorCode);

	virtual void begin();
	virtual void maintain();

private:
	int _coffeeTsp;
	int _creamTsp;
	int _sugarTsp;
	int _waterCup;

	CoffeeMakerTraySlotStatus _traySlotStatus[3];
	CoffeeMakerErrorCode _errorCode;

	CoffeeMakerHardware(const CoffeeMakerHardware& c);
	CoffeeMakerHardware& operator=(const CoffeeMakerHardware& d) = delete;
};

#endif

