package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.model.IdentityModel;
import com.anhndn.assessment.user.service.model.ProfileModel;

public interface UserService {
    ProfileEntity getById(Long id);
    ProfileEntity getByEmail(String email);
    ProfileEntity createUser(ProfileModel profileModel, IdentityModel identityModel);
}
