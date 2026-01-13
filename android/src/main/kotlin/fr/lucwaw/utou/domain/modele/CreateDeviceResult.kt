package fr.lucwaw.utou.domain.modele

import utou.v1.Common

data class CreateDeviceResult(
    val status: Common.StatusCode,
    val message: String
)
