package com.epam.rd.autocode.spring.project.model.enums;

public enum Role {
    CLIENT,
    EMPLOYEE;

    @Override
    public String toString(){
        return "ROLE_" + this.name();
    }

    public String getSimpleName() {
        return this.name();
    }

    public static Role fromString(String role) {
        if (role == null) return null;

        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}