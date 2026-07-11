package com.fuelnivo.app.util

import java.util.UUID

/** Locally generated identifiers. No network or account is involved. */
object Ids {
    fun newId(): String = UUID.randomUUID().toString()
}
