package com.nanoit.hub.r2dbc.entity.embed

data class Address(
    var type: String? = null,
    var country: String? = null,
    var postalCode: String? = null,
    var region: String? = null,
    var poBox: String? = null,
    var city: String? = null,
    var street: String? = null,
)
