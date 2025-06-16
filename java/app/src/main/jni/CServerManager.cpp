#include "CServerManager.h"
#include "obfuscate/str_obfuscate.hpp"

const CServerInstance::CServerInstanceEncrypted g_sEncryptedAddresses[MAX_SERVERS] = {
    CServerInstance::create(OBFUSCATE("94.23.168.153"), 1, 16, 1410, false),  // 1
	CServerInstance::create(OBFUSCATE("77.105.146.144"), 1, 16, 7777, false)  // 2
};