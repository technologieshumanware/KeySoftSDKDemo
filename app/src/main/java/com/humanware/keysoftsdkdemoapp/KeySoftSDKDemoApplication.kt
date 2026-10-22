package com.humanware.keysoftsdkdemoapp

import android.app.Application
import com.humanware.keysoftsdk.contextmenu.WriteCommandsXmlFileToInternalMemoryStorageExecutor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KeySoftSDKDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //pass the commands.xml file to KeySoft
        WriteCommandsXmlFileToInternalMemoryStorageExecutor(this).execute()
    }
}