#include "CloudCoffeeMaker.h"

EthernetUDP CloudCoffeeMaker::_ntpClient;
byte CloudCoffeeMaker::_packetBuffer[NTP_PACKET_SIZE];

//IP of jp.pool.ntp.org
CloudCoffeeMaker::CloudCoffeeMaker(const uint8_t * macAddress, const char * feedId, const char * apiKey) 
	: _ntpIp(157, 7, 154, 23), _cloudIP(64, 94, 18, 120), _lastUpdateTime(0) {
	_macAddress = new uint8_t[6];

	for (int i = 0; i < 6; i++) {
		_macAddress[i] = macAddress[i];
	}

	for (int i = 0; i < 3; i++) {
		strcpy(_trayOwner[i], "^");
	}

	//feedid and apikey
	_feedId = new char[strlen(feedId) + 1];
	strcpy(_feedId, feedId);

	_apiKey = new char[strlen(apiKey) + 1];
	strcpy(_apiKey, apiKey);

	//endpoints
	strcpy(_endpointFeedId[0], EP1_FEED_ID);
	strcpy(_endpointFeedId[1], EP2_FEED_ID);
	strcpy(_endpointFeedId[2], EP3_FEED_ID);
	strcpy(_endpointFeedId[3], EP4_FEED_ID);

	strcpy(_endpointApiKey[0], EP1_API_KEY);
	strcpy(_endpointApiKey[1], EP2_API_KEY);
	strcpy(_endpointApiKey[2], EP3_API_KEY);
	strcpy(_endpointApiKey[3], EP4_API_KEY);
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

	//subscribe to endpoints.
	for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 3; j++) {
			if (_subscribeEndpoint(i)) {
				Serial.print("Subscribed to (");
				Serial.print(i);
				Serial.println(")");
				break;
			}
		}
	}
}

void CloudCoffeeMaker::maintain() {
	CoffeeMakerHardware::maintain();

	Ethernet.maintain();

	//UPDATE XIVELY EVERY 5S
	unsigned long m = millis();
	if (_lastUpdateTime + CONSTANT_UPDATE_TIME_MS < m) {
		_updateServer();
		Serial.println("Server updated");
		_lastUpdateTime = m;
	}

	//read ethernet if there are available bytes
	_readEthernetIfAvailable();
}

//server communication
boolean CloudCoffeeMaker::_sendToServer(const char * buf) {
	_ethernetClient.print(buf);

	unsigned long startTime = millis();
	char res_buf[500];
	unsigned int cnt = 0;
	boolean res_success = false;
	boolean wait_for_n = false;

	//wait for server response
	while (startTime + 10000 > millis()) {
		if (_ethernetClient.available()) {
			res_buf[cnt] = _ethernetClient.read();

			if (wait_for_n && res_buf[cnt] == 10) {

				res_buf[++cnt] = '\0';
				res_success = true;
				break;
			}

			if (res_buf[cnt] == 13) {
				wait_for_n = true;
			}
			cnt++;
		}
	}

	return res_success;
}

boolean CloudCoffeeMaker::_updateServer() {
	char buf[800];
	char sBuf[15];
	char nBuf[2];
	nBuf[1] = '\0';

	strcpy(buf, "{\"method\":\"put\",\"resource\":\"/feeds/");
	strcat(buf, _feedId);
	strcat(buf, "\",\"headers\":{\"X-ApiKey\":\"");
	strcat(buf, _apiKey);
	strcat(buf, "\"},\"body\":{\"version\":\"1.0.0\",\"datastreams\":[");
	strcat(buf, "{\"id\":\"coffee_tsp\",\"current_value\":");
	snprintf(sBuf, 15, "%d", getCoffeeTspRemaining());
	strcat(buf, sBuf);
	strcat(buf, "},{\"id\":\"creamer_tsp\",\"current_value\":");
	snprintf(sBuf, 15, "%d", getCreamTspRemaining());
	strcat(buf, sBuf);
	strcat(buf, "},{\"id\":\"sugar_tsp\",\"current_value\":");
	snprintf(sBuf, 15, "%d", getSugarTspRemaining());
	strcat(buf, sBuf);
	strcat(buf, "},{\"id\":\"water_cups\",\"current_value\":");
	snprintf(sBuf, 15, "%d", getWaterCupRemaining());
	strcat(buf, sBuf);
	strcat(buf, "},{\"id\":\"error_code\",\"current_value\":");
	snprintf(sBuf, 15, "%d", getErrorCode());
	strcat(buf, sBuf);
	for (int i = 0; i < 3; i++) {
		nBuf[0] = '0' + i;
		strcat(buf, "},{\"id\":\"tray_");
		strcat(buf, nBuf);
		strcat(buf, "_owner\",\"current_value\":\"");
		strcat(buf, _trayOwner[i]);
		strcat(buf, "\"},{\"id\":\"tray_");
		strcat(buf, nBuf);
		strcat(buf, "_status\",\"current_value\":");
		snprintf(sBuf, 15, "%d", getTraySlotStatus(i));
		strcat(buf, sBuf);
	}
	strcat(buf, "}]}}");

	boolean res = false;
	//try 3 times
	for (int i = 0; i < 3; i++) {
		res = _sendToServer(buf);
		if (res)
			break;
		else delay(1000);
	}

	return res;
	//printToServer(buf);
}

boolean CloudCoffeeMaker::_subscribeEndpoint(int endpoint) {
	// initialize jsonBuffer
	StaticJsonBuffer<200> jsonBuffer;

	// create subscription request to feed
	JsonObject& root = jsonBuffer.createObject();
	root["method"] = "subscribe";
	char resource[50];
	strcpy(resource, "/feeds/");
	strcat(resource, _endpointFeedId[endpoint]);
	strcat(resource, "/datastreams/req_id");
	strcat(resource, "req_id");
	root["resource"] = resource;

	JsonObject& headers = root.createNestedObject("headers");
	headers["X-ApiKey"] = _apiKey;

	Serial.println();
	//root.prettyPrintTo(Serial);

	root.printTo(_ethernetClient);

	unsigned long startTime = millis();
	char res_buf[500];
	unsigned int cnt = 0;
	boolean res_success = false;
	boolean wait_for_n = false;

	//wait for server response
	while (startTime + 10000 > millis()) {
		if (_ethernetClient.available()) {
			res_buf[cnt] = _ethernetClient.read();

			if (wait_for_n && res_buf[cnt] == 10) { // \n
													//add '\0'
				res_buf[++cnt] = '\0';
				res_success = true;
				break;
			}

			if (res_buf[cnt] == 13) { // \r
				wait_for_n = true;
			}
			cnt++;
		}
	}

	return res_success;
}

void CloudCoffeeMaker::_readEthernetIfAvailable() {
	if (_ethernetClient.available() > 0) {
		unsigned long startTime = millis();
		char res_buf[500];
		unsigned int cnt = 0;
		boolean res_success = false;
		boolean wait_for_n = false;

		//wait for server response
		//shouldnt take more than 10 secs
		while (startTime + 10000 > millis()) {
			if (_ethernetClient.available()) {
				res_buf[cnt] = _ethernetClient.read();

				if (wait_for_n && res_buf[cnt] == 10) { // \n
														//add '\0'
					res_buf[++cnt] = '\0';
					res_success = true;
					break;
				}

				if (res_buf[cnt] == 13) { // \r
					wait_for_n = true;
				}
				cnt++;
			}
		}

		if (res_success) {
			Serial.println(res_buf);
			//_processIncomingUpdate(res_buf);
			//_prevHeartBeat = millis();
		}
	}
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
	delete[] _feedId;
	delete[] _apiKey;
}