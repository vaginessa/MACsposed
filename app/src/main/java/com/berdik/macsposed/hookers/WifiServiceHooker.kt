package com.berdik.macsposed.hookers

import android.annotation.SuppressLint
import android.net.MacAddress
import com.berdik.macsposed.BuildConfig
import dalvik.system.PathClassLoader
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerLoadedParam
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
class WifiServiceHooker {
    companion object {
        var module: XposedModule? = null

        @SuppressLint("PrivateApi")
        fun hook(param: SystemServerLoadedParam, module: XposedModule) {
            this.module = module
            module.hook(
                param.classLoader.loadClass("com.android.server.SystemServiceManager")
                    .getDeclaredMethod("loadClassFromLoader", String::class.java, ClassLoader::class.java),
                SystemServiceManagerHooker::class.java
            )
        }

        @XposedHooker
        private class SystemServiceManagerHooker(private val wifiService: PathClassLoader?) : XposedInterface.Hooker {
            companion object {
                @JvmStatic
                @BeforeInvocation
                fun beforeInvocation(callback: BeforeHookCallback): SystemServiceManagerHooker {
                    val className = callback.args[0] as String
                    if (className == "com.android.server.wifi.WifiService") {
                        val classLoader = callback.args[1] as PathClassLoader
                        return SystemServiceManagerHooker(classLoader)
                    }

                    return SystemServiceManagerHooker(null)
                }

                @SuppressLint("PrivateApi")
                @JvmStatic
                @AfterInvocation
                fun afterInvocation(callback: AfterHookCallback, context: SystemServiceManagerHooker) {
                    if (context.wifiService != null) {
                        val wifiVendorHalClassLoader = context.wifiService.loadClass("com.android.server.wifi.WifiVendorHal")
                        val setStaMacAddressMethod = wifiVendorHalClassLoader
                            .getDeclaredMethod("setStaMacAddress", String::class.java, MacAddress::class.java)
                        val setApMacAddressMethod = wifiVendorHalClassLoader
                            .getDeclaredMethod("setApMacAddress", String::class.java, MacAddress::class.java)

                        module?.hook(setStaMacAddressMethod, MacAddrSetGenericHooker::class.java)
                        module?.hook(setApMacAddressMethod, MacAddrSetGenericHooker::class.java)
                    }
                }
            }
        }

        @XposedHooker
        private class MacAddrSetGenericHooker(private val isHookActive: Boolean) : XposedInterface.Hooker {
            companion object {
                @JvmStatic
                @BeforeInvocation
                fun beforeInvocation(callback: BeforeHookCallback): MacAddrSetGenericHooker {
                    val prefs = module?.getRemotePreferences(BuildConfig.APPLICATION_ID)
                    var isHookActive = prefs?.getBoolean("hookActive", false)

                    if (isHookActive!!) {
                        module?.log("[MACsposed] Blocked MAC address change to ${callback.args[1]} on ${callback.args[0]}.")
                        callback.returnAndSkip(true)
                    }

                    return MacAddrSetGenericHooker(isHookActive)
                }

                @JvmStatic
                @AfterInvocation
                fun AfterInvocation(callback: AfterHookCallback, context: MacAddrSetGenericHooker) {
                    if (!context.isHookActive) {
                        module?.log("[MACsposed] Allowed MAC address change to ${callback.args[1]} on ${callback.args[0]}.")
                    }
                }
            }
        }
    }
}