/*
 * YGOGameOptions.h
 *
 *  Created on: 2014-4-5
 *      Author: mabin
 */

#ifndef YGOGAMEOPTIONS_H_
#define YGOGAMEOPTIONS_H_

#include "bufferio_android.h"
#include "../gframe/config.h"
#include "xstring.h"

namespace irr {
namespace android {

class YGOGameOptions {
public:
	YGOGameOptions(void* data);
	virtual ~YGOGameOptions();

	inline const char* getIPAddr() const {
		return m_pipAddr;
	}

	inline const char* getUserName() {
		return m_puserName;
	}

	inline int getPort() const {
		return m_port;
	}

	inline void formatGameParams(wchar_t* dst) {
		char formatParams[512] = {0};
		sprintf(formatParams, "%d%d%c%c%c%d,%d,%d,%s",
				m_rule, m_mode, m_enablePriority, m_noDeckCheck, m_noDeckShuffle, m_startLP, m_startHand, m_drawCount, m_proomName);
		if (m_proomPasswd != NULL) {
			char * extraParams = formatParams + strlen(formatParams);
			sprintf(extraParams, "$%s", m_proomPasswd);
		}
		BufferIO::DecodeUTF8(formatParams, dst);
	}
private:
	char* m_pipAddr;
	char* m_puserName;
	char* m_proomName;
	char* m_proomPasswd;
	int m_port;
	int m_mode;
	int m_rule;
	int m_startLP;
	int m_startHand;
	int m_drawCount;
	char m_enablePriority;
	char m_noDeckCheck;
	char m_noDeckShuffle;
};

} /* namespace android */
}
 /* namespace irr */
#endif /* YGOGAMEOPTIONS_H_ */
