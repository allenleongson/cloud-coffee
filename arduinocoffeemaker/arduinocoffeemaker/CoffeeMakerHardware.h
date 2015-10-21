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

	virtual void begin();
	virtual void maintain();

protected:
private:
	CoffeeMakerHardware(const CoffeeMakerHardware& c);
	CoffeeMakerHardware& operator=(const CoffeeMakerHardware& d) = delete;
};

#endif

