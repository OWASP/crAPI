package com.crapi.model;

import lombok.Data;

@Data
public class DashboardResponse {
    private long id;
    private String name;

    private String email;

    private String number;
    private String picture_url;
    private String video_url;
    private String video_name;
    private double available_credit;
    private long video_id;

    private String role;



    public DashboardResponse(Long id,String name, String email,String number, String role, double available_credit){
        this.id =id;
        this.name = name;
        this.email=email;
        this.number = number;
        this.role = role;
        this.available_credit=available_credit;


    }
}
