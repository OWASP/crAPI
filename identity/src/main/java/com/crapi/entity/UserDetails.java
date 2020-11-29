package com.crapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Base64;

/**
 * @author Traceabel AI
 */

@Entity
@Table(name = "user_details")
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_details_generator")
    @SequenceGenerator(name="user_details_generator", sequenceName = "user_details_id_seq", allocationSize = 1)
    private long id;
    private String name;
    private String status;
    private double available_credit;
    @Lob
    private byte[] picture;

    @OneToOne
    private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAvailable_credit() {
        return available_credit;
    }

    public void setAvailable_credit(double available_credit) {
        this.available_credit = available_credit;
    }

    @JsonIgnore
    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonProperty("picture")
    public String getPhotoBase64() {
        // just assuming it is a jpeg. you would need to cater for different media types
        return "data:image/jpeg;base64," + new String(Base64.getEncoder().encode(picture));
    }
}
