package com.anhndn.assessment.user.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VideoModel {
    private Long id;
    private String url;
    private Long sharedByUserId;
    private String sharedByUserUsername;
    private Long votedUpCount;
    private Long votedDownCount;
    private String title;
    private String description;
}
