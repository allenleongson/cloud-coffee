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

#define NTP_PACKET_SIZE 48
#define CONSTANT_UPDATE_TIME_MS 5000

class CloudCoffeeMaker : public CoffeeMakerHardware {
public:
	CloudCoffeeMaker(const uint8_t * macAddress, const char * feedId, const char * apiKey);
	~CloudCoffeeMaker();

	void begin();
	void maintain();
protected:
	char _trayOwner[3][50];

private:
	static EthernetUDP _ntpClient;
	IPAddress _ntpIp;
	uint8_t * _macAddress;

	UIPClientExt _ethernetClient;
	IPAddress _cloudIP;
	
	//server communication
	boolean _sendToServer(const char * buf);
	boolean _updateServer();

	boolean _setTime();
	time_t _retrieveNtp();
	void _sendNTPPacket();
	static byte _packetBuffer[NTP_PACKET_SIZE];
	char * _feedId;
	char * _apiKey;

	unsigned long _lastUpdateTime;

	CloudCoffeeMaker(const CloudCoffeeMaker& c);
	CloudCoffeeMaker& operator=(const CloudCoffeeMaker& d) = delete;
};

#endif

