package com.authorization.domain.staff

data class Condition(
    val keyword: String?    = null,
    val roles:   List<Int>  = emptyList(),
)
