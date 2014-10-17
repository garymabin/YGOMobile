#include "game.h"
#include "image_manager.h"

namespace ygo {

ImageManager imageManager;

bool ImageManager::Initial(const path dir) {
	tCover = driver->getTexture((dir + path("textures/cover.jpg")).c_str());
	tUnknown = driver->getTexture((dir + path("textures/unknown.jpg")).c_str());
	tAct = driver->getTexture((dir + path("textures/act.png")).c_str());
	tAttack = driver->getTexture((dir + path("textures/attack.png")).c_str());
	tChain = driver->getTexture((dir + path("textures/chain.png")).c_str());
	tNegated = driver->getTexture((dir + path("textures/negated.png")).c_str());
	tNumber = driver->getTexture((dir + path("textures/number.png")).c_str());
	tLPBar = driver->getTexture((dir + path("textures/lp.png")).c_str());
	tLPFrame = driver->getTexture((dir + path("textures/lpf.png")).c_str());
	tMask = driver->getTexture((dir + path("textures/mask.png")).c_str());
	tEquip = driver->getTexture((dir + path("textures/equip.png")).c_str());
	tTarget = driver->getTexture((dir + path("textures/target.png")).c_str());
	tLim = driver->getTexture((dir + path("textures/lim.png")).c_str());
	tHand[0] = driver->getTexture((dir + path("textures/f1.jpg")).c_str());
	tHand[1] = driver->getTexture((dir + path("textures/f2.jpg")).c_str());
	tHand[2] = driver->getTexture((dir + path("textures/f3.jpg")).c_str());
	tBackGround = driver->getTexture((dir + path("textures/bg.jpg")).c_str());
	tField = driver->getTexture((dir + path("textures/field2.png")).c_str());
	return true;
}
void ImageManager::SetDevice(irr::IrrlichtDevice* dev) {
	device = dev;
	driver = dev->getVideoDriver();
}
void ImageManager::ClearTexture() {
	for(auto tit = tMap.begin(); tit != tMap.end(); ++tit) {
		if(tit->second)
			driver->removeTexture(tit->second);
	}
	for(auto tit = tThumb.begin(); tit != tThumb.end(); ++tit) {
		if(tit->second)
			driver->removeTexture(tit->second);
	}
	tMap.clear();
	tThumb.clear();
}
void ImageManager::RemoveTexture(int code) {
	auto tit = tMap.find(code);
	if(tit != tMap.end()) {
		if(tit->second)
			driver->removeTexture(tit->second);
		tMap.erase(tit);
	}
}
irr::video::ITexture* ImageManager::GetTexture(int code) {
	if(code == 0)
		return tUnknown;
	auto tit = tMap.find(code);
	if(tit == tMap.end()) {
		char file[256];
#ifdef _IRR_ANDROID_PATFORM_
		sprintf(file, "%s/%d.jpg", irr::android::getCardImagePath(mainGame->appMain).c_str(), code);
#else
        sprintf(file, "pics/%d.jpg", code);
#endif
		irr::video::ITexture* img = driver->getTexture(file);
		if(img == NULL) {
			tMap[code] = NULL;
			return GetTextureThumb(code);
		} else {
			tMap[code] = img;
			return img;
		}
	}
	if(tit->second)
		return tit->second;
	else
		return GetTextureThumb(code);
}
irr::video::ITexture* ImageManager::GetTextureThumb(int code) {
	if(code == 0)
		return tUnknown;
	auto tit = tThumb.find(code);
	if(tit == tThumb.end()) {
		char file[256];
#ifdef _IRR_ANDROID_PATFORM_
		sprintf(file, "%s/thumbnail/%d.jpg", irr::android::getCardImagePath(mainGame->appMain).c_str(), code);
#else
        sprintf(file, "pics/thumbnail/%d.jpg", code);
#endif
		irr::video::ITexture* img = driver->getTexture(file);
		if(img == NULL) {
			tThumb[code] = NULL;
			return tUnknown;
		} else {
			tThumb[code] = img;
			return img;
		}
	}
	if(tit->second)
		return tit->second;
	else
		return tUnknown;
}
}
