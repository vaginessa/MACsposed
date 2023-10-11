package com.berdik.macsposed

import com.berdik.macsposed.hookers.SelfHooker
import com.berdik.macsposed.hookers.SystemUIHooker
import com.berdik.macsposed.hookers.WifiServiceHooker
import com.github.kyuubiran.ezxhelper.EzXHelper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MACsposed : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelper.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam != null) {
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag("MACsposed")
            EzXHelper.setToastTag("MACsposed")
            
            when (lpparam.packageName) {
                "android" -> {
                    try {
                        WifiServiceHooker.hook(lpparam)
                    } catch (e: Exception) {
                        XposedBridge.log("[MACsposed] ERROR: $e")
                    }
                }

                "com.android.systemui" -> {
                    val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID)
                    if (!prefs.getBoolean("tileRevealDone", false)) {
                        try {
                            XposedBridge.log("[MACsposed] Hooking System UI to add and reveal quick settings tile.")
                            SystemUIHooker.hook(lpparam)
                        } catch (e: Exception) {
                            XposedBridge.log("[MACsposed] ERROR: $e")
                        }
                    }
                }

                BuildConfig.APPLICATION_ID -> {
                    try {
                        XposedBridge.log("[MACsposed] Hooking self to determine if module is active.")
                        SelfHooker.hook(lpparam)
                    } catch (e: Exception) {
                        XposedBridge.log("[MACsposed] ERROR: $e")
                    }
                }
            }
        }
    }
}