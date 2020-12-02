package com.crapi.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */

@Data
public class VideoForm {


    private long id;
    private String videoName;
    private String video_url;
    private String conversion_params;
}
