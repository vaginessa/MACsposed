package com.berdik.macsposed.hookers

import com.berdik.macsposed.BuildConfig
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto
import com.github.kyuubiran.ezxhelper.utils.isNotAbstract
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/*
    SelfHooker is used to hook isEnabled in XposedChecker and make it return true. XposedChecker
    is used to determine if the Xposed module is enabled or not.
 */
class SelfHooker {
    companion object {
        fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
            findAllMethods(lpparam.classLoader.loadClass("${BuildConfig.APPLICATION_ID}.XposedChecker\$Companion")) {
                name == "isEnabled"
            }.hookMethod {
                before { param ->
                    param.result = true
                }
            }
        }
    }
}