#ifndef _CLOUDCOFFEEMAKER_h
#define _CLOUDCOFFEEMAKER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "CoffeeMakerHardware.h"

class CloudCoffeeMaker : public CoffeeMakerHardware {
public:
	CloudCoffeeMaker();
	~CloudCoffeeMaker();

	void begin();
	void maintain();
protected:
private:
	CloudCoffeeMaker(const CloudCoffeeMaker& c);
	CloudCoffeeMaker& operator=(const CloudCoffeeMaker& d) = delete;
};

#endif

