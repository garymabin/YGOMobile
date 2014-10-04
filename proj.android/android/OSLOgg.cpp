#include "OSLOgg.h"
#include <assert.h>
#include <string.h>

OSLOgg::OSLOgg(AAssetManager* am, char * path) {
	load(am, path);
}
OSLOgg::~OSLOgg() {
	free(buf);
}

unsigned int Min(unsigned int agr1, unsigned int agr2) {
	return (agr1 < agr2) ? agr1 : agr2;
}

void OSLOgg::load(AAssetManager* am, char* filename) {

	AAsset* f = AAssetManager_open(am, filename, AASSET_MODE_BUFFER);
	if (!f) {
		return;
	}
	buf = 0;
	off_t length = AAsset_getLength(f);
	buf = (char*) malloc(length);
	if (buf) {
		if (AAsset_read(f, buf, length)) {

		} else {
			free(buf);
			AAsset_close(f);
		}
	}
	this->buf = (char *) ConvertOggToPCM(length, buf);
	if (f)
		AAsset_close(f);
}

unsigned int suiCurrPos = 0;
unsigned int suiSize = 0;

static size_t read_func(void* ptr, size_t size, size_t nmemb,
		void* datasource) {
	unsigned int uiBytes = Min(suiSize - suiCurrPos,
			(unsigned int) nmemb * (unsigned int) size);
	memcpy(ptr, (unsigned char*) datasource + suiCurrPos, uiBytes);
	suiCurrPos += uiBytes;
	return uiBytes;
}

static int seek_func(void* datasource, ogg_int64_t offset, int whence) {
	if (whence == SEEK_SET)
		suiCurrPos = (unsigned int) offset;
	else if (whence == SEEK_CUR)
		suiCurrPos = suiCurrPos + (unsigned int) offset;
	else if (whence == SEEK_END)
		suiCurrPos = suiSize;
	return 0;
}

static int close_func(void* datasource) {
	return 0;
}

static long tell_func(void* datasource) {
	return (long) suiCurrPos;
}

void OSLOgg::getInfo(unsigned int uiOggSize, char* pvOggBuffer) {
	// replace callbacks
	ov_callbacks callbacks;
	callbacks.read_func = &read_func;
	callbacks.seek_func = &seek_func;
	callbacks.close_func = &close_func;
	callbacks.tell_func = &tell_func;

	suiCurrPos = 0;
	suiSize = uiOggSize;
	int iRet = ov_open_callbacks(pvOggBuffer, &vf, NULL, 0, callbacks);

	// header
	vi = ov_info(&vf, -1);

	uiPCMSamples = (unsigned int) ov_pcm_total(&vf, -1);
}

void * OSLOgg::ConvertOggToPCM(unsigned int uiOggSize, char* pvOggBuffer) {
	long iRead;

	if (suiSize == 0) {
		getInfo(uiOggSize, pvOggBuffer);
		current_section = 0;
		iRead = 0;
		uiCurrPos = 0;
	}

	void* pvPCMBuffer = malloc(uiPCMSamples * vi->channels * sizeof(short));

	// decode
	do {
		iRead = ov_read(&vf, (char*) pvPCMBuffer + uiCurrPos, 4096,
				&current_section);
		uiCurrPos += (unsigned int) iRead;
	} while (iRead != 0);
	size = uiCurrPos;
	free(pvOggBuffer);
	buf = (char *) pvPCMBuffer;

	return buf;

}

long OSLOgg::getSize() {
	return size;
}
char * OSLOgg::getBuffer() {
	return buf;
}
