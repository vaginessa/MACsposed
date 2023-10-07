package com.berdik.macsposed

import com.berdik.macsposed.hookers.SystemUIHooker
import com.berdik.macsposed.hookers.WifiServiceHooker
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

private lateinit var module: MACsposed

class MACsposed(base: XposedInterface, param: ModuleLoadedParam) : XposedModule(base, param) {
    init {
        module = this
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        super.onPackageLoaded(param)

        if (param != null) {
            module.log("[MACsposed] SAW ${param.packageName}")

            when (param.packageName) {
                "android" -> {
                    try {
                        WifiServiceHooker.hook(param, module)
                    } catch (e: Exception) {
                        module.log("[MACsposed] ERROR: $e")
                    }
                }

                "com.android.systemui" -> {
                    // TODO: Add logic for hooking the system UI.
                }
            }
        }
    }

    /*override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam != null) {
            when (lpparam.packageName) {
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
            }
        }
    }*/
}