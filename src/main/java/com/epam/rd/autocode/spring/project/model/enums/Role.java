package com.epam.rd.autocode.spring.project.model.enums;

public enum Role {
    CLIENT("ROLE_CLIENT"),
    EMPLOYEE("ROLE_EMPLOYEE");

    private final String role;
    Role(String roleEmployee) {
        this.role = roleEmployee;
    }

    @Override
    public String toString(){
        return role;
    }
}
