package lk.ceylontravel.model;

import java.time.LocalDate;

/**
 * Function: Guide Calendar Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class Availability {
    private Long id;
    private Long guideId;
    private LocalDate availableDate;
    private String status; // 'AVAILABLE' or 'BOOKED'

    public Availability() {}

    public Availability(Long id, Long guideId, LocalDate availableDate, String status) {
        this.id = id;
        this.guideId = guideId;
        this.availableDate = availableDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGuideId() { return guideId; }
    public void setGuideId(Long guideId) { this.guideId = guideId; }

    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
