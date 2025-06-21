package dev.onelenyk.pprominec

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform