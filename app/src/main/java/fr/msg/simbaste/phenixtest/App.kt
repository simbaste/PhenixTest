package fr.msg.simbaste.phenixtest

import android.app.Application
import android.content.Context

class App: Application() {
    companion object {
        private lateinit var context: Context

        fun getAppContext(): Context = App.context
    }

    override fun onCreate() {
        super.onCreate()
        App.context = applicationContext;
    }
}