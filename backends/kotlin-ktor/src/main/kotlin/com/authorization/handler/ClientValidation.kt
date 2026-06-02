/*
 * クライアントリクエストバリデーションモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.pattern

private val telPattern = Regex("""\d{10,11}""")
private val emailPattern = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

data class StoreClientBody(
    val name: String = "",
    val postCode: String = "",
    val pref: String = "",
    val city: String = "",
    val address: String = "",
    val building: String? = null,
    val tel: String = "",
    val email: String = "",
)

data class UpdateClientBody(
    val name: String? = null,
    val postCode: String? = null,
    val pref: String? = null,
    val city: String? = null,
    val address: String? = null,
    val building: String? = null,
    val tel: String? = null,
    val email: String? = null,
    val status: Int? = null,
)

val storeClientValidation = Validation<StoreClientBody> {
    StoreClientBody::name required {
        maxLength(255)
    }
    StoreClientBody::postCode required {
        maxLength(8)
    }
    StoreClientBody::pref required {
        maxLength(50)
    }
    StoreClientBody::city required {
        maxLength(100)
    }
    StoreClientBody::address required {
        maxLength(255)
    }
    StoreClientBody::building ifPresent {
        maxLength(255)
    }
    StoreClientBody::tel required {
        pattern(telPattern)
    }
    StoreClientBody::email required {
        pattern(emailPattern)
        maxLength(255)
    }
}

val updateClientValidation = Validation<UpdateClientBody> {
    UpdateClientBody::name ifPresent {
        maxLength(255)
    }
    UpdateClientBody::postCode ifPresent {
        maxLength(8)
    }
    UpdateClientBody::pref ifPresent {
        maxLength(50)
    }
    UpdateClientBody::city ifPresent {
        maxLength(100)
    }
    UpdateClientBody::address ifPresent {
        maxLength(255)
    }
    UpdateClientBody::building ifPresent {
        maxLength(255)
    }
    UpdateClientBody::tel ifPresent {
        pattern(telPattern)
    }
    UpdateClientBody::email ifPresent {
        pattern(emailPattern)
        maxLength(255)
    }
}
