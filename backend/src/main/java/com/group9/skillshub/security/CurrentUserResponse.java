package com.group9.skillshub.security;

import com.group9.skillshub.organizations.model.Role;

public record CurrentUserResponse(int personId, String personName, Role role, boolean isAdmin) {
}