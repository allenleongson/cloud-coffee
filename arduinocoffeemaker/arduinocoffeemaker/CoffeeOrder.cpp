#include "CoffeeOrder.h"

CoffeeOrder::CoffeeOrder(const char * cFeedId, const char * cApiKey, int cTray) {
	strcpy(apiKey, cApiKey);
	strcpy(feedId, cFeedId);
	tray = cTray;
}

CoffeeOrder::CoffeeOrder(){}