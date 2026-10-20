package com.arvind.medicine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "medicine_import_jobs")
public class MedicineImportJob {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String previewJson;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected MedicineImportJob() {
    }

    public MedicineImportJob(String id, String previewJson) {
        this.id = id;
        this.previewJson = previewJson;
        this.status = "PREVIEWED";
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getPreviewJson() {
        return previewJson;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
