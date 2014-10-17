#include "game.h"
#include "materials.h"
#include "image_manager.h"
#include "deck_manager.h"
#include "duelclient.h"
#include "../ocgcore/field.h"

namespace ygo {

void Game::DrawSelectionLine(irr::video::S3DVertex* vec, bool strip, int width, float* cv) {
#if defined(_IRR_ANDROID_PLATFORM_) || defined(_IRR_IPHONE_PLATFORM_)
	matManager.mOutLine.ColorMaterial = irr::video::ECM_NONE;
	driver->setMaterial(matManager.mOutLine);
	if(strip) {
		if(linePattern < 15) {
			driver->draw3DLine(vec[0].Pos, vec[0].Pos + (vec[1].Pos - vec[0].Pos) * (linePattern + 1) / 15.0);
			driver->draw3DLine(vec[1].Pos, vec[1].Pos + (vec[3].Pos - vec[1].Pos) * (linePattern + 1) / 15.0);
			driver->draw3DLine(vec[3].Pos, vec[3].Pos + (vec[2].Pos - vec[3].Pos) * (linePattern + 1) / 15.0);
			driver->draw3DLine(vec[2].Pos, vec[2].Pos + (vec[0].Pos - vec[2].Pos) * (linePattern + 1) / 15.0);
		} else {
			driver->draw3DLine(vec[0].Pos + (vec[1].Pos - vec[0].Pos) * (linePattern - 14) / 15.0, vec[1].Pos);
			driver->draw3DLine(vec[1].Pos + (vec[3].Pos - vec[1].Pos) * (linePattern - 14) / 15.0, vec[3].Pos);
			driver->draw3DLine(vec[3].Pos + (vec[2].Pos - vec[3].Pos) * (linePattern - 14) / 15.0, vec[2].Pos);
			driver->draw3DLine(vec[2].Pos + (vec[0].Pos - vec[2].Pos) * (linePattern - 14) / 15.0, vec[0].Pos);
		}
	} else {
		driver->draw3DLine(vec[0].Pos, vec[1].Pos);
		driver->draw3DLine(vec[1].Pos, vec[3].Pos);
		driver->draw3DLine(vec[3].Pos, vec[2].Pos);
		driver->draw3DLine(vec[2].Pos, vec[0].Pos);
	}
#else
	if(!gameConf.use_d3d) {
		float origin[4] = {1.0f, 1.0f, 1.0f, 1.0f};
		glLineWidth(width);
		glLineStipple(1, linePattern);
		if(strip)
			glEnable(GL_LINE_STIPPLE);
		glDisable(GL_TEXTURE_2D);
		glMaterialfv(GL_FRONT, GL_AMBIENT, cv);
		glBegin(GL_LINE_LOOP);
		glVertex3fv((float*)&vec[0].Pos);
		glVertex3fv((float*)&vec[1].Pos);
		glVertex3fv((float*)&vec[3].Pos);
		glVertex3fv((float*)&vec[2].Pos);
		glEnd();
		glMaterialfv(GL_FRONT, GL_AMBIENT, origin);
		glDisable(GL_LINE_STIPPLE);
	} else {
		driver->setMaterial(matManager.mOutLine);
		if(strip) {
			if(linePattern < 15) {
				driver->draw3DLine(vec[0].Pos, vec[0].Pos + (vec[1].Pos - vec[0].Pos) * (linePattern + 1) / 15.0);
				driver->draw3DLine(vec[1].Pos, vec[1].Pos + (vec[3].Pos - vec[1].Pos) * (linePattern + 1) / 15.0);
				driver->draw3DLine(vec[3].Pos, vec[3].Pos + (vec[2].Pos - vec[3].Pos) * (linePattern + 1) / 15.0);
				driver->draw3DLine(vec[2].Pos, vec[2].Pos + (vec[0].Pos - vec[2].Pos) * (linePattern + 1) / 15.0);
			} else {
				driver->draw3DLine(vec[0].Pos + (vec[1].Pos - vec[0].Pos) * (linePattern - 14) / 15.0, vec[1].Pos);
				driver->draw3DLine(vec[1].Pos + (vec[3].Pos - vec[1].Pos) * (linePattern - 14) / 15.0, vec[3].Pos);
				driver->draw3DLine(vec[3].Pos + (vec[2].Pos - vec[3].Pos) * (linePattern - 14) / 15.0, vec[2].Pos);
				driver->draw3DLine(vec[2].Pos + (vec[0].Pos - vec[2].Pos) * (linePattern - 14) / 15.0, vec[0].Pos);
			}
		} else {
			driver->draw3DLine(vec[0].Pos, vec[1].Pos);
			driver->draw3DLine(vec[1].Pos, vec[3].Pos);
			driver->draw3DLine(vec[3].Pos, vec[2].Pos);
			driver->draw3DLine(vec[2].Pos, vec[0].Pos);
		}
	}
#endif
}
void Game::DrawBackGround() {
	static int selFieldAlpha = 255;
	static int selFieldDAlpha = -10;
	matrix4 im = irr::core::IdentityMatrix;
	im.setTranslation(vector3df(0, 0, -0.01f));
	driver->setTransform(irr::video::ETS_WORLD, im);
	//dark shade
//	matManager.mSelField.AmbientColor = 0xff000000;
//	matManager.mSelField.DiffuseColor = 0xa0000000;
//	driver->setMaterial(matManager.mSelField);
//	for(int i = 0; i < 120; i += 4)
//		driver->drawVertexPrimitiveList(&matManager.vFields[i], 4, matManager.iRectangle, 2);
//	driver->setTransform(irr::video::ETS_WORLD, irr::core::IdentityMatrix);
//	driver->setMaterial(matManager.mBackLine);
//	driver->drawVertexPrimitiveList(matManager.vBackLine, 76, matManager.iBackLine, 58, irr::video::EVT_STANDARD, irr::scene::EPT_LINES);
	//draw field
	driver->setTransform(irr::video::ETS_WORLD, irr::core::IdentityMatrix);
	matManager.mTexture.setTexture(0, imageManager.tField);
	driver->setMaterial(matManager.mTexture);
	driver->drawVertexPrimitiveList(matManager.vField, 4, matManager.iRectangle, 2);
	driver->setMaterial(matManager.mBackLine);
	//select field
	if(dInfo.curMsg == MSG_SELECT_PLACE || dInfo.curMsg == MSG_SELECT_DISFIELD) {
		float cv[4] = {0.0f, 0.0f, 1.0f, 1.0f};
		int filter = 0x1;
		for (int i = 0; i < 5; ++i, filter <<= 1) {
			if ((dField.selectable_field & filter) > 0)
				DrawSelectionLine(&matManager.vFields[16 + i * 4], !(dField.selected_field & filter), 2, cv);
		}
		filter = 0x100;
		for (int i = 0; i < 8; ++i, filter <<= 1) {
			if ((dField.selectable_field & filter) > 0)
				DrawSelectionLine(&matManager.vFields[36 + i * 4], !(dField.selected_field & filter), 2, cv);
		}
		filter = 0x10000;
		for (int i = 0; i < 5; ++i, filter <<= 1) {
			if ((dField.selectable_field & filter) > 0)
				DrawSelectionLine(&matManager.vFields[84 + i * 4], !(dField.selected_field & filter), 2, cv);
		}
		filter = 0x1000000;
		for (int i = 0; i < 8; ++i, filter <<= 1) {
			if ((dField.selectable_field & filter) > 0)
				DrawSelectionLine(&matManager.vFields[104 + i * 4], !(dField.selected_field & filter), 2, cv);
		}
	}
	//disabled field
	{
		/*float cv[4] = {0.0f, 0.0f, 1.0f, 1.0f};*/
		int filter = 0x1;
		for (int i = 0; i < 5; ++i, filter <<= 1) {
			if ((dField.disabled_field & filter) > 0) {
				driver->draw3DLine(matManager.vFields[16 + i * 4].Pos, matManager.vFields[16 + i * 4 + 3].Pos, 0xffffffff);
				driver->draw3DLine(matManager.vFields[16 + i * 4 + 1].Pos, matManager.vFields[16 + i * 4 + 2].Pos, 0xffffffff);
			}
		}
		filter = 0x100;
		for (int i = 0; i < 8; ++i, filter <<= 1) {
			if ((dField.disabled_field & filter) > 0) {
				driver->draw3DLine(matManager.vFields[36 + i * 4].Pos, matManager.vFields[36 + i * 4 + 3].Pos, 0xffffffff);
				driver->draw3DLine(matManager.vFields[36 + i * 4 + 1].Pos, matManager.vFields[36 + i * 4 + 2].Pos, 0xffffffff);
			}
		}
		filter = 0x10000;
		for (int i = 0; i < 5; ++i, filter <<= 1) {
			if ((dField.disabled_field & filter) > 0) {
				driver->draw3DLine(matManager.vFields[84 + i * 4].Pos, matManager.vFields[84 + i * 4 + 3].Pos, 0xffffffff);
				driver->draw3DLine(matManager.vFields[84 + i * 4 + 1].Pos, matManager.vFields[84 + i * 4 + 2].Pos, 0xffffffff);
			}
		}
		filter = 0x1000000;
		for (int i = 0; i < 8; ++i, filter <<= 1) {
			if ((dField.disabled_field & filter) > 0) {
				driver->draw3DLine(matManager.vFields[104 + i * 4].Pos, matManager.vFields[104 + i * 4 + 3].Pos, 0xffffffff);
				driver->draw3DLine(matManager.vFields[104 + i * 4 + 1].Pos, matManager.vFields[104 + i * 4 + 2].Pos, 0xffffffff);
			}
		}
	}
	//current sel
	if (dField.hovered_location != 0 && dField.hovered_location  != 2) {
		int index = 0;
		if (dField.hovered_controler == 0) {
			if (dField.hovered_location == LOCATION_DECK) index = 0;
			else if (dField.hovered_location == LOCATION_MZONE) index = 16 + dField.hovered_sequence * 4;
			else if (dField.hovered_location == LOCATION_SZONE) index = 36 + dField.hovered_sequence * 4;
			else if (dField.hovered_location == LOCATION_GRAVE) index = 4;
			else if (dField.hovered_location == LOCATION_REMOVED) index = 12;
			else if (dField.hovered_location == LOCATION_EXTRA) index = 8;
		} else {
			if (dField.hovered_location == LOCATION_DECK) index = 68;
			else if (dField.hovered_location == LOCATION_MZONE) index = 84 + dField.hovered_sequence * 4;
			else if (dField.hovered_location == LOCATION_SZONE) index = 104 + dField.hovered_sequence * 4;
			else if (dField.hovered_location == LOCATION_GRAVE) index = 72;
			else if (dField.hovered_location == LOCATION_REMOVED) index = 80;
			else if (dField.hovered_location == LOCATION_EXTRA) index = 76;
		}
		selFieldAlpha += selFieldDAlpha;
		if (selFieldAlpha <= 5) {
			selFieldAlpha = 5;
			selFieldDAlpha = 10;
		}
		if (selFieldAlpha >= 205) {
			selFieldAlpha = 205;
			selFieldDAlpha = -10;
		}
		matManager.mSelField.AmbientColor = 0xffffffff;
		matManager.mSelField.DiffuseColor = selFieldAlpha << 24;
		driver->setMaterial(matManager.mSelField);
		driver->drawVertexPrimitiveList(&matManager.vFields[index], 4, matManager.iRectangle, 2);
	}
}
void Game::DrawCards() {
	for(int p = 0; p < 2; ++p) {
		for(int i = 0; i < 5; ++i)
			if(dField.mzone[p][i])
				DrawCard(dField.mzone[p][i]);
		for(int i = 0; i < 8; ++i)
			if(dField.szone[p][i])
				DrawCard(dField.szone[p][i]);
		for(size_t i = 0; i < dField.deck[p].size(); ++i)
			DrawCard(dField.deck[p][i]);
		for(size_t i = 0; i < dField.hand[p].size(); ++i)
			DrawCard(dField.hand[p][i]);
		for(size_t i = 0; i < dField.grave[p].size(); ++i)
			DrawCard(dField.grave[p][i]);
		for(size_t i = 0; i < dField.remove[p].size(); ++i)
			DrawCard(dField.remove[p][i]);
		for(size_t i = 0; i < dField.extra[p].size(); ++i)
			DrawCard(dField.extra[p][i]);
	}
	for(auto cit = dField.overlay_cards.begin(); cit != dField.overlay_cards.end(); ++cit)
		DrawCard(*cit);
}
void Game::DrawCard(ClientCard* pcard) {
	driver->setTransform(irr::video::ETS_WORLD, pcard->mTransform);
	if(pcard->aniFrame) {
		if(pcard->is_moving) {
			pcard->curPos += pcard->dPos;
			pcard->curRot += pcard->dRot;
			pcard->mTransform.setTranslation(pcard->curPos);
			pcard->mTransform.setRotationRadians(pcard->curRot);
		}
		if(pcard->is_fading)
			pcard->curAlpha += pcard->dAlpha;
		pcard->aniFrame--;
		if(pcard->aniFrame == 0) {
			pcard->is_moving = false;
			pcard->is_fading = false;
		}
	}
	matManager.mCard.AmbientColor = 0xffffffff;
	matManager.mCard.DiffuseColor = (pcard->curAlpha << 24) | 0xffffff;
	matManager.mCard.setTexture(0, imageManager.GetTexture(pcard->code));
	driver->setTransform(irr::video::ETS_WORLD, pcard->mTransform);
	driver->setMaterial(matManager.mCard);
	driver->drawVertexPrimitiveList(matManager.vCardFront, 4, matManager.iRectangle, 2);
	matManager.mCard.setTexture(0, imageManager.tCover);
	driver->setMaterial(matManager.mCard);
	driver->drawVertexPrimitiveList(matManager.vCardBack, 4, matManager.iRectangle, 2);
	if(pcard->is_showequip) {
		matManager.mTexture.setTexture(0, imageManager.tEquip);
		driver->setMaterial(matManager.mTexture);
		driver->drawVertexPrimitiveList(matManager.vSymbol, 4, matManager.iRectangle, 2);
	} else if(pcard->is_showtarget) {
		matManager.mTexture.setTexture(0, imageManager.tTarget);
		driver->setMaterial(matManager.mTexture);
		driver->drawVertexPrimitiveList(matManager.vSymbol, 4, matManager.iRectangle, 2);
	} else if(pcard->is_disabled && (pcard->location & LOCATION_ONFIELD) && (pcard->position & POS_FACEUP)) {
		matManager.mTexture.setTexture(0, imageManager.tNegated);
		driver->setMaterial(matManager.mTexture);
		driver->drawVertexPrimitiveList(matManager.vNegate, 4, matManager.iRectangle, 2);
	}
	if(pcard->is_selectable && (pcard->location & 0xe)) {
		float cv[4] = {1.0f, 1.0f, 0.0f, 1.0f};
		if((pcard->location == LOCATION_HAND && pcard->code) || ((pcard->location & 0xc) && (pcard->position & POS_FACEUP)))
			DrawSelectionLine(matManager.vCardOutline, !pcard->is_selected, 2, cv);
		else
			DrawSelectionLine(matManager.vCardOutliner, !pcard->is_selected, 2, cv);
	}
	if(pcard->is_highlighting) {
		float cv[4] = {0.0f, 1.0f, 1.0f, 1.0f};
		if((pcard->location == LOCATION_HAND && pcard->code) || ((pcard->location & 0xc) && (pcard->position & POS_FACEUP)))
			DrawSelectionLine(matManager.vCardOutline, true, 2, cv);
		else
			DrawSelectionLine(matManager.vCardOutliner, true, 2, cv);
	}
	if(pcard->cmdFlag & COMMAND_ATTACK) {
		matManager.mTexture.setTexture(0, imageManager.tAttack);
		driver->setMaterial(matManager.mTexture);
		irr::core::matrix4 atk;
		atk.setTranslation(pcard->curPos + vector3df(0, -atkdy / 4.0f - 0.35f, 0.05f));
		driver->setTransform(irr::video::ETS_WORLD, atk);
		driver->drawVertexPrimitiveList(matManager.vSymbol, 4, matManager.iRectangle, 2);
	}
}
void Game::DrawMisc() {
	static irr::core::vector3df act_rot(0, 0, 0);
	irr::core::matrix4 im, ic, it;
	act_rot.Z += 0.02f;
	im.setRotationRadians(act_rot);
	matManager.mTexture.setTexture(0, imageManager.tAct);
	driver->setMaterial(matManager.mTexture);
	if(dField.deck_act) {
		im.setTranslation(vector3df(matManager.vFields[0].Pos.X - (matManager.vFields[0].Pos.X - matManager.vFields[1].Pos.X)/2,
			matManager.vFields[0].Pos.Y - (matManager.vFields[0].Pos.Y - matManager.vFields[3].Pos.Y)/2, dField.deck[0].size() * 0.01f + 0.02f));
		driver->setTransform(irr::video::ETS_WORLD, im);
		driver->drawVertexPrimitiveList(matManager.vActivate, 4, matManager.iRectangle, 2);
	}
	if(dField.grave_act) {
		im.setTranslation(vector3df(matManager.vFields[4].Pos.X - (matManager.vFields[4].Pos.X - matManager.vFields[5].Pos.X)/2,
			matManager.vFields[4].Pos.Y - (matManager.vFields[4].Pos.Y - matManager.vFields[6].Pos.Y)/2, dField.grave[0].size() * 0.01f + 0.02f));
		driver->setTransform(irr::video::ETS_WORLD, im);
		driver->drawVertexPrimitiveList(matManager.vActivate, 4, matManager.iRectangle, 2);
	}
	if(dField.remove_act) {
		im.setTranslation(vector3df(matManager.vFields[12].Pos.X - (matManager.vFields[12].Pos.X - matManager.vFields[13].Pos.X)/2,
			matManager.vFields[12].Pos.Y - (matManager.vFields[12].Pos.Y - matManager.vFields[14].Pos.Y)/2, dField.remove[0].size() * 0.01f + 0.02f));
		driver->setTransform(irr::video::ETS_WORLD, im);
		driver->drawVertexPrimitiveList(matManager.vActivate, 4, matManager.iRectangle, 2);
	}
	if(dField.extra_act) {
		im.setTranslation(vector3df(matManager.vFields[8].Pos.X - (matManager.vFields[8].Pos.X - matManager.vFields[9].Pos.X)/2,
			matManager.vFields[8].Pos.Y - (matManager.vFields[8].Pos.Y - matManager.vFields[10].Pos.Y)/2, dField.extra[0].size() * 0.01f + 0.02f));
		driver->setTransform(irr::video::ETS_WORLD, im);
		driver->drawVertexPrimitiveList(matManager.vActivate, 4, matManager.iRectangle, 2);
	}
	if(dField.pzone_act) {
		im.setTranslation(vector3df(matManager.vFields[60].Pos.X - (matManager.vFields[60].Pos.X - matManager.vFields[61].Pos.X)/2,
			matManager.vFields[60].Pos.Y - (matManager.vFields[60].Pos.Y - matManager.vFields[62].Pos.Y)/2, 0.03f));
		driver->setTransform(irr::video::ETS_WORLD, im);
		driver->drawVertexPrimitiveList(matManager.vActivate, 4, matManager.iRectangle, 2);
	}
	if(dField.chains.size() > 1) {
		for(size_t i = 0; i < dField.chains.size(); ++i) {
			if(dField.chains[i].solved)
				break;
			matManager.mTRTexture.setTexture(0, imageManager.tChain);
			matManager.mTRTexture.AmbientColor = 0xffffff00;
			ic.setRotationRadians(act_rot);
			ic.setTranslation(dField.chains[i].chain_pos);
			driver->setMaterial(matManager.mTRTexture);
			driver->setTransform(irr::video::ETS_WORLD, ic);
			driver->drawVertexPrimitiveList(matManager.vSymbol, 4, matManager.iRectangle, 2);
			it.setScale(0.6f);
			it.setTranslation(dField.chains[i].chain_pos);
			matManager.mTRTexture.setTexture(0, imageManager.tNumber);
#ifdef _IRR_ANDROID_PLATFORM_
			matManager.vChainNum[0].Color = SColor(255, 255, 255, 255);
			matManager.vChainNum[1].Color = SColor(255, 255, 255, 255);
			matManager.vChainNum[2].Color = SColor(255, 255, 255, 255);
			matManager.vChainNum[3].Color = SColor(255, 255, 255, 255);
#endif
			matManager.vChainNum[0].TCoords = vector2df(0.19375f * (i % 5), 0.2421875f * (i / 5));
			matManager.vChainNum[1].TCoords = vector2df(0.19375f * (i % 5 + 1), 0.2421875f * (i / 5));
			matManager.vChainNum[2].TCoords = vector2df(0.19375f * (i % 5), 0.2421875f * (i / 5 + 1));
			matManager.vChainNum[3].TCoords = vector2df(0.19375f * (i % 5 + 1), 0.2421875f * (i / 5 + 1));
			driver->setMaterial(matManager.mTRTexture);
			driver->setTransform(irr::video::ETS_WORLD, it);
			driver->drawVertexPrimitiveList(matManager.vChainNum, 4, matManager.iRectangle, 2);
		}
	}
	//lp bar
	if((dInfo.turn % 2 && dInfo.isFirst) || (!(dInfo.turn % 2) && !dInfo.isFirst)) {
		driver->draw2DRectangle(0xa0000000, recti(327 * mainGame->xScale, 8 * mainGame->yScale, 630 * mainGame->xScale, 51 * mainGame->yScale));
		driver->draw2DRectangleOutline(recti(327 * mainGame->xScale, 8 * mainGame->yScale, 630 * mainGame->xScale, 51 * mainGame->yScale), 0xffff8080);
	} else {
		driver->draw2DRectangle(0xa0000000, recti(689 * mainGame->xScale, 8 * mainGame->yScale, 991 * mainGame->xScale, 51 * mainGame->yScale));
		driver->draw2DRectangleOutline(recti(689 * mainGame->xScale, 8 * mainGame->yScale, 991 * mainGame->xScale, 51 * mainGame->yScale), 0xffff8080);
	}
	driver->draw2DImage(imageManager.tLPFrame, recti(330 * mainGame->xScale, 10 * mainGame->yScale, 629 * mainGame->xScale, 30 * mainGame->yScale), recti(0, 0, 200, 20), 0, 0, true);
	driver->draw2DImage(imageManager.tLPFrame, recti(691 * mainGame->xScale, 10 * mainGame->yScale, 990 * mainGame->xScale, 30 * mainGame->yScale), recti(0, 0, 200, 20), 0, 0, true);
	if(dInfo.lp[0] >= 8000)
		driver->draw2DImage(imageManager.tLPBar, recti(335 * mainGame->xScale, 12 * mainGame->yScale, 625 * mainGame->xScale, 28 * mainGame->yScale), recti(0, 0, 16, 16), 0, 0, true);
	else driver->draw2DImage(imageManager.tLPBar, recti(335 * mainGame->xScale, 12 * mainGame->yScale, (335 + 290 * dInfo.lp[0] / 8000) * mainGame->xScale, 28 * mainGame->yScale), recti(0, 0, 16, 16), 0, 0, true);
	if(dInfo.lp[1] >= 8000)
		driver->draw2DImage(imageManager.tLPBar, recti(696 * mainGame->xScale, 12 * mainGame->yScale, 986 * mainGame->xScale, 28 * mainGame->yScale), recti(0, 0, 16, 16), 0, 0, true);
	else driver->draw2DImage(imageManager.tLPBar, recti((986 - 290 * dInfo.lp[1] / 8000) * mainGame->xScale, 12 * mainGame->yScale, 986 * mainGame->xScale, 28 * mainGame->yScale), recti(0, 0, 16, 16), 0, 0, true);
	if(lpframe) {
		dInfo.lp[lpplayer] -= lpd;
		myswprintf(dInfo.strLP[lpplayer], L"%d", dInfo.lp[lpplayer]);
		lpccolor -= 0x19000000;
		lpframe--;
	}
	if(lpcstring) {
		if(lpplayer == 0) {
			lpcFont->draw(lpcstring, recti(400 * mainGame->xScale, 470 * mainGame->yScale, 920 * mainGame->xScale, 520 * mainGame->yScale), lpccolor | 0x00ffffff, true, false, 0);
			lpcFont->draw(lpcstring, recti(400 * mainGame->xScale, 472 * mainGame->yScale, 922 * mainGame->xScale, 520 * mainGame->yScale), lpccolor, true, false, 0);
		} else {
			lpcFont->draw(lpcstring, recti(400 * mainGame->xScale, 160 * mainGame->yScale, 920 * mainGame->xScale, 210 * mainGame->yScale), lpccolor | 0x00ffffff, true, false, 0);
			lpcFont->draw(lpcstring, recti(400 * mainGame->xScale, 162 * mainGame->yScale, 922 * mainGame->xScale, 210 * mainGame->yScale), lpccolor, true, false, 0);
		}
	}
	if(!dInfo.isReplay && dInfo.player_type < 7 && dInfo.time_limit) {
		driver->draw2DRectangle(recti(525 * mainGame->xScale, 34 * mainGame->yScale, (525 + dInfo.time_left[0] * 100 / dInfo.time_limit) * mainGame->xScale, 44 * mainGame->yScale), 0xa0e0e0e0, 0xa0e0e0e0, 0xa0c0c0c0, 0xa0c0c0c0);
		driver->draw2DRectangleOutline(recti(525 * mainGame->xScale, 34 * mainGame->yScale, 625 * mainGame->xScale, 44 * mainGame->yScale), 0xffffffff);
		driver->draw2DRectangle(recti((795 - dInfo.time_left[1] * 100 / dInfo.time_limit) * mainGame->xScale, 34 * mainGame->yScale, 795 * mainGame->xScale, 44 * mainGame->yScale), 0xa0e0e0e0, 0xa0e0e0e0, 0xa0c0c0c0, 0xa0c0c0c0);
		driver->draw2DRectangleOutline(recti(695 * mainGame->xScale, 34 * mainGame->yScale, 795 * mainGame->xScale, 44 * mainGame->yScale), 0xffffffff);
	}
	numFont->draw(dInfo.strLP[0], recti(330 * mainGame->xScale, 11 * mainGame->yScale, 629 * mainGame->xScale, 30 * mainGame->yScale), 0xff000000, true, false, 0);
	numFont->draw(dInfo.strLP[0], recti(330 * mainGame->xScale, 12 * mainGame->yScale, 631 * mainGame->xScale, 30 * mainGame->yScale), 0xffffff00, true, false, 0);
	numFont->draw(dInfo.strLP[1], recti(691 * mainGame->xScale, 11 * mainGame->yScale, 990 * mainGame->xScale, 30 * mainGame->yScale), 0xff000000, true, false, 0);
	numFont->draw(dInfo.strLP[1], recti(691 * mainGame->xScale, 12 * mainGame->yScale, 992 * mainGame->xScale, 30 * mainGame->yScale), 0xffffff00, true, false, 0);

	if(!dInfo.isTag || !dInfo.tag_player[0])
		textFont->draw(dInfo.hostname, recti(335 * mainGame->xScale, 31 * mainGame->yScale, 629 * mainGame->xScale, 50 * mainGame->yScale), 0xffffffff, false, false, 0);
	else
		textFont->draw(dInfo.hostname_tag, recti(335 * mainGame->xScale, 31 * mainGame->yScale, 629 * mainGame->xScale, 50 * mainGame->yScale), 0xffffffff, false, false, 0);
	if(!dInfo.isTag || !dInfo.tag_player[1]) {
		auto cld = textFont->getDimension(dInfo.clientname);
		textFont->draw(dInfo.clientname, recti((986 - cld.Width) * mainGame->xScale, 31 * mainGame->yScale, 986 * mainGame->xScale, 50 * mainGame->yScale), 0xffffffff, false, false, 0);
	} else {
		auto cld = textFont->getDimension(dInfo.clientname_tag);
		textFont->draw(dInfo.clientname_tag, recti((986 - cld.Width) * mainGame->xScale, 31 * mainGame->yScale, 986 * mainGame->xScale, 50 * mainGame->yScale), 0xffffffff, false, false, 0);
	}
	driver->draw2DRectangle(recti(632 * mainGame->xScale, 10 * mainGame->yScale, 688 * mainGame->xScale, 30 * mainGame->yScale), 0x00000000, 0x00000000, 0xffffffff, 0xffffffff);
	driver->draw2DRectangle(recti(632 * mainGame->xScale, 30 * mainGame->yScale, 688 * mainGame->xScale, 50 * mainGame->yScale), 0xffffffff, 0xffffffff, 0x00000000, 0x00000000);
	lpcFont->draw(dataManager.GetNumString(dInfo.turn), recti(635 * mainGame->xScale, 5 * mainGame->yScale, 685 * mainGame->xScale, 40 * mainGame->yScale), 0x80000000, true, false, 0);
	lpcFont->draw(dataManager.GetNumString(dInfo.turn), recti(635 * mainGame->xScale, 5 * mainGame->yScale, 687 * mainGame->xScale, 40 * mainGame->yScale), 0x8000ffff, true, false, 0);
    ClientCard* pcard;
	for(int i = 0; i < 5; ++i) {
		pcard = dField.mzone[0][i];
		if(pcard && pcard->code != 0) {
			int m = 493 + i * 85;
			adFont->draw(L"/", recti((m - 4) * mainGame->xScale, 416 * mainGame->yScale, (m + 4) * mainGame->xScale, 436 * mainGame->yScale), 0xff000000, true, false, 0);
			adFont->draw(L"/", recti((m - 3) * mainGame->xScale, 417 * mainGame->yScale, (m + 5) * mainGame->xScale, 437 * mainGame->yScale), 0xffffffff, true, false, 0);
			int w = adFont->getDimension(pcard->atkstring).Width;
			adFont->draw(pcard->atkstring, recti((m - 5 - w) * mainGame->xScale, 416 * mainGame->yScale, (m - 5) * mainGame->xScale, 436 * mainGame->yScale), 0xff000000, false, false, 0);
			adFont->draw(pcard->atkstring, recti((m - 4 - w) * mainGame->xScale, 417 * mainGame->yScale, (m - 4) * mainGame->xScale, 437 * mainGame->yScale),
			             pcard->attack > pcard->base_attack ? 0xffffff00 : pcard->attack < pcard->base_attack ? 0xffff2090 : 0xffffffff , false, false, 0);
			w = adFont->getDimension(pcard->defstring).Width;
			adFont->draw(pcard->defstring, recti((m + 4) * mainGame->xScale, 416 * mainGame->yScale, (m + 4 + w) * mainGame->xScale, 436 * mainGame->yScale), 0xff000000, false, false, 0);
			adFont->draw(pcard->defstring, recti((m + 5) * mainGame->xScale, 417 * mainGame->yScale, (m + 5 + w) * mainGame->xScale, 437 * mainGame->yScale),
			             pcard->defence > pcard->base_defence ? 0xffffff00 : pcard->defence < pcard->base_defence ? 0xffff2090 : 0xffffffff , false, false, 0);
			adFont->draw(pcard->lvstring, recti((473 + i * 80) * mainGame->xScale, 356 * mainGame->yScale, (475 + i * 80) * mainGame->xScale, 366 * mainGame->yScale), 0xff000000, false, false, 0);
			adFont->draw(pcard->lvstring, recti((474 + i * 80) * mainGame->xScale, 357 * mainGame->yScale, (476 + i * 80) * mainGame->xScale, 367 * mainGame->yScale),
			             (pcard->type & TYPE_XYZ) ? 0xffff80ff : (pcard->type & TYPE_TUNER) ? 0xffffff00 : 0xffffffff, false, false, 0);
		}
	}
	for(int i = 0; i < 5; ++i) {
		pcard = dField.mzone[1][i];
		if(pcard && (pcard->position & POS_FACEUP)) {
			int m = 803 - i * 68;
			adFont->draw(L"/", recti((m - 4) * mainGame->xScale, 235 * mainGame->yScale, (m + 4) * mainGame->xScale, 255 * mainGame->yScale), 0xff000000, true, false, 0);
			adFont->draw(L"/", recti((m - 3) * mainGame->xScale, 236 * mainGame->yScale, (m + 5) * mainGame->xScale, 256 * mainGame->yScale), 0xffffffff, true, false, 0);
			int w = adFont->getDimension(pcard->atkstring).Width;
			adFont->draw(pcard->atkstring, recti((m - 5 - w) * mainGame->xScale, 235 * mainGame->yScale, (m - 5) * mainGame->xScale, 255 * mainGame->yScale), 0xff000000, false, false, 0);
			adFont->draw(pcard->atkstring, recti((m - 4 - w) * mainGame->xScale, 236 * mainGame->yScale, (m - 4) * mainGame->xScale, 256 * mainGame->yScale),
			             pcard->attack > pcard->base_attack ? 0xffffff00 : pcard->attack < pcard->base_attack ? 0xffff2090 : 0xffffffff , false, false, 0);
			w = adFont->getDimension(pcard->defstring).Width;
			adFont->draw(pcard->defstring, recti((m + 4) * mainGame->xScale, 235 * mainGame->yScale, (m + 4 + w) * mainGame->xScale, 255 * mainGame->yScale), 0xff000000, false, false, 0);
			adFont->draw(pcard->defstring, recti((m + 5) * mainGame->xScale, 236 * mainGame->yScale, (m + 5 + w) * mainGame->xScale, 256 * mainGame->yScale),
			             pcard->defence > pcard->base_defence ? 0xffffff00 : pcard->defence < pcard->base_defence ? 0xffff2090 : 0xffffffff , false, false, 0);
			adFont->draw(pcard->lvstring, recti((779 - i * 71) * mainGame->xScale, 272 * mainGame->yScale, (800 - i * 71) * mainGame->xScale, 292 * mainGame->yScale), 0xff000000, false, false, 0);
			adFont->draw(pcard->lvstring, recti((780 - i * 71) * mainGame->xScale, 273 * mainGame->yScale, (800 - i * 71) * mainGame->xScale, 293 * mainGame->yScale),
			             (pcard->type & TYPE_XYZ) ? 0xffff80ff : (pcard->type & TYPE_TUNER) ? 0xffffff00 : 0xffffffff, false, false, 0);
		}
	}
	pcard = dField.szone[0][6];
	if(pcard) {
		adFont->draw(pcard->lscstring, recti(386 * mainGame->xScale, 398 * mainGame->yScale, 398 * mainGame->xScale, 418 * mainGame->yScale), 0xff000000, true, false, 0);
		adFont->draw(pcard->lscstring, recti(387 * mainGame->xScale, 399 * mainGame->yScale, 399 * mainGame->xScale, 419 * mainGame->yScale), 0xffffffff, true, false, 0);
	}
	pcard = dField.szone[0][7];
	if(pcard) {
		adFont->draw(pcard->rscstring, recti(880 * mainGame->xScale, 398 * mainGame->yScale, 912 * mainGame->xScale, 418 * mainGame->yScale), 0xff000000, true, false, 0);
		adFont->draw(pcard->rscstring, recti(881 * mainGame->xScale, 399 * mainGame->yScale, 913 * mainGame->xScale, 419 * mainGame->yScale), 0xffffffff, true, false, 0);
	}
	pcard = dField.szone[1][6];
	if(pcard) {
		adFont->draw(pcard->lscstring, recti(834 * mainGame->xScale, 245 * mainGame->yScale, 866 * mainGame->xScale, 265 * mainGame->yScale), 0xff000000, true, false, 0);
		adFont->draw(pcard->lscstring, recti(835 * mainGame->xScale, 246 * mainGame->yScale, 867 * mainGame->xScale, 266 * mainGame->yScale), 0xffffffff, true, false, 0);
	}
	pcard = dField.szone[1][7];
	if(pcard) {
		adFont->draw(pcard->rscstring, recti(428 * mainGame->xScale, 245 * mainGame->yScale, 460 * mainGame->xScale, 265 * mainGame->yScale), 0xff000000, true, false, 0);
		adFont->draw(pcard->rscstring, recti(429 * mainGame->xScale, 246 * mainGame->yScale, 461 * mainGame->xScale, 266 * mainGame->yScale), 0xffffffff, true, false, 0);
	}
	if(dField.extra[0].size()) {
		numFont->draw(dataManager.GetNumString(dField.extra[0].size()), recti(330 * mainGame->xScale, 562 * mainGame->yScale, 381 * mainGame->xScale, 552 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.extra[0].size()), recti(330 * mainGame->xScale, 563 * mainGame->yScale, 383 * mainGame->xScale, 553 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.deck[0].size()) {
		numFont->draw(dataManager.GetNumString(dField.deck[0].size()), recti(907 * mainGame->xScale, 562 * mainGame->yScale, 1021 * mainGame->xScale, 552 * mainGame->yScale) , 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.deck[0].size()), recti(908 * mainGame->xScale, 563 * mainGame->yScale, 1023 * mainGame->xScale, 553 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.grave[0].size()) {
		numFont->draw(dataManager.GetNumString(dField.grave[0].size()), recti(837 * mainGame->xScale, 375 * mainGame->yScale, 984 * mainGame->xScale, 456 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.grave[0].size()), recti(837 * mainGame->xScale, 376 * mainGame->yScale, 986 * mainGame->xScale, 457 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.remove[0].size()) {
		numFont->draw(dataManager.GetNumString(dField.remove[0].size()), recti(1015 * mainGame->xScale, 375 * mainGame->yScale, 957 * mainGame->xScale, 380 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.remove[0].size()), recti(1015 * mainGame->xScale, 376 * mainGame->yScale, 959 * mainGame->xScale, 381 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.extra[1].size()) {
		numFont->draw(dataManager.GetNumString(dField.extra[1].size()), recti(818 * mainGame->xScale, 207 * mainGame->yScale, 908 * mainGame->xScale, 232 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.extra[1].size()), recti(818 * mainGame->xScale, 208 * mainGame->yScale, 910 * mainGame->xScale, 233 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.deck[1].size()) {
		numFont->draw(dataManager.GetNumString(dField.deck[1].size()), recti(465 * mainGame->xScale, 207 * mainGame->yScale, 481 * mainGame->xScale, 232 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.deck[1].size()), recti(465 * mainGame->xScale, 208 * mainGame->yScale, 483 * mainGame->xScale, 233 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.grave[1].size()) {
		numFont->draw(dataManager.GetNumString(dField.grave[1].size()), recti(420 * mainGame->xScale, 310 * mainGame->yScale, 462 * mainGame->xScale, 281 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.grave[1].size()), recti(420 * mainGame->xScale, 311 * mainGame->yScale, 464 * mainGame->xScale, 282 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
	if(dField.remove[1].size()) {
		numFont->draw(dataManager.GetNumString(dField.remove[1].size()), recti(300 * mainGame->xScale, 310 * mainGame->yScale, 443 * mainGame->xScale, 340 * mainGame->yScale), 0xff000000, true, false, 0);
		numFont->draw(dataManager.GetNumString(dField.remove[1].size()), recti(300 * mainGame->xScale, 311 * mainGame->yScale, 445 * mainGame->xScale, 341 * mainGame->yScale), 0xffffff00, true, false, 0);
	}
}
void Game::DrawGUI() {
	if(imageLoading.size()) {
		std::map<irr::gui::CGUIImageButton*, int>::iterator mit;
		for(mit = imageLoading.begin(); mit != imageLoading.end(); ++mit)
			mit->first->setImage(imageManager.GetTexture(mit->second));
		imageLoading.clear();
	}
	for(auto fit = fadingList.begin(); fit != fadingList.end();) {
		auto fthis = fit++;
		FadingUnit& fu = *fthis;
		if(fu.fadingFrame) {
			fu.guiFading->setVisible(true);
			if(fu.isFadein) {
				if(fu.fadingFrame > 5) {
					fu.fadingUL.X -= fu.fadingDiff.X;
					fu.fadingLR.X += fu.fadingDiff.X;
					fu.fadingFrame--;
					fu.guiFading->setRelativePosition(irr::core::recti(fu.fadingUL, fu.fadingLR));
				} else {
					fu.fadingUL.Y -= fu.fadingDiff.Y;
					fu.fadingLR.Y += fu.fadingDiff.Y;
					fu.fadingFrame--;
					if(!fu.fadingFrame) {
						fu.guiFading->setRelativePosition(fu.fadingSize);
						if(fu.guiFading == wPosSelect) {
							btnPSAU->setDrawImage(true);
							btnPSAD->setDrawImage(true);
							btnPSDU->setDrawImage(true);
							btnPSDD->setDrawImage(true);
						}
						if(fu.guiFading == wCardSelect) {
							for(int i = 0; i < 5; ++i)
								btnCardSelect[i]->setDrawImage(true);
						}
						env->setFocus(fu.guiFading);
					} else
						fu.guiFading->setRelativePosition(irr::core::recti(fu.fadingUL, fu.fadingLR));
				}
			} else {
				if(fu.fadingFrame > 5) {
					fu.fadingUL.Y += fu.fadingDiff.Y;
					fu.fadingLR.Y -= fu.fadingDiff.Y;
					fu.fadingFrame--;
					fu.guiFading->setRelativePosition(irr::core::recti(fu.fadingUL, fu.fadingLR));
				} else {
					fu.fadingUL.X += fu.fadingDiff.X;
					fu.fadingLR.X -= fu.fadingDiff.X;
					fu.fadingFrame--;
					if(!fu.fadingFrame) {
						fu.guiFading->setVisible(false);
						fu.guiFading->setRelativePosition(fu.fadingSize);
						if(fu.guiFading == wPosSelect) {
							btnPSAU->setDrawImage(true);
							btnPSAD->setDrawImage(true);
							btnPSDU->setDrawImage(true);
							btnPSDD->setDrawImage(true);
						}
						if(fu.guiFading == wCardSelect) {
							for(int i = 0; i < 5; ++i)
								btnCardSelect[i]->setDrawImage(true);
						}
					} else
						fu.guiFading->setRelativePosition(irr::core::recti(fu.fadingUL, fu.fadingLR));
				}
				if(fu.signalAction && !fu.fadingFrame) {
					DuelClient::SendResponse();
					fu.signalAction = false;
				}
			}
		} else if(fu.autoFadeoutFrame) {
			fu.autoFadeoutFrame--;
			if(!fu.autoFadeoutFrame)
				HideElement(fu.guiFading);
		} else
			fadingList.erase(fthis);
	}
	env->drawAll();
}
void Game::DrawSpec() {
	if(showcard) {
		switch(showcard) {
		case 1: {
//			driver->draw2DImage(imageManager.GetTexture(showcardcode), position2di(574 * mainGame->xScale, 150 * mainGame->yScale));
			driver->draw2DImage(imageManager.GetTexture(showcardcode), recti(574 * mainGame->xScale, 150 * mainGame->yScale, (574 + 177) * mainGame->xScale, (150 + 254) * mainGame->yScale), recti(0, 0, 177, 254), 0, 0, true);
			driver->draw2DImage(imageManager.tMask, recti(574 * mainGame->xScale, 150 * mainGame->yScale, (574 + (showcarddif > 177 ? 177 : showcarddif)) * mainGame->xScale, 404 * mainGame->yScale),
			                    recti((254 - showcarddif), 0, 254 - (showcarddif > 177 ? showcarddif - 177 : 0), 254), 0, 0, true);
			showcarddif += 15;
			if(showcarddif >= 254) {
				showcard = 2;
				showcarddif = 0;
			}
			break;
		}
		case 2: {
//			driver->draw2DImage(imageManager.GetTexture(showcardcode), position2di(574 * mainGame->xScale, 150 * mainGame->yScale));
			driver->draw2DImage(imageManager.GetTexture(showcardcode), recti(574 * mainGame->xScale, 150 * mainGame->yScale, (574 + 177) * mainGame->xScale, (150 + 254) * mainGame->yScale), recti(0, 0, 177, 254), 0, 0, true);
			driver->draw2DImage(imageManager.tMask, recti((574 + showcarddif) * mainGame->xScale, 150 * mainGame->yScale, 761 * mainGame->xScale, 404 * mainGame->yScale), recti(0, 0, (177 - showcarddif), 254), 0, 0, true);
			showcarddif += 15;
			if(showcarddif >= 177) {
				showcard = 0;
			}
			break;
		}
		case 3: {
//			driver->draw2DImage(imageManager.GetTexture(showcardcode), position2di(574 * mainGame->xScale, 150 * mainGame->yScale));
			driver->draw2DImage(imageManager.GetTexture(showcardcode), recti(574 * mainGame->xScale, 150 * mainGame->yScale, (574 + 177) * mainGame->xScale, (150 + 254) * mainGame->yScale), recti(0, 0, 177, 254), 0, 0, true);
			driver->draw2DImage(imageManager.tNegated, recti((536 + showcarddif) * mainGame->xScale, (141 + showcarddif) * mainGame->yScale, (793 - showcarddif) * mainGame->xScale, (397 - showcarddif) * mainGame->yScale), recti(0, 0, 128, 128), 0, 0, true);
			if(showcarddif < 64)
				showcarddif += 4;
			break;
		}
		case 4: {
			matManager.c2d[0] = (showcarddif << 24) | 0xffffff;
			matManager.c2d[1] = (showcarddif << 24) | 0xffffff;
			matManager.c2d[2] = (showcarddif << 24) | 0xffffff;
			matManager.c2d[3] = (showcarddif << 24) | 0xffffff;
			driver->draw2DImage(imageManager.GetTexture(showcardcode), recti(574 * mainGame->xScale, 154 * mainGame->yScale, 751 * mainGame->xScale, 404 * mainGame->yScale),
			                    recti(0, 0, 177, 254), 0, matManager.c2d, true);
			if(showcarddif < 255)
				showcarddif += 17;
			break;
		}
		case 5: {
			matManager.c2d[0] = (showcarddif << 25) | 0xffffff;
			matManager.c2d[1] = (showcarddif << 25) | 0xffffff;
			matManager.c2d[2] = (showcarddif << 25) | 0xffffff;
			matManager.c2d[3] = (showcarddif << 25) | 0xffffff;
			driver->draw2DImage(imageManager.GetTexture(showcardcode), recti((662 - showcarddif * 0.69685f) * mainGame->xScale, (277 - showcarddif) * mainGame->yScale, (662 + showcarddif * 0.69685f) * mainGame->xScale, (277 + showcarddif) * mainGame->yScale),
			                    recti(0, 0, 177, 254), 0, matManager.c2d, true);
			if(showcarddif < 127)
				showcarddif += 9;
			break;
		}
		case 6: {
			driver->draw2DImage(imageManager.GetTexture(showcardcode), recti(574 * mainGame->xScale, 150 * mainGame->yScale, (574 + 177) * mainGame->xScale, (150 + 254) * mainGame->yScale), recti(0, 0, 177, 254), 0, 0, true);
			driver->draw2DImage(imageManager.tNumber, recti((536 + showcarddif) * mainGame->xScale, (141 + showcarddif) * mainGame->yScale, (793 - showcarddif) * mainGame->xScale, (397 - showcarddif) * mainGame->yScale),
			                    recti(((showcardp % 5) * 64), ((showcardp / 5) * 64), ((showcardp % 5 + 1) * 64), (showcardp / 5 + 1) * 64), 0, 0, true);
			if(showcarddif < 64)
				showcarddif += 4;
			break;
		}
		case 7: {
			core::position2d<s32> corner[4];
			float y = sin(showcarddif * 3.1415926f / 180.0f) * 254;
			corner[0] = core::position2d<s32>(574 - (254 - y) * 0.3f, 404 - y);
			corner[1] = core::position2d<s32>(751 + (254 - y) * 0.3f, 404 - y);
			corner[2] = core::position2d<s32>(574, 404);
			corner[3] = core::position2d<s32>(751, 404);
			irr::gui::Draw2DImageQuad(driver, imageManager.GetTexture(showcardcode), rect<s32>(0, 0, 177, 254), corner);
			showcardp++;
			showcarddif += 9;
			if(showcarddif >= 90)
				showcarddif = 90;
			if(showcardp == 60) {
				showcardp = 0;
				showcarddif = 0;
			}
			break;
		}
		case 100: {
			if(showcardp < 60) {
				driver->draw2DImage(imageManager.tHand[(showcardcode >> 16) & 0x3], position2di(615 * mainGame->xScale, showcarddif * mainGame->yScale));
				driver->draw2DImage(imageManager.tHand[showcardcode & 0x3], position2di(615 * mainGame->xScale, (540 - showcarddif) * mainGame->yScale));
				float dy = -0.333333f * showcardp + 10;
				showcardp++;
				if(showcardp < 30)
					showcarddif += (int)dy;
			} else
				showcard = 0;
			break;
		}
		case 101: {
			const wchar_t* lstr = L"";
			switch(showcardcode) {
			case 1:
				lstr = L"You Win!";
				break;
			case 2:
				lstr = L"You Lose!";
				break;
			case 3:
				lstr = L"Draw Game";
				break;
			case 4:
				lstr = L"Draw Phase";
				break;
			case 5:
				lstr = L"Standby Phase";
				break;
			case 6:
				lstr = L"Main Phase 1";
				break;
			case 7:
				lstr = L"Battle Phase";
				break;
			case 8:
				lstr = L"Main Phase 2";
				break;
			case 9:
				lstr = L"End Phase";
				break;
			case 10:
				lstr = L"Next Players Turn";
				break;
			case 11:
				lstr = L"Duel Start";
				break;
			case 12:
				lstr = L"Duel1 Start";
				break;
			case 13:
				lstr = L"Duel2 Start";
				break;
			case 14:
				lstr = L"Duel3 Start";
				break;
			}
			auto pos = lpcFont->getDimension(lstr);
			if(showcardp < 10) {
				int alpha = (showcardp * 25) << 24;
				lpcFont->draw(lstr, recti((671 - pos.Width / 2 - (9 - showcardp) * 40) * mainGame->xScale, 271 * mainGame->yScale, 970 * mainGame->xScale, 350 * mainGame->yScale), alpha);
				lpcFont->draw(lstr, recti((670 - pos.Width / 2 - (9 - showcardp) * 40) * mainGame->xScale, 270 * mainGame->yScale, 970 * mainGame->xScale, 350 * mainGame->yScale), alpha | 0xffffff);
			} else if(showcardp < showcarddif) {
				lpcFont->draw(lstr, recti((671 - pos.Width / 2) * mainGame->xScale, 271 * mainGame->yScale, 970 * mainGame->xScale, 350 * mainGame->yScale), 0xff000000);
				lpcFont->draw(lstr, recti((670 - pos.Width / 2) * mainGame->xScale, 270 * mainGame->yScale, 970 * mainGame->xScale, 350 * mainGame->yScale), 0xffffffff);
				if(dInfo.vic_string && (showcardcode == 1 || showcardcode == 2)) {
					driver->draw2DRectangle(0xa0000000, recti(540 * mainGame->xScale, 320 * mainGame->yScale, 800 * mainGame->xScale, 340 * mainGame->yScale));
					guiFont->draw(dInfo.vic_string, recti(502 * mainGame->xScale, 321 * mainGame->yScale, 840 * mainGame->xScale, 340 * mainGame->yScale), 0xff000000, true, true);
					guiFont->draw(dInfo.vic_string, recti(500 * mainGame->xScale, 320 * mainGame->yScale, 840 * mainGame->xScale, 340 * mainGame->yScale), 0xffffffff, true, true);
				}
			} else if(showcardp < showcarddif + 10) {
				int alpha = ((showcarddif + 10 - showcardp) * 25) << 24;
				lpcFont->draw(lstr, recti((671 - pos.Width / 2 + (showcardp - showcarddif) * 40) * mainGame->xScale, 271 * mainGame->yScale, 970 * mainGame->xScale, 350 * mainGame->yScale), alpha);
				lpcFont->draw(lstr, recti((670 - pos.Width / 2 + (showcardp - showcarddif) * 40) * mainGame->xScale, 270 * mainGame->yScale, 970 * mainGame->xScale, 350 * mainGame->yScale), alpha | 0xffffff);
			}
			showcardp++;
			break;
		}
		}
	}
	if(is_attacking) {
		irr::core::matrix4 matk;
		matk.setTranslation(atk_t);
		matk.setRotationRadians(atk_r);
		driver->setTransform(irr::video::ETS_WORLD, matk);
		driver->setMaterial(matManager.mATK);
		driver->drawVertexPrimitiveList(&matManager.vArrow[attack_sv], 40, matManager.iArrow, 10, EVT_STANDARD, EPT_TRIANGLE_STRIP);
		attack_sv += 4;
		if (attack_sv > 28)
			attack_sv = 0;
	}
	bool showChat = true;
	if(hideChat) {
	    showChat = false;
	    hideChatTimer = 10;
	} else if(hideChatTimer > 0) {
	    showChat = false;
	    hideChatTimer--;
	}
	int maxChatLines = mainGame->dInfo.isStarted ? 5 : 8;
	for(int i = 0; i < maxChatLines; ++i) {
		static unsigned int chatColor[] = {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xff8080ff, 0xffff4040, 0xffff4040,
		                                   0xffff4040, 0xff40ff40, 0xff4040ff, 0xff40ffff, 0xffff40ff, 0xffffff40, 0xffffffff, 0xff808080, 0xff404040};
		if(chatTiming[i]) {
			chatTiming[i]--;
			if(!showChat && i > 2)
				continue;
			int w = textFont->getDimension(chatMsg[i].c_str()).Width;
			driver->draw2DRectangle(recti(305 * mainGame->xScale, (596 - 20 * i) * mainGame->yScale, (307 + w) * mainGame->xScale, (616 - 20 * i) * mainGame->yScale), 0xa0000000, 0xa0000000, 0xa0000000, 0xa0000000);
			textFont->draw(chatMsg[i].c_str(), rect<s32>(305 * mainGame->xScale, (595 - 20 * i) * mainGame->yScale, 1020 * mainGame->xScale, (615 - 20 * i) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(chatMsg[i].c_str(), rect<s32>(306 * mainGame->xScale, (596 - 20 * i) * mainGame->yScale, 1021 * mainGame->xScale, (616 - 20 * i) * mainGame->yScale), chatColor[chatType[i]], false, false);
		}
	}
}
void Game::ShowElement(irr::gui::IGUIElement * win, int autoframe) {
	FadingUnit fu;
	fu.fadingSize = win->getRelativePosition();
	for(auto fit = fadingList.begin(); fit != fadingList.end(); ++fit)
		if(win == fit->guiFading)
			fu.fadingSize = fit->fadingSize;
	irr::core::position2di center = fu.fadingSize.getCenter();
	fu.fadingDiff.X = fu.fadingSize.getWidth() / 10;
	fu.fadingDiff.Y = (fu.fadingSize.getHeight() - 4) / 10;
	fu.fadingUL = center;
	fu.fadingLR = center;
	fu.fadingUL.Y -= 2;
	fu.fadingLR.Y += 2;
	fu.guiFading = win;
	fu.isFadein = true;
	fu.fadingFrame = 10;
	fu.autoFadeoutFrame = autoframe;
	fu.signalAction = 0;
	if(win == wPosSelect) {
		btnPSAU->setDrawImage(false);
		btnPSAD->setDrawImage(false);
		btnPSDU->setDrawImage(false);
		btnPSDD->setDrawImage(false);
	}
	if(win == wCardSelect) {
		for(int i = 0; i < 5; ++i)
			btnCardSelect[i]->setDrawImage(false);
	}
	win->setRelativePosition(irr::core::recti(center.X, center.Y, 0, 0));
	fadingList.push_back(fu);
}
void Game::HideElement(irr::gui::IGUIElement * win, bool set_action) {
	FadingUnit fu;
	fu.fadingSize = win->getRelativePosition();
	for(auto fit = fadingList.begin(); fit != fadingList.end(); ++fit)
		if(win == fit->guiFading)
			fu.fadingSize = fit->fadingSize;
	fu.fadingDiff.X = fu.fadingSize.getWidth() / 10;
	fu.fadingDiff.Y = (fu.fadingSize.getHeight() - 4) / 10;
	fu.fadingUL = fu.fadingSize.UpperLeftCorner;
	fu.fadingLR = fu.fadingSize.LowerRightCorner;
	fu.guiFading = win;
	fu.isFadein = false;
	fu.fadingFrame = 10;
	fu.autoFadeoutFrame = 0;
	fu.signalAction = set_action;
	if(win == wPosSelect) {
		btnPSAU->setDrawImage(false);
		btnPSAD->setDrawImage(false);
		btnPSDU->setDrawImage(false);
		btnPSDD->setDrawImage(false);
	}
	if(win == wCardSelect) {
		for(int i = 0; i < 5; ++i)
			btnCardSelect[i]->setDrawImage(false);
	}
	fadingList.push_back(fu);
}
void Game::PopupElement(irr::gui::IGUIElement * element, int hideframe) {
	element->getParent()->bringToFront(element);
	dField.panel = element;
	env->setFocus(element);
	if(!hideframe)
		ShowElement(element);
	else ShowElement(element, hideframe);
}
void Game::WaitFrameSignal(int frame) {
	frameSignal.Reset();
	signalFrame = frame;
	frameSignal.Wait();
}
void Game::DrawThumb(code_pointer cp, position2di pos, std::unordered_map<int, int>* lflist) {
	const int width = 44; //standard pic size, maybe it should be defined in game.h
	const int height = 64;
	int code = cp->first;
	int lcode = cp->second.alias;
	if(lcode == 0)
		lcode = code;
	irr::video::ITexture* img = imageManager.GetTextureThumb(code);
	if(img == NULL)
		return; //NULL->getSize() will cause a crash
	dimension2d<u32> size = img->getOriginalSize();
	driver->draw2DImage(img, rect<s32>(pos.X, pos.Y, pos.X + width * mainGame->xScale, pos.Y + height * mainGame->yScale), rect<s32>(0, 0, size.Width, size.Height));

	if(lflist->count(lcode)) {
		switch((*lflist)[lcode]) {
		case 0:
			driver->draw2DImage(imageManager.tLim, recti(pos.X, pos.Y, pos.X + 20 * mainGame->xScale, pos.Y + 20 * mainGame->yScale), recti(0, 0, 64, 64), 0, 0, true);
			break;
		case 1:
			driver->draw2DImage(imageManager.tLim, recti(pos.X, pos.Y, pos.X + 20 * mainGame->xScale, pos.Y + 20 * mainGame->yScale), recti(64, 0, 128, 64), 0, 0, true);
			break;
		case 2:
			driver->draw2DImage(imageManager.tLim, recti(pos.X, pos.Y, pos.X + 20 * mainGame->xScale, pos.Y + 20 * mainGame->yScale), recti(0, 64, 64, 128), 0, 0, true);
			break;
		}
	}
}
void Game::DrawDeckBd() {
	wchar_t textBuffer[64];
	//main deck
	driver->draw2DRectangle(recti(310 * mainGame->xScale, 137 * mainGame->yScale, 410 * mainGame->xScale, 157 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(309 * mainGame->xScale, 136 * mainGame->yScale, 410 * mainGame->xScale, 157 * mainGame->yScale));
	textFont->draw(dataManager.GetSysString(1330), recti(314 * mainGame->xScale, 136 * mainGame->yScale, 409 * mainGame->xScale, 156 * mainGame->yScale), 0xff000000, false, true);
	textFont->draw(dataManager.GetSysString(1330), recti(315 * mainGame->xScale, 137 * mainGame->yScale, 410 * mainGame->xScale, 157 * mainGame->yScale), 0xffffffff, false, true);
	numFont->draw(dataManager.numStrings[deckManager.current_deck.main.size()], recti(379 * mainGame->xScale, 137 * mainGame->yScale, 439 * mainGame->xScale, 157 * mainGame->yScale), 0xff000000, false, true);
	numFont->draw(dataManager.numStrings[deckManager.current_deck.main.size()], recti(380 * mainGame->xScale, 138 * mainGame->yScale, 440 * mainGame->xScale, 158 * mainGame->yScale), 0xffffffff, false, true);
	driver->draw2DRectangle(recti(310 * mainGame->xScale, 160 * mainGame->yScale, 797 * mainGame->xScale, 436 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(309 * mainGame->xScale, 159 * mainGame->yScale, 797 * mainGame->xScale, 436 * mainGame->yScale));
	int lx;
	float dx;
	if(deckManager.current_deck.main.size() <= 40) {
		dx = 436.0f / 9;
		lx = 10;
	} else {
		lx = (deckManager.current_deck.main.size() - 41) / 4 + 11;
		dx = 436.0f / (lx - 1);
	}
	for(size_t i = 0; i < deckManager.current_deck.main.size(); ++i) {
		DrawThumb(deckManager.current_deck.main[i], position2di((314 + (i % lx) * dx)  * mainGame->xScale, (164 + (i / lx) * 68)  * mainGame->yScale), deckBuilder.filterList);
		if(deckBuilder.hovered_pos == 1 && deckBuilder.hovered_seq == (int)i)
			driver->draw2DRectangleOutline(recti((313 + (i % lx) * dx)  * mainGame->xScale, (163 + (i / lx) * 68) * mainGame->yScale, (359 + (i % lx) * dx) * mainGame->xScale, (228 + (i / lx) * 68) * mainGame->yScale));
	}
	//extra deck
	driver->draw2DRectangle(recti(310 * mainGame->xScale, 440 * mainGame->yScale, 410 * mainGame->xScale, 460 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(309 * mainGame->xScale, 439 * mainGame->yScale, 410 * mainGame->xScale, 460 * mainGame->yScale));
	textFont->draw(dataManager.GetSysString(1331), recti(314 * mainGame->xScale, 439 * mainGame->yScale, 409 * mainGame->xScale, 459 * mainGame->yScale), 0xff000000, false, true);
	textFont->draw(dataManager.GetSysString(1331), recti(315 * mainGame->xScale, 440 * mainGame->yScale, 410 * mainGame->xScale, 460 * mainGame->yScale), 0xffffffff, false, true);
	numFont->draw(dataManager.numStrings[deckManager.current_deck.extra.size()], recti(379 * mainGame->xScale, 440 * mainGame->yScale, 439 * mainGame->xScale, 460 * mainGame->yScale), 0xff000000, false, true);
	numFont->draw(dataManager.numStrings[deckManager.current_deck.extra.size()], recti(380 * mainGame->xScale, 441 * mainGame->yScale, 440 * mainGame->xScale, 461 * mainGame->yScale), 0xffffffff, false, true);
	driver->draw2DRectangle(recti(310 * mainGame->xScale, 463 * mainGame->yScale, 797 * mainGame->xScale, 533 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(309 * mainGame->xScale, 462 * mainGame->yScale, 797 * mainGame->xScale, 533 * mainGame->yScale));
	if(deckManager.current_deck.extra.size() <= 10)
		dx = 436.0f / 9;
	else dx = 436.0f / (deckManager.current_deck.extra.size() - 1);
	for(size_t i = 0; i < deckManager.current_deck.extra.size(); ++i) {
		DrawThumb(deckManager.current_deck.extra[i], position2di((314 + i * dx) * mainGame->xScale, 466 * mainGame->yScale), deckBuilder.filterList);
		if(deckBuilder.hovered_pos == 2 && deckBuilder.hovered_seq == (int)i)
			driver->draw2DRectangleOutline(recti((313 + i * dx) * mainGame->xScale, 465 * mainGame->yScale, (359 + i * dx) * mainGame->xScale, 531 * mainGame->yScale));
	}
	//side deck
	driver->draw2DRectangle(recti(310 * mainGame->xScale, 537 * mainGame->yScale, 410 * mainGame->xScale, 557 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(309 * mainGame->xScale, 536 * mainGame->yScale, 410 * mainGame->xScale, 557 * mainGame->yScale));
	textFont->draw(dataManager.GetSysString(1332), recti(314 * mainGame->xScale, 536 * mainGame->yScale, 409 * mainGame->xScale, 556 * mainGame->yScale), 0xff000000, false, true);
	textFont->draw(dataManager.GetSysString(1332), recti(315 * mainGame->xScale, 537 * mainGame->yScale, 410 * mainGame->xScale, 557 * mainGame->yScale), 0xffffffff, false, true);
	numFont->draw(dataManager.numStrings[deckManager.current_deck.side.size()], recti(379 * mainGame->xScale, 537 * mainGame->yScale, 439 * mainGame->xScale, 557 * mainGame->yScale), 0xff000000, false, true);
	numFont->draw(dataManager.numStrings[deckManager.current_deck.side.size()], recti(380 * mainGame->xScale, 538 * mainGame->yScale, 440 * mainGame->xScale, 558 * mainGame->yScale), 0xffffffff, false, true);
	driver->draw2DRectangle(recti(310 * mainGame->xScale, 560 * mainGame->yScale, 797 * mainGame->xScale, 630 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(309 * mainGame->xScale, 559 * mainGame->yScale, 797 * mainGame->xScale, 630 * mainGame->yScale));
	if(deckManager.current_deck.side.size() <= 10)
		dx = 436.0f / 9;
	else dx = 436.0f / (deckManager.current_deck.side.size() - 1);
	for(size_t i = 0; i < deckManager.current_deck.side.size(); ++i) {
		DrawThumb(deckManager.current_deck.side[i], position2di((314 + i * dx) * mainGame->xScale, 564 * mainGame->yScale), deckBuilder.filterList);
		if(deckBuilder.hovered_pos == 3 && deckBuilder.hovered_seq == (int)i)
			driver->draw2DRectangleOutline(recti((313 + i * dx) * mainGame->xScale, 563 * mainGame->yScale, (359 + i * dx) * mainGame->xScale, 629 * mainGame->yScale));
	}
	driver->draw2DRectangle(recti(805 * mainGame->xScale, 137 * mainGame->yScale, 915 * mainGame->xScale, 157 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(804 * mainGame->xScale, 136 * mainGame->yScale, 915 * mainGame->xScale, 157 * mainGame->yScale));
	textFont->draw(dataManager.GetSysString(1333), recti(809 * mainGame->xScale, 136 * mainGame->yScale, 914 * mainGame->xScale, 156 * mainGame->yScale), 0xff000000, false, true);
	textFont->draw(dataManager.GetSysString(1333), recti(810 * mainGame->xScale, 137 * mainGame->yScale, 915 * mainGame->xScale, 157 * mainGame->yScale), 0xffffffff, false, true);
	numFont->draw(deckBuilder.result_string, recti(874 * mainGame->xScale, 136 * mainGame->yScale, 934 * mainGame->xScale, 156 * mainGame->yScale), 0xff000000, false, true);
	numFont->draw(deckBuilder.result_string, recti(875 * mainGame->xScale, 137 * mainGame->yScale, 935 * mainGame->xScale, 157 * mainGame->yScale), 0xffffffff, false, true);
	driver->draw2DRectangle(recti(805 * mainGame->xScale, 160 * mainGame->yScale, 1020 * mainGame->xScale, 630 * mainGame->yScale), 0x400000ff, 0x400000ff, 0x40000000, 0x40000000);
	driver->draw2DRectangleOutline(recti(804 * mainGame->xScale, 159 * mainGame->yScale, 1020 * mainGame->xScale, 630 * mainGame->yScale));
#ifdef _IRR_ANDROID_PLATFORM_
	for(size_t i = 0; i < 7 && i + mainGame->scrFilter->getPos() < deckBuilder.results.size(); ++i) {
		code_pointer ptr = deckBuilder.results[i + mainGame->scrFilter->getPos()];
		if(deckBuilder.hovered_pos == 4 && deckBuilder.hovered_seq == (int)i)
			driver->draw2DRectangle(0x80000000, recti(806 * mainGame->xScale, (164 + i * 66) * mainGame->yScale, 1019 * mainGame->xScale, (230 + i * 66) * mainGame->yScale));
		DrawThumb(ptr, position2di(855 * mainGame->xScale, (165 + i * 66) * mainGame->yScale), deckBuilder.filterList);

		DrawThumb(ptr, position2di(855 * mainGame->xScale, (165 + i * 66) * mainGame->yScale), deckBuilder.filterList);
		if(ptr->second.type & TYPE_MONSTER) {
			myswprintf(textBuffer, L"%ls", dataManager.GetName(ptr->first));
			textFont->draw(textBuffer, recti(904 * mainGame->xScale, (164 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (185 + i * 66) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(904 * mainGame->xScale, (165 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (185 + i * 66) * mainGame->yScale), 0xffffffff, false, false);
			myswprintf(textBuffer, L"%ls/%ls \x2605%d", dataManager.FormatAttribute(ptr->second.attribute), dataManager.FormatRace(ptr->second.race), ptr->second.level);
			textFont->draw(textBuffer, recti(904 * mainGame->xScale, (186 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (207 + i * 66) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(905 * mainGame->xScale, (187 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (207 + i * 66) * mainGame->yScale), 0xffffffff, false, false);
			if(ptr->second.attack < 0 && ptr->second.defence < 0)
				myswprintf(textBuffer, L"?/?");
			else if(ptr->second.attack < 0)
				myswprintf(textBuffer, L"?/%d", ptr->second.defence);
			else if(ptr->second.defence < 0)
				myswprintf(textBuffer, L"%d/?", ptr->second.attack);
			else myswprintf(textBuffer, L"%d/%d", ptr->second.attack, ptr->second.defence);
			if(ptr->second.type & TYPE_PENDULUM) {
				wchar_t scaleBuffer[16];
				myswprintf(scaleBuffer, L" %d/%d", ptr->second.lscale, ptr->second.rscale);
				wcscat(textBuffer, scaleBuffer);
			}
			if((ptr->second.ot & 0x3) == 1)
				wcscat(textBuffer, L" [OCG]");
			else if((ptr->second.ot & 0x3) == 2)
				wcscat(textBuffer, L" [TCG]");
			textFont->draw(textBuffer, recti(904 * mainGame->xScale, (208 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (229 + i * 66) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(905 * mainGame->xScale, (209 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (229 + i * 66) * mainGame->yScale), 0xffffffff, false, false);
		} else {
			myswprintf(textBuffer, L"%ls", dataManager.GetName(ptr->first));
			textFont->draw(textBuffer, recti(904 * mainGame->xScale, (164 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (185 + i * 66) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(905 * mainGame->xScale, (165 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (185 + i * 66) * mainGame->yScale), 0xffffffff, false, false);
			const wchar_t* ptype = dataManager.FormatType(ptr->second.type);
			textFont->draw(ptype, recti(904 * mainGame->xScale, (186 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (207 + i * 66) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(ptype, recti(905 * mainGame->xScale, (187 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (207 + i * 66) * mainGame->yScale), 0xffffffff, false, false);
			textBuffer[0] = 0;
			if((ptr->second.ot & 0x3) == 1)
				wcscat(textBuffer, L"[OCG]");
			else if((ptr->second.ot & 0x3) == 2)
				wcscat(textBuffer, L"[TCG]");
			textFont->draw(textBuffer, recti(904 * mainGame->xScale, (208 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (229 + i * 66) * mainGame->yScale), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(905 * mainGame->xScale, (209 + i * 66) * mainGame->yScale, 1000 * mainGame->xScale, (229 + i * 66) * mainGame->yScale), 0xffffffff, false, false);
		}
	}
#else
	for(size_t i = 0; i < 7 && i + mainGame->scrFilter->getPos() < deckBuilder.results.size(); ++i) {
		code_pointer ptr = deckBuilder.results[i + mainGame->scrFilter->getPos()];
		if(deckBuilder.hovered_pos == 4 && deckBuilder.hovered_seq == (int)i)
			driver->draw2DRectangle(0x80000000, recti(806, 164 + i * 66, 1019, 230 + i * 66));
		DrawThumb(ptr, position2di(810, 165 + i * 66), deckBuilder.filterList);
		if(ptr->second.type & TYPE_MONSTER) {
			myswprintf(textBuffer, L"%ls", dataManager.GetName(ptr->first));
			textFont->draw(textBuffer, recti(859, 164 + i * 66, 955, 185 + i * 66), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(860, 165 + i * 66, 955, 185 + i * 66), 0xffffffff, false, false);
			myswprintf(textBuffer, L"%ls/%ls \x2605%d", dataManager.FormatAttribute(ptr->second.attribute), dataManager.FormatRace(ptr->second.race), ptr->second.level);
			textFont->draw(textBuffer, recti(859, 186 + i * 66, 955, 207 + i * 66), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(860, 187 + i * 66, 955, 207 + i * 66), 0xffffffff, false, false);
			if(ptr->second.attack < 0 && ptr->second.defence < 0)
				myswprintf(textBuffer, L"?/?");
			else if(ptr->second.attack < 0)
				myswprintf(textBuffer, L"?/%d", ptr->second.defence);
			else if(ptr->second.defence < 0)
				myswprintf(textBuffer, L"%d/?", ptr->second.attack);
			else myswprintf(textBuffer, L"%d/%d", ptr->second.attack, ptr->second.defence);
			if(ptr->second.type & TYPE_PENDULUM) {
				wchar_t scaleBuffer[16];
				myswprintf(scaleBuffer, L" %d/%d", ptr->second.lscale, ptr->second.rscale);
				wcscat(textBuffer, scaleBuffer);
			}
			if((ptr->second.ot & 0x3) == 1)
				wcscat(textBuffer, L" [OCG]");
			else if((ptr->second.ot & 0x3) == 2)
				wcscat(textBuffer, L" [TCG]");
			textFont->draw(textBuffer, recti(859, 208 + i * 66, 955, 229 + i * 66), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(860, 209 + i * 66, 955, 229 + i * 66), 0xffffffff, false, false);
		} else {
			myswprintf(textBuffer, L"%ls", dataManager.GetName(ptr->first));
			textFont->draw(textBuffer, recti(859, 164 + i * 66, 955, 185 + i * 66), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(860, 165 + i * 66, 955, 185 + i * 66), 0xffffffff, false, false);
			const wchar_t* ptype = dataManager.FormatType(ptr->second.type);
			textFont->draw(ptype, recti(859, 186 + i * 66, 955, 207 + i * 66), 0xff000000, false, false);
			textFont->draw(ptype, recti(860, 187 + i * 66, 955, 207 + i * 66), 0xffffffff, false, false);
			textBuffer[0] = 0;
			if((ptr->second.ot & 0x3) == 1)
				wcscat(textBuffer, L"[OCG]");
			else if((ptr->second.ot & 0x3) == 2)
				wcscat(textBuffer, L"[TCG]");
			textFont->draw(textBuffer, recti(859, 208 + i * 66, 955, 229 + i * 66), 0xff000000, false, false);
			textFont->draw(textBuffer, recti(860, 209 + i * 66, 955, 229 + i * 66), 0xffffffff, false, false);
		}
	}
#endif
	if(deckBuilder.is_draging) {
		DrawThumb(deckBuilder.draging_pointer, position2di(deckBuilder.dragx - 22, deckBuilder.dragy - 32), deckBuilder.filterList);
	}
}
}
