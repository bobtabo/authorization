package com.authorization.domain.client

import java.time.LocalDateTime

data class Condition(
    val keyword:   String?          = null,
    val startFrom: LocalDateTime?   = null,
    val startTo:   LocalDateTime?   = null,
    val statuses:  List<Int>        = emptyList(),
)
