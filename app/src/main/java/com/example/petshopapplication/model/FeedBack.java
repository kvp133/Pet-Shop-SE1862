package com.example.petshopapplication.model;

import java.io.Serializable;
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
public class FeedBack implements Serializable {
    private String id;
    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String imageUrl;
    private String content;
    private String createdAt;
    private boolean isDeleted;
}
