#ifndef _CLOUDCOFFEEMAKER_h
#define _CLOUDCOFFEEMAKER_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "CoffeeMakerHardware.h"
#include <Time.h>
#include <UIPEthernet.h>
#include <ArduinoJson.h>

#define NTP_PACKET_SIZE 48
#define CONSTANT_UPDATE_TIME_MS 10000

#define EP1_FEED_ID "631913265"
#define EP2_FEED_ID "1663563459"
#define EP3_FEED_ID "1829222295"
#define EP4_FEED_ID "14274993"

#define EP1_API_KEY "3LYXcaFzpmZCb18Rr7u9Am0ZFLLDtoW90OXdYKQRaDHpXEJu"
#define EP2_API_KEY "SuB8htKzqYKpeFq9cPd0dsgIqv2d45cGotNEgpzgd4mFlIvW"
#define EP3_API_KEY "yONkjCBQUKRpiAiGrTMLp5LdEp7bILVRjGCyI7XB0OZf0pEv"
#define EP4_API_KEY "D3aTFj7c2JqtL3ya09hJGAIVGvjKt0yteBOFuwRB55E6LwwS"

class CloudCoffeeMaker : public CoffeeMakerHardware {
public:
	CloudCoffeeMaker(const uint8_t * macAddress, const char * feedId, const char * apiKey);
	~CloudCoffeeMaker();

	void begin();
	void maintain();
protected:
	char _trayOwner[3][50];
	char _endpointFeedId[4][20];
	char _endpointApiKey[4][49];

private:
	uint8_t * _macAddress;

	UIPClientExt _ethernetClient;
	IPAddress _cloudIP;
	
	//server communication
	boolean _sendToServer(const char * buf);
	boolean _updateServer();
	boolean _subscribeEndpoint(int endpoint);
	void _readEthernetIfAvailable();
	void _processAvailableData(char * buf);

	//update endpoints
	boolean _sendErrorCode(const char * feedId, const char * apiKey, unsigned long req_id, int error);

	char * _feedId;
	char * _apiKey;

	unsigned long _lastUpdateTime;

	CloudCoffeeMaker(const CloudCoffeeMaker& c);
	CloudCoffeeMaker& operator=(const CloudCoffeeMaker& d) = delete;
};

#endif

