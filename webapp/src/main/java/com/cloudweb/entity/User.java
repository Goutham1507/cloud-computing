package com.cloudweb.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "user")
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String first_Name;
    private String last_Name;
    private String password;
    private String emailAddress;
    private String account_created;
    private String account_updated;
    @Transient
    private String accessToken;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirst_Name() {
        return first_Name;
    }

    public void setFirst_Name(String first_Name) {
        this.first_Name = first_Name;
    }

    public String getLast_Name() {
        return last_Name;
    }

    public void setLast_Name(String last_Name) {
        this.last_Name = last_Name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAccount_created() {
        return account_created;
    }

    public void setAccount_created(String account_cretaed) {
        this.account_created = account_cretaed;
    }

    public String getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(String account_updated) {
        this.account_updated = account_updated;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", first_Name='" + first_Name + '\'' +
                ", last_Name='" + last_Name + '\'' +
                ", password='" + password + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", account_cretaed='" + account_created + '\'' +
                ", account_updated='" + account_updated + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
