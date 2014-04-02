// Copyright (C) 2002-2012 Nikolaus Gebhardt
// This file is part of the "Irrlicht Engine".
// For conditions of distribution and use, see copyright notice in irrlicht.h

#ifndef __C_SCENE_NODE_ANIMATOR_FLY_STRAIGHT_H_INCLUDED__
#define __C_SCENE_NODE_ANIMATOR_FLY_STRAIGHT_H_INCLUDED__

#include "ISceneNodeAnimatorFinishing.h"

namespace irr
{
namespace scene
{
	class CSceneNodeAnimatorFlyStraight : public ISceneNodeAnimatorFinishing
	{
	public:

		//! constructor
		CSceneNodeAnimatorFlyStraight(const core::vector3df& startPoint,
						const core::vector3df& endPoint,
						u32 timeForWay,
						bool loop, u32 now, bool pingpong);

		//! animates a scene node
		virtual void animateNode(ISceneNode* node, u32 timeMs) _IRR_OVERRIDE_;

		//! Writes attributes of the scene node animator.
		virtual void serializeAttributes(io::IAttributes* out, io::SAttributeReadWriteOptions* options=0) const _IRR_OVERRIDE_;

		//! Reads attributes of the scene node animator.
		virtual void deserializeAttributes(io::IAttributes* in, io::SAttributeReadWriteOptions* options=0) _IRR_OVERRIDE_;

		//! Returns type of the scene node animator
		virtual ESCENE_NODE_ANIMATOR_TYPE getType() const _IRR_OVERRIDE_ { return ESNAT_FLY_STRAIGHT; }

		//! Creates a clone of this animator.
		/** Please note that you will have to drop
		(IReferenceCounted::drop()) the returned pointer after calling this. */
		virtual ISceneNodeAnimator* createClone(ISceneNode* node, ISceneManager* newManager=0) _IRR_OVERRIDE_;
		
		//! Reset a time-based movement by changing the starttime. 
		virtual void setStartTime(u32 time) _IRR_OVERRIDE_
		{
			StartTime = time;
		}
		
		//! Get the starttime. 
		virtual irr::u32 getStartTime() const _IRR_OVERRIDE_
		{
			return StartTime;
		}
		

	private:

		void recalculateIntermediateValues();

		core::vector3df Start;
		core::vector3df End;
		core::vector3df Vector;
		f32 TimeFactor;
		u32 StartTime;
		u32 TimeForWay;
		bool Loop;
		bool PingPong;
	};


} // end namespace scene
} // end namespace irr

#endif