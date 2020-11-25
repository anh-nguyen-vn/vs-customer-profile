package com.anhndn.assessment.user.service.impl;

import com.anhndn.assessment.user.repository.IdentityRepository;
import com.anhndn.assessment.user.repository.ProfileRepository;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.UserService;
import com.anhndn.assessment.user.service.model.IdentityModel;
import com.anhndn.assessment.user.service.model.ProfileModel;
import com.anhndn.assessment.user.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private IdentityRepository identityRepository;

    public ProfileEntity getById(Long id) {
        log.info("Finding user profile by ID [{}]", id);
        Optional<ProfileEntity> profileEntity = profileRepository.findById(id);
        log.info("Retrieved user profile with ID [{}]", id);

        return profileEntity.orElse(null);
    }

    public ProfileEntity getByEmail(String email) {
        log.info("Finding user profile by email [{}]", email);
        List<ProfileEntity> profileEntities = profileRepository.findByEmailAndIsDeleted(email, false);
        log.info("Retrieved [{}] user profile with email [{}]", profileEntities.size(), email);

        return profileEntities.size() == 0 ? null : profileEntities.get(0);
    }

    @Transactional
    public ProfileEntity createUser(ProfileModel profileModel, IdentityModel identityModel) {
        ProfileEntity profileEntity = this.createProfile(profileModel);
        this.createIdentity(profileEntity.getId(), identityModel);

        return profileEntity;
    }

    private ProfileEntity createProfile(ProfileModel model) {
        log.info("Create user profile with email [{}]", model.getEmail());
        ProfileEntity entity = this.buildUserProfileEntity(model);

        log.info("Start to save profile entity with email [{}]", model.getEmail());
        profileRepository.save(entity);
        log.info("Profile entity with email [{}] is created successfully", model.getEmail());

        return entity;
    }

    private ProfileEntity buildUserProfileEntity(ProfileModel model) {
        return ProfileEntity.builder()
                .email(model.getEmail())
                .build();
    }

    private IdentityEntity createIdentity(Long profileId, IdentityModel model) {
        log.info("create identity for user profile ID [{}] with username [{}]", profileId, model.getUsername());
        IdentityEntity entity = IdentityEntity.builder()
                .profileId(profileId)
                .userName(model.getUsername())
                .password(EncryptionUtil.sha256(model.getPassword()))
                .build();

        log.info("start to save identity for user profile ID [{}] with username [{}]", profileId, model.getUsername());
        identityRepository.save(entity);
        log.info("identity for user ID [{}] with user profile [{}] is saved successfully", profileId, model.getUsername());

        return entity;
    }
}
