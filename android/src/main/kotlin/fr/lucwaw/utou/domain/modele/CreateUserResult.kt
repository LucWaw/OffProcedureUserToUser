package fr.lucwaw.utou.domain.modele

import utou.v1.Common

data class CreateUserResult(
    val status: Common.StatusCode,
    val userId: String?,
    val message: String,
    val registerDeviceStatus: Common.StatusCode,
    val registerDeviceMessage: String
)
