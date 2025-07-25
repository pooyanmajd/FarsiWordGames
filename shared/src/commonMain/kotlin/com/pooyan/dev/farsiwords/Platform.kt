package com.pooyan.dev.farsiwords

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform