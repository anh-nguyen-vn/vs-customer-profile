package com.anhndn.assessment.user.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "video")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VideoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "shared_by_user_id")
    private Long sharedByUserId;

    @Column(name = "shared_by_user_username")
    private String sharedByUserUsername;

    @Column(name = "voted_up_count")
    private Long votedUpCount;

    @Column(name = "voted_down_count")
    private Long votedDownCount;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_timestamp")
    private Date createdTimestamp;

    @Column(name = "last_updated_timestamp")
    private Date lastUpdatedTimestamp;

    @PrePersist
    public void preInsert() {
        this.isDeleted = false;
        Date createdDate = new Date();
        this.createdTimestamp = createdDate;
        this.lastUpdatedTimestamp = createdDate;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedTimestamp = new Date();
    }
}
