package com.cloudweb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.json.JSONPropertyName;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity(name = "Bill")
public class Bill {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String created_ts;
    private String updated_ts;
    @Column(updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    @JsonProperty("owner_id")
    private UUID ownerId;
    private String vendor;
    private String bill_date;
    private String due_date;
    private double amount_due;
    @ElementCollection
    private List<String> categories;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @OneToOne
    private File attachment;

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(String created_ts) {
        this.created_ts = created_ts;
    }

    public String getUpdated_ts() {
        return updated_ts;
    }

    public void setUpdated_ts(String updated_ts) {
        this.updated_ts = updated_ts;
    }


    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getBill_date() {
        return bill_date;
    }

    public void setBill_date(String bill_date) {
        this.bill_date = bill_date;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public double getAmount_due() {
        return amount_due;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void setAmount_due(double amount_due) {
        this.amount_due = amount_due;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public File getAttachment() {
        return attachment;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", created_ts='" + created_ts + '\'' +
                ", updated_ts='" + updated_ts + '\'' +
                ", ownerId=" + ownerId +
                ", vendor='" + vendor + '\'' +
                ", bill_date='" + bill_date + '\'' +
                ", due_date='" + due_date + '\'' +
                ", amount_due=" + amount_due +
                ", categories=" + categories +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
