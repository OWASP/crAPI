package com.crapi.model;

import com.crapi.entity.VehicleModel;
import lombok.Data;

import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Traceabel AI
 */

@Data
public class VehicleForm {

    @NotBlank
    @Size(min = 4, max = 8)
    private String pincode;
    @NotBlank
    @Size(min = 4, max = 20)
    private String vin;

}
