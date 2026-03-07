package com.mariaribeiro.nexo.identity.adapters.in.rest;

import java.util.Map;

public record AuthValidationErrorResponse(String message, Map<String, String> errors) {
}

