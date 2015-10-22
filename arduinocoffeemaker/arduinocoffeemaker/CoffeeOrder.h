// CoffeeOrder.h

#ifndef _COFFEEORDER_h
#define _COFFEEORDER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

class CoffeeOrder {
public:
	CoffeeOrder(const char * cApiKey, const char * cFeedId, int cTray);

	char apiKey[50];
	char feedId[20];
	char username[30];
	int tray;
	int coffeeTsp;
	int sugarTsp;
	int creamTsp;
};

#endif

