package io.neoply.neopinconnect.exception

import com.google.gson.JsonParseException

class RequiredFieldException(field: String = "") : JsonParseException("'$field' is required")