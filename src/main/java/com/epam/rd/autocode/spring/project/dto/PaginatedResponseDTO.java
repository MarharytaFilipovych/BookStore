package com.epam.rd.autocode.spring.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PaginatedResponseDTO<T> {
    private MetaDTO meta;
    @JsonIgnore
    private List<T> entities;

    @JsonProperty("clients")
    public void setClients(List<T> clients) {
        this.entities = clients;
    }

    @JsonProperty("books")
    public void setBooks(List<T> books) {
        this.entities = books;
    }

    @JsonProperty("employees")
    public void setEmployees(List<T> employees) {
        this.entities = employees;
    }

    @JsonProperty("orders")
    public void setOrders(List<T> orders) {
        this.entities = orders;
    }

    @JsonProperty("clients")
    public List<T> getClients() {
        return this.entities;
    }

    @JsonProperty("books")
    public List<T> getBooks() {
        return this.entities;
    }

    @JsonProperty("employees")
    public List<T> getEmployees() {
        return this.entities;
    }

    @JsonProperty("orders")
    public List<T> getOrders() {
        return this.entities;
    }
}
