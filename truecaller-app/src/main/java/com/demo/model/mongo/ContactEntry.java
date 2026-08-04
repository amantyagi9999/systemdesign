package com.demo.model.mongo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEntry {

    private String phoneNumber;            // E.164 format, e.g. +919876543210
    private String savedName;         // Name as saved by the uploading user
    private String photoUrl;          // Optional
    private String reportedCategory;  // Optional: SPAM / SCAM / TELEMARKETER if flagged at upload time
}
