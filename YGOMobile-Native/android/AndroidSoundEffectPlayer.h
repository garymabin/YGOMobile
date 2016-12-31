/*
 * AndroidSoundEffectPlayer.h
 *
 *  Created on: 2014-10-1
 *      Author: mabin
 */

#ifndef ANDROIDSOUNDEFFECTPLAYER_H_
#define ANDROIDSOUNDEFFECTPLAYER_H_

#include "../../Classes/gframe/IYGOSoundEffectPlayer.h"
#include "../../Classes/gframe/IAudioTracker.h"

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android_native_app_glue.h>

namespace ygo {

class AndroidSoundEffectPlayer: virtual public ygo::IYGOSoundEffectPlayer {
public:
	AndroidSoundEffectPlayer(android_app* app);
	virtual ~AndroidSoundEffectPlayer();

	virtual void doPlayerEnterEffect();

	virtual void doShuffleCardEffect();

	virtual void doNewTurnEffect();

	virtual void doNewPhaseEffect();

	virtual void doSetCardEffect();

	virtual void doSummonEffect();

	virtual void doSpecialSummonEffect();

	virtual void doFlipCardEffect();

	virtual void doActivateEffect();

	virtual void doDrawCardEffect();

	virtual void doDamageEffect();

	virtual void doGainLpEffect();

	virtual void doEquipEffect();

	virtual void doAddCounterEffect();

	virtual void doRemoveCounterEffect();

	virtual void doAttackEffect();

	virtual void doCoinFlipEffect();

	virtual void doDiceRollEffect();

	virtual void doChatEffect();

	virtual void doDestroyEffect();

	virtual void setSEEnabled(bool enabled);

private:
	IAudioTracker* m_pAudioTracker;
	bool m_isEnabled;
};

}
 /* namespace ygo */
#endif /* ANDROIDSOUNDEFFECTPLAYER_H_ */
