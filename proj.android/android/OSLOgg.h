#ifndef _OSLOgg_
#define _OSLOgg_

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <os.h>
#include <config_types.h>
#include <ivorbisfile.h>

class OSLOgg {
public:

	OSLOgg(AAssetManager* am, char * path);
	~OSLOgg();
	void load(AAssetManager* am, char* filename);

	long getSize();
	char * getBuffer();

private:

	long size;
	unsigned int uiCurrPos;
	int current_section;
	unsigned int uiPCMSamples;
	vorbis_info* vi;
	char * buf;
	OggVorbis_File vf;

	void * ConvertOggToPCM(unsigned int uiOggSize, char* pvOggBuffer);

	void getInfo(unsigned int uiOggSize, char* pvOggBuffer);
};

#endif
