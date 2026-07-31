package com.cvezga.gatecontroller.model;

/**
 * Form model used to bind login credentials submitted by the login page.
 */
public class LoginForm {

    private String user;
    private String password;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginForm{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
