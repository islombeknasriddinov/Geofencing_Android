package com.example.geofencing.model

import java.io.Serializable

class Message(
    var enter: String? = null,
    var dwell: String? = null,
    var exit: String? = null,
    var radius: Float? = null
) : Serializable