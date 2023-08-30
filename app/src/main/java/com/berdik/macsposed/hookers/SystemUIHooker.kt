package com.berdik.macsposed.hookers

import android.annotation.SuppressLint
import android.util.ArraySet
import com.berdik.macsposed.BuildConfig
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Modifier

class SystemUIHooker {
    companion object {
        private const val tileId = "custom(${BuildConfig.APPLICATION_ID}/.QuickTile)"
        private var tileRevealed = false

        @SuppressLint("PrivateApi")
        fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
            MethodFinder.fromClass(lpparam.classLoader.loadClass("com.android.systemui.qs.QSPanelControllerBase"))
                .filterByName("setTiles")
                .filterIncludeModifiers(Modifier.PUBLIC)
                .filterByParamCount(0)
                .first()
                .createHook {
                    before { param ->
                        if (!tileRevealed) {
                            val tileHost = XposedHelpers.getObjectField(param.thisObject, "mHost")

                            /*
                            According to the AOSP 13 source code, the addTile function was not changed,
                            however, it was not hooking properly on the Android 13 Pixel 4a August 2022
                            Factory Image. Listing the declared methods of the class revealed that the
                            function's parameters had been reversed in the shipped image even though
                            AOSP did not reflect this. To account for this discrepancy, we try calling
                            the Android 13 Pixel variant first, and if it fails, we fall back to the
                            other variant. Ideally, we would check the API version here with a proper
                            conditional, but since it is possible that Android 13 builds will use the
                            old variant of the function, this uglier but safer approach is used instead.
                             */
                            try {
                                // Used by Android 13 Pixel 4a August 2022 Factory Image.
                                XposedHelpers.callMethod(tileHost, "addTile", -1, tileId)
                            }
                            catch (t: Throwable) {
                                // Used by Android 12, and possibly some Android 13 distros.
                                XposedHelpers.callMethod(tileHost, "addTile", tileId)
                            }

                            XposedBridge.log("[MACsposed] Tile added to quick settings panel.")
                        }
                    }
                }

            // Properly fixing the unchecked cast warning with Kotlin adds more performance overhead than it is worth,
            // so we are suppressing the warning instead.
            @Suppress("UNCHECKED_CAST")
            MethodFinder.fromClass(lpparam.classLoader.loadClass("com.android.systemui.qs.QSTileRevealController\$1"))
                .filterByName("run")
                .first()
                .createHook {
                    before { param ->
                        if (!tileRevealed) {
                            val tilesToReveal = XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject),
                                "mTilesToReveal") as ArraySet<String>
                            tilesToReveal.add(tileId)
                            tileRevealed = true
                            XposedBridge.log("[MACsposed] Tile quick settings panel animation played. " +
                                    "MACsposed will not hook SystemUI on next reboot.")
                        }
                    }
                }
        }
    }
}