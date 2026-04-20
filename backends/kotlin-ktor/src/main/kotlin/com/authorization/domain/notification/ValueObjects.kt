package com.authorization.domain.notification

data class Page(
    val items:      List<Notification>,
    val nextCursor: String?,
)
