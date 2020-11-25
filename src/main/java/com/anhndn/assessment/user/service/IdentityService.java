package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import org.springframework.stereotype.Service;

public interface IdentityService {
    IdentityEntity getByUsername(String username);
}
