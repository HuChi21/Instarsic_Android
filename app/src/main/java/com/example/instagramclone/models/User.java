package com.example.instagramclone.models;

public class User {
    String uid,email, username ,password, fullname, imageUrl, phonenumber;
    Long birthdate, created, signedin;
    String token;

    public User() {

    }

    public User(String uid,String email, String username, String password, String fullname, String imageUrl, String phoneNumber, Long birthday, Long signupdate, Long lastsignin,String token) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.imageUrl = imageUrl;
        this.phonenumber = phoneNumber;
        this.birthdate = birthday;
        this.created = signupdate;
        this.signedin = lastsignin;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Long birthdate) {
        this.birthdate = birthdate;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getSignedin() {
        return signedin;
    }

    public void setSignedin(Long signedin) {
        this.signedin = signedin;
    }
}
