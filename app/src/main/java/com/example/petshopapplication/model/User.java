package com.example.petshopapplication.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String dateOfBirth;
    private boolean isDeleted;
    private int roleId;
    private Date createdAt;
    private String avatar;
}
