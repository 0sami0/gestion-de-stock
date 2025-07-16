package com.fsr.gestion_de_stock;

import java.util.List;

public class User {
    private final int id;
    private final String username;
    private List<String> roles; // Changed from a single role

    public User(int id, String username, List<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    @Override
    public String toString() {
        return username; // The list view will now just show the name
    }
}