package com.example.womapp.Objects;


public class User{

    private String email;
    private String user_id;
    private String username;
    private String sesso;
    private String password;
    private String age_range;

    public User(String email, String user_id, String username, String sesso, String password, String age_range) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.sesso = sesso;
        this.password = password;
        this.age_range = age_range;
    }
    public User() {
    }

    public String getAge_range() {
        return age_range;
    }

    public void setAge_range(String age_range) {
        this.age_range = age_range;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", sesso='" + sesso + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

