package com.holidaymessenger.data.db.entity

enum class Channel {
    SMS,
    WHATSAPP
}

enum class MessageType {
    HOLIDAY,
    BIRTHDAY,
    RECURRING
}

enum class Frequency {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class MessageStatus {
    PENDING,
    SENT,
    FAILED
}

enum class Category {
    HOLIDAY,
    BIRTHDAY,
    RECURRING
}
