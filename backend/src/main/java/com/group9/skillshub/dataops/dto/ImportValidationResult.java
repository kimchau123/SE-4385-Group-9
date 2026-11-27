package com.group9.skillshub.dataops.dto;

import java.util.List;

public record ImportValidationResult(boolean valid, int rowCount, List<String> errors) {
}
