/*
 * YGOGameOptions.cpp
 *
 *  Created on: 2014-4-5
 *      Author: mabin
 */

#include "YGOGameOptions.h"
#include "string.h"
#include "stdio.h"

namespace irr {
namespace android {

YGOGameOptions::YGOGameOptions(void* data) {
	//read ip addr
	char * rawdata = (char*)data;
	int tmplength = ::BufferIO::ReadInt32(rawdata);
	m_pipAddr = new char[tmplength];
	memcpy(m_pipAddr, rawdata, tmplength);
	rawdata += tmplength;

	//read user name
	tmplength = ::BufferIO::ReadInt32(rawdata);
	m_puserName = new char[tmplength];
	memcpy(m_puserName, rawdata, tmplength);
	rawdata += tmplength;

	//read room name
	tmplength = ::BufferIO::ReadInt32(rawdata);
	m_proomName = new char[tmplength];
	memcpy(m_proomName, rawdata, tmplength);
	rawdata += tmplength;

	//read room password
	tmplength = ::BufferIO::ReadInt32(rawdata);
	m_proomPasswd = new char[tmplength];
	memcpy(m_proomPasswd, rawdata, tmplength);
	rawdata += tmplength;

	m_port = ::BufferIO::ReadInt32(rawdata);
	m_rule = ::BufferIO::ReadInt32(rawdata);
	m_startLP = ::BufferIO::ReadInt32(rawdata);
	m_startHand = ::BufferIO::ReadInt32(rawdata);
	m_drawCount = ::BufferIO::ReadInt32(rawdata);

	m_enablePriority = ::BufferIO::ReadInt32(rawdata) == 1 ? 'T' : 'F';
	m_noDeckCheck = ::BufferIO::ReadInt32(rawdata) == 1 ? 'T' : 'F';
	m_noDeckShuffle = ::BufferIO::ReadInt32(rawdata) == 1 ? 'T' : 'F';
}

YGOGameOptions::~YGOGameOptions() {
	delete m_pipAddr;
	delete m_puserName;
	delete m_proomName;
	delete m_proomPasswd;
}

} /* namespace android */
} /* namespace irr */
