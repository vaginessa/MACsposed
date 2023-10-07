package com.berdik.macsposed

/*
    XposedChecker is used to detect if this module is enabled. The isEnabled() function is
    hard-coded here to return false. When this module is enabled, SelfHooker hooks isEnabled()
    and makes it return true.
 */
class XposedChecker {
    companion object {
        // If Xposed is enabled, SelfHooker will hook this function and force it to return true.
        fun isEnabled(): Boolean {
            return false
        }
    }
}