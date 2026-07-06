package dev.esteki.ipulse.data.local

import android.content.Context

object AndroidContextHolder {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
