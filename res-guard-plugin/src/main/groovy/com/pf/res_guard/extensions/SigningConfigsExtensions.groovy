package com.pf.res_guard.extensions

class SigningConfigsExtensions {
    def keyAlias
    def keyPassword
    def storeFile
    def storePassword

    @Override
    public String toString() {
        return "SigningConfigsExtensions{" +
                "keyAlias=" + keyAlias +
                ", keyPassword=" + keyPassword +
                ", storeFile=" + storeFile +
                ", storePassword=" + storePassword +
                '}';
    }
}