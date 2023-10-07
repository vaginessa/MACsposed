package com.berdik.macsposed.hookers

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import dalvik.system.PathClassLoader
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.BeforeHookCallback
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker

@XposedHooker
class WifiServiceHooker {
    companion object {
        var module: XposedModule? = null

        @SuppressLint("PrivateApi")
        fun hook(param: PackageLoadedParam, module: XposedModule) {
            this.module = module
            val loadClassFromLoaderMethod = Application::class.java.getDeclaredMethod("loadClassFromLoader",
                param.classLoader.loadClass("com.android.server.SystemServiceManager"))
            module.hook(loadClassFromLoaderMethod, SystemServiceManagerHooker::class.java)
        }

        @XposedHooker
        private class SystemServiceManagerHooker : XposedInterface.Hooker {
            companion object {
                @JvmStatic
                @AfterInvocation
                fun afterInvocation(callback: AfterHookCallback, context: SystemServiceManagerHooker) {
                    if (callback.args[0] == "com.android.server.wifi.WifiService") {
                        val wifiServiceClassLoader = callback.args[1] as PathClassLoader
                        val wifiVendorHalClassLoader =  wifiServiceClassLoader
                            .loadClass("com.android.server.wifi.WifiVendorHal")

                        val setStaMacAddressMethod = Application::class.java.getDeclaredMethod("setStaMacAddress", wifiVendorHalClassLoader)
                        val setApMacAddressMethod = Application::class.java.getDeclaredMethod("setApMacAddress", wifiVendorHalClassLoader)

                        module?.hook(setStaMacAddressMethod, MacAddrSetGenericHooker::class.java)
                        module?.hook(setApMacAddressMethod, MacAddrSetGenericHooker::class.java)
                    }
                }
            }
        }

        @XposedHooker
        private class MacAddrSetGenericHooker : XposedInterface.Hooker {
            companion object {
                @JvmStatic
                @BeforeInvocation
                fun beforeInvocation(callback: BeforeHookCallback): MacAddrSetGenericHooker {
                    module?.log("BLOCKED BLOCKED BLOCKED BLOCKED")
                    callback.returnAndSkip(true)
                    return MacAddrSetGenericHooker()
                }
            }
        }
    }
}

/*class WifiServiceHooker {
    companion object {
        @SuppressLint("PrivateApi")
        fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
            findAllMethods(lpparam.classLoader.loadClass("com.android.server.SystemServiceManager")) {
                name == "loadClassFromLoader" && isStatic
            }.hookMethod {
                after { param ->
                    if (param.args[0] == "com.android.server.wifi.WifiService") {
                        hookMacAddrSet(param.args[1] as PathClassLoader)
                    }
                }
            }
        }

        @SuppressLint("PrivateApi")
        private fun hookMacAddrSet(classloader: PathClassLoader) {
            val wifiVendorHalClass = classloader.loadClass("com.android.server.wifi.WifiVendorHal")
            macAddrSetGenericHook(wifiVendorHalClass, "setStaMacAddress")
            macAddrSetGenericHook(wifiVendorHalClass, "setApMacAddress")
        }

        private fun macAddrSetGenericHook(wifiVendorHalClass: Class<*>, functionName: String) {
            findAllMethods(wifiVendorHalClass) {
                name == functionName
            }.hookMethod {
                var isHookActive = false

                before { param ->
                    // Get the active state of the hook.
                    val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID)
                    isHookActive = prefs.getBoolean("hookActive", false)

                    // If the hook is active, log a block of the MAC address change and bypass the real function.
                    if (isHookActive) {
                        XposedBridge.log("[MACsposed] Blocked MAC address change to ${param.args[1]} on ${param.args[0]}.")
                        param.result = true
                    }
                }

                after { param ->
                    // If the hook is active and the result of the address change attempt was successful, make a log entry
                    // after the real function executes indicating so.
                    if (param.result as Boolean && !isHookActive) {
                        XposedBridge.log("[MACsposed] Allowed MAC address change to ${param.args[1]} on ${param.args[0]}.")
                    }
                }
            }
        }
    }
}*/