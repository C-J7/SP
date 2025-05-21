package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Busy Hour DTO")
public class BusyHourDTO {
    @NotNull
    @Schema(description = "Start time of busy hour")
    private LocalDateTime start;
    @NotNull
    @Schema(description = "End time of busy hour")
    private LocalDateTime end;

    //getters and setters
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
}
