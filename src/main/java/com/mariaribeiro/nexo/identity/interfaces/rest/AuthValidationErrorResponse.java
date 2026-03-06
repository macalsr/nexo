package com.mariaribeiro.nexo.identity.interfaces.rest;

import java.util.Map;

public record AuthValidationErrorResponse(String message, Map<String, String> errors) {
}
