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

	enum TrayStatus {
		Vacant,
		Occupied
	};

	enum CoffeeMakerStatus {
		Available,
		Preparing,
		Finished
	};

	enum CoffeeMakerErrorCode {
		None,
		TrayUnaligned,
		IngredientShortSupply,
		TrayFull
	};

	//getters
	int getCoffeeTspRemaining();
	int getCreamTspRemaining();
	int getSugarTspRemaining();
	int getWaterCupRemaining();

	int getAvailableTraySlot();
	int getTraySlotStatus(int slot);
	int getErrorCode();
	int getCoffeeMakerStatus();

	//setters
	void setIngredientsRemaining(int coffeeTsp, int creamTsp, int sugarTsp, int waterCup);
	void setCoffeeMakerErrorCode(CoffeeMakerHardware::CoffeeMakerErrorCode errorCode);
	void setTrayStatus(CoffeeMakerHardware::TrayStatus trayStatus, int slot);
	void setCoffeeMakerStatus(CoffeeMakerHardware::CoffeeMakerStatus status);

	virtual void begin();
	virtual void maintain();

private:
	int _coffeeTsp;
	int _creamTsp;
	int _sugarTsp;
	int _waterCup;

	CoffeeMakerStatus _coffeeMakerStatus;
	CoffeeMakerErrorCode _errorCode;
	TrayStatus _trayStatus[3];

	CoffeeMakerHardware(const CoffeeMakerHardware& c);
	CoffeeMakerHardware& operator=(const CoffeeMakerHardware& d) = delete;
};

#endif

