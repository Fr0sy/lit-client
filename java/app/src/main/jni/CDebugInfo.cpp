#include "CDebugInfo.h"

#include "main.h"
#include "gui/gui.h"
#include "game/game.h"
#include "net/netgame.h"
#include "game/RW/RenderWare.h"
#include "chatwindow.h"
#include "playertags.h"
#include "dialog.h"
#include "keyboard.h"
#include "CSettings.h"
#include "util/armhook.h"

extern CGUI* pGUI;
extern CGame* pGame;
extern CChatWindow* pChatWindow;
extern CSettings* pSettings;

uint32_t CDebugInfo::uiStreamedPeds = 0;
uint32_t CDebugInfo::uiStreamedVehicles = 0;
uint32_t CDebugInfo::m_uiDrawDebug = 0;
float CDebugInfo::FPS = 0.0f;
uint32_t CDebugInfo::m_dwSpeedMode = 0;
uint32_t CDebugInfo::m_dwSpeedStart = 0;

void CDebugInfo::ToggleDebugDraw()
{
	m_uiDrawDebug ^= 1;
}

void CDebugInfo::Draw()
{
	char szStr[256];
	char szStrPr[256];
	char szStrMem[256];
	char szStrPos[256];
	ImVec2 pos;
	if (m_uiDrawDebug)
	{
	                  *(uint8_t*)(g_libGTASA + 0x8ED875) = 1; // gtasa

	                  uint32_t msUsed = *(uint32_t*)(g_libGTASA + 0x0067067C); // gtasa1
	                  uint32_t msAvailable = *(uint32_t*)(g_libGTASA + 0x005DE734);
		float percentUsed = ((float)msUsed/(float)msAvailable)*100;
		snprintf(&szStrMem[0], 256, "MEM: %.1f/%.1f (%.1f %%)",
				 (float)msUsed/ (1024*1024),
				 (float)msAvailable / (1024*1024),
				 percentUsed
				 );
		pos = ImVec2(pGUI->ScaleX(40.0f), pGUI->ScaleY(1080.0f - pGUI->GetFontSize() * 10));

		pGUI->RenderText(pos, (ImU32)0xFFFFFFFF, true, &szStrMem[0]);

		if (pGame->FindPlayerPed()->m_pPed)
		{
                                                      MATRIX4X4 matFromPlayer;
                                                      CPlayerPed *pLocalPlayerPed = pGame->FindPlayerPed();
                                                      pLocalPlayerPed->GetMatrix(&matFromPlayer);

			snprintf(&szStrPos[0], 256, "POS: %.4f, %.4f, %.4f", matFromPlayer.pos.X, matFromPlayer.pos.Y, matFromPlayer.pos.Z);
			pos = ImVec2(pGUI->ScaleX(40.0f), pGUI->ScaleY(1080.0f - pGUI->GetFontSize() * 8));
			pGUI->RenderText(pos, (ImU32)0xFFFFFFFF, true, &szStrPos[0]);

	                                    snprintf(&szStr[0], 256, "STREAMED PEDS: %d, STREAMED VEHICLES: %d", uiStreamedPeds, uiStreamedVehicles);
		                  pos = ImVec2(pGUI->ScaleX(40.0f), pGUI->ScaleY(1080.0f - pGUI->GetFontSize() * 12));
	                                    pGUI->RenderText(pos, (ImU32)0xFFFFFFFF, true, &szStr[0]);

	                                   snprintf(&szStrPr[0], 256, "FPS: %f", FPS);
		                 pos = ImVec2(pGUI->ScaleX(40.0f), pGUI->ScaleY(1080.0f - pGUI->GetFontSize() * 6));
	                                   pGUI->RenderText(pos, (ImU32)0xFF00FFFF, true, &szStrPr[0]);
		}
	}
    else {
        *(uint8_t*)(g_libGTASA + 0x8ED875) = 0;
    }
}

void CDebugInfo::ApplyDebugPatches()
{
#ifdef DEBUG_INFO_ENABLED

	UnFuck(g_libGTASA + 0x008B8018);
	*(uint8_t*)(g_libGTASA + 0x008B8018) = 1;
	NOP(g_libGTASA + 0x00399EDA, 2);
	NOP(g_libGTASA + 0x00399F46, 2);
	NOP(g_libGTASA + 0x00399F92, 2);

#endif
}

void CDebugInfo::ToggleSpeedMode()
{
	m_dwSpeedMode ^= 1;
	if (m_dwSpeedMode)
	{
		pChatWindow->AddDebugMessage("Speed mode enabled");
	}
	else
	{
		pChatWindow->AddDebugMessage("Speed mode disabled");
	}
}

void CDebugInfo::ProcessSpeedMode(VECTOR* pVecSpeed)
{
	if (!m_dwSpeedMode)
	{
		return;
	}
	static uint32_t m_dwState = 0;
	float speed = sqrt((pVecSpeed->X * pVecSpeed->X) + (pVecSpeed->Y * pVecSpeed->Y) + (pVecSpeed->Z * pVecSpeed->Z)) * 2.0f * 100.0f;
	if (speed >= 1.0f)
	{
		if (!m_dwSpeedStart)
		{
			m_dwSpeedStart = GetTickCount();
			m_dwState = 0;
			pChatWindow->AddDebugMessage("Start");
		}
		if ((speed >= 119.0f) && (speed <= 121.0f) && (m_dwState == 0))
		{
			pChatWindow->AddDebugMessage("1 to 100: %d", GetTickCount() - m_dwSpeedStart);
			m_dwSpeedStart = GetTickCount();
			m_dwState = 1;
		}
		if ((speed >= 230.0f) && (speed <= 235.0f) && (m_dwState == 1))
		{
			pChatWindow->AddDebugMessage("100 to 200: %d", GetTickCount() - m_dwSpeedStart);
			m_dwSpeedStart = 0;
			m_dwState = 0;
		}
		// process for 100 and 200
	}
	else
	{
		if (m_dwSpeedStart)
		{
			m_dwSpeedStart = 0;
			m_dwState = 0;
			pChatWindow->AddDebugMessage("Reseted");
			return;
		}
	}
}
