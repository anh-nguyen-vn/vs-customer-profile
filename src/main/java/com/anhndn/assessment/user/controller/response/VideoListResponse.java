package com.anhndn.assessment.user.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VideoListResponse {
    private PageResponse page;
    private List<Data> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Data {
        private Long id;
        private String url;
        private Long sharedByUserId;
        private String sharedByUserUsername;
        private String title;
        private String description;
        private Long votedUpCount;
        private Long votedDownCount;
        private Date createdTimestamp;
    }
}
