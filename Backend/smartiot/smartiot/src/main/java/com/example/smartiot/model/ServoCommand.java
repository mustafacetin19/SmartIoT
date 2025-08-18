package com.example.smartiot.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** (Opsiyonel) JSON body ile servo kontrolünde kullanılabilir. */
@Data
@Schema(description = "Servo komutu")
public class ServoCommand {

    @Schema(description = "Servo cihazın görünen adı / veya ID referansı", example = "SERVO-1", nullable = true)
    private String servo;

    @Schema(description = "Açı (0-180)", example = "45", minimum = "0", maximum = "180")
    private int angle;
}
