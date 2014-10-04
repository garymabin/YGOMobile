/*
 * IYGOSoundEffectPlayer.h
 *
 *  Created on: 2014-9-30
 *      Author: mabin
 */

#ifndef IYGOSOUNDEFFECTPLAYER_H_
#define IYGOSOUNDEFFECTPLAYER_H_

namespace ygo {

class IYGOSoundEffectPlayer {
public:
	IYGOSoundEffectPlayer(){}

	virtual ~IYGOSoundEffectPlayer(){}

	virtual void doPlayerEnterEffect() = 0;

	virtual void doShuffleCardEffect() = 0;

	virtual void doNewTurnEffect() = 0;

	virtual void doNewPhaseEffect() = 0;

	virtual void doSetCardEffect() = 0;

	virtual void doSummonEffect() = 0;

	virtual void doSpecialSummonEffect() = 0;

	virtual void doFlipCardEffect() = 0;

	virtual void doActivateEffect() = 0;

	virtual void doDrawCardEffect() = 0;

	virtual void doDamageEffect() = 0;

	virtual void doGainLpEffect() = 0;

	virtual void doEquipEffect() = 0;

	virtual void doAddCounterEffect() = 0;

	virtual void doRemoveCounterEffect() = 0;

	virtual void doAttackEffect() = 0;

	virtual void doCoinFlipEffect() = 0;

	virtual void doDiceRollEffect() = 0;

	virtual void doChatEffect() = 0;

	virtual void doDestroyEffect() = 0;

	virtual void setSEEnabled(bool enabled) = 0;
};

}

#endif /* IYGOSOUNDEFFECTPLAYER_H_ */
