package com.mechat.app.ui.navigation

/** All navigation routes. */
object NavRoutes {
    const val Onboarding = "onboarding"
    const val SetPin = "set_pin"
    const val Lock = "lock"
    const val ChatList = "chat_list"
    const val Chat = "chat/{id}"
    const val AddContact = "add_contact"
    const val ScanQr = "scan_qr"
    const val Contacts = "contacts"
    const val Settings = "settings"
    const val Call = "call"

    fun chat(id: String) = "chat/$id"
}
