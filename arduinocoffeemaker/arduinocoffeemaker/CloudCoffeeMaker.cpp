#include "CloudCoffeeMaker.h"

EthernetUDP CloudCoffeeMaker::_ntpClient;
byte CloudCoffeeMaker::_packetBuffer[NTP_PACKET_SIZE];

//IP of jp.pool.ntp.org
CloudCoffeeMaker::CloudCoffeeMaker(const uint8_t * macAddress) : _ntpIp(157, 7, 154, 23), _cloudIP(64, 94, 18, 120) {
	_macAddress = new uint8_t[6];

	for (int i = 0; i < 6; i++) {
		_macAddress[i] = macAddress[i];
	}

	for (int i = 0; i < 3; i++) {
		strcpy(_trayOwner[i], "^");
	}
}

void CloudCoffeeMaker::begin() {
	CoffeeMakerHardware::begin();

	//initialize ethernet.
	while (!Ethernet.begin(_macAddress)) {
		Serial.println("DHCP failed");
		delay(1000);
	}

	Serial.println("DHCP Successful.");

	//set time
	while (!_setTime()) {
		Serial.println("Time not set.");
		delay(1000);
	}

	//connect to cloud/Xively
	while (!_ethernetClient.connect(_cloudIP, 8081)) {
		Serial.println("Connection failed");
		delay(1000);
	}

	Serial.println("Cloud connection successful");
}

void CloudCoffeeMaker::maintain() {
	CoffeeMakerHardware::maintain();

	Ethernet.maintain();
}

boolean CloudCoffeeMaker::_setTime() {
	boolean timeset = false;
	_ntpClient.begin(8888);
	time_t time = 0;
	time = _retrieveNtp();
	if (time > 0) {
		setTime(time);
		timeset = true;
		Serial.print("NOW: ");
		Serial.println(now());
	}
	return timeset;
}

//Retrieve NTP Time
time_t CloudCoffeeMaker::_retrieveNtp() {
	while (_ntpClient.parsePacket() > 0); // discard any previously received packets
	Serial.println("Transmit NTP Request");
	_sendNTPPacket();
	uint32_t beginWait = millis();
	while (millis() - beginWait < 1500) {
		int size = _ntpClient.parsePacket();
		if (size >= NTP_PACKET_SIZE) {
			Serial.println("Receive NTP Response");
			_ntpClient.read((char*)_packetBuffer, NTP_PACKET_SIZE);  // read packet into the buffer
			unsigned long secsSince1900;
			// convert four bytes starting at location 40 to a long integer
			secsSince1900 = (unsigned long)_packetBuffer[40] << 24;
			secsSince1900 |= (unsigned long)_packetBuffer[41] << 16;
			secsSince1900 |= (unsigned long)_packetBuffer[42] << 8;
			secsSince1900 |= (unsigned long)_packetBuffer[43];
			return secsSince1900 - 2208988800UL;
		}
	}
	Serial.println("No NTP Response :-(");
	return 0; // return 0 if unable to get the time
}

void CloudCoffeeMaker::_sendNTPPacket() {
	// set all bytes in the buffer to 0
	memset(_packetBuffer, 0, NTP_PACKET_SIZE);
	// Initialize values needed to form NTP request
	// (see URL above for details on the packets)
	_packetBuffer[0] = 0b11100011;   // LI, Version, Mode
	_packetBuffer[1] = 0;     // Stratum, or type of clock
	_packetBuffer[2] = 6;     // Polling Interval
	_packetBuffer[3] = 0xEC;  // Peer Clock Precision
							  // 8 bytes of zero for Root Delay & Root Dispersion
	_packetBuffer[12] = 49;
	_packetBuffer[13] = 0x4E;
	_packetBuffer[14] = 49;
	_packetBuffer[15] = 52;
	// all NTP fields have been given values, now
	// you can send a packet requesting a timestamp:                 
	_ntpClient.beginPacket(_ntpIp, 123); //NTP requests are to port 123
	_ntpClient.write(_packetBuffer, NTP_PACKET_SIZE);
	_ntpClient.endPacket();
}

CloudCoffeeMaker::~CloudCoffeeMaker() {
	delete[] _macAddress;
}