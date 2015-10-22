#include "CoffeeOrder.h"

CoffeeOrder::CoffeeOrder(const char * cApiKey, const char * cFeedId, int cTray) {
	strcpy(apiKey, cApiKey);
	strcpy(feedId, cFeedId);
	tray = cTray;
}