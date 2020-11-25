package com.anhndn.assessment.user.service.impl;

import com.anhndn.assessment.user.repository.IdentityRepository;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.service.IdentityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class IdentityServiceImpl implements IdentityService {

    @Autowired
    private IdentityRepository identityRepository;

    public IdentityEntity getByUsername(String username) {
        log.info("Finding user identity by username [{}]", username);
        List<IdentityEntity> identityEntities = identityRepository.findByUserNameAndIsDeleted(username, false);
        log.info("Retrieved [{}] user identity with username [{}]", identityEntities.size(), username);

        return identityEntities.size() == 0 ? null : identityEntities.get(0);
    }

}
