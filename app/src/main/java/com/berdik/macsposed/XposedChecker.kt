package com.berdik.macsposed

class XposedChecker {
    companion object {
        private var isEnabled = false

        fun flagAsEnabled() {
            isEnabled = true
        }

        fun isEnabled(): Boolean {
            return isEnabled
        }
    }
}