package com.epam.rd.autocode.spring.project.model.enums;

public enum SortableEntity {
    CLIENT, EMPLOYEE, BOOK, ORDER;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
