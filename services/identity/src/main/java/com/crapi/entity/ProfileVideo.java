package com.crapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Base64;


/**
 * @author Traceabel AI
 */
@Entity
@Table(name = "profile_Video")
public class ProfileVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String video_name;
    private String conversion_params="-v codec h264";
    @Lob
    private byte[] video;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public ProfileVideo(String video_name, User user){
        this.video_name = video_name;
        this.user = user;

    }


    //constructor for new object
    public ProfileVideo(String video_name,byte[] file, User user){
        this.video_name = video_name;
        this.user = user;
        this.video = file;
    }

    public ProfileVideo(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVideo_name() {
        return video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public String getConversion_params() {
        return conversion_params;
    }

    public void setConversion_params(String conversion_params) {
        this.conversion_params = conversion_params;
    }

    @JsonIgnore
    public byte[] getVideo() {
        return video;
    }

    public void setVideo(byte[] video) {
        this.video = video;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    @JsonProperty("profileVideo")
    public String getVideoBase64() {
        // just assuming it is a jpeg. you would need to cater for different media types
        return "data:image/jpeg;base64," + new String(Base64.getEncoder().encode(video));
    }
}
