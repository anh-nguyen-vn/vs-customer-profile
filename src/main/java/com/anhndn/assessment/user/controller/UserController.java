package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.aspect.LogAround;
import com.anhndn.assessment.user.controller.request.UserCreateRequest;
import com.anhndn.assessment.user.controller.response.DefaultCreateResponse;
import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.exception.RsaDecryptException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.IdentityService;
import com.anhndn.assessment.user.service.RsaDecryptService;
import com.anhndn.assessment.user.service.UserService;
import com.anhndn.assessment.user.service.model.IdentityModel;
import com.anhndn.assessment.user.service.model.ProfileModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RsaDecryptService rsaDecryptService;

    @Autowired
    private ResponseFactory responseFactory;

    @LogAround(message = "create user")
    @PostMapping(value = "v${api.version}/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GeneralResponse<DefaultCreateResponse>> createUserProfile(@Valid @RequestBody UserCreateRequest request) {
        log.info("create user with email [{}]", request.getProfile().getEmail());

        this.validateEmailDuplicated(request.getProfile().getEmail());
        ProfileModel profileModel = ProfileModel.builder()
                .email(request.getProfile().getEmail())
                .build();

        String plainPassword = this.validateEncryptedPassword(request.getIdentity().getPassword());
        this.validateIdentityDuplicated(request.getIdentity().getUsername());

        IdentityModel identityModel = IdentityModel.builder()
                .username(request.getIdentity().getUsername())
                .password(plainPassword)
                .build();


        ProfileEntity createdUserProfileEntity = userService.createUser(profileModel, identityModel);
        log.info("User profile with email [{}] is created with ID [{}]", request.getProfile().getEmail(), createdUserProfileEntity.getId());

        ResponseEntity success = responseFactory.success(new DefaultCreateResponse(createdUserProfileEntity.getId()), DefaultCreateResponse.class);
        return success;
    }

    private String validateEncryptedPassword(String encryptedPassword) {
        try {
            String plainPassword = rsaDecryptService.decrypt(encryptedPassword);
            if (StringUtils.isEmpty(plainPassword)) {
                log.error("Password is empty");
                throw new BadRequestClientErrorException(ResponseStatus.PASSWORD_REQUIRED);
            }

            return plainPassword;
        } catch (RsaDecryptException ex) {
            log.error("Can not decrypt password", ex);
            throw new BadRequestClientErrorException(ResponseStatus.RSA_DECRYPT_ERROR);
        }
    }

    private boolean validateEmailDuplicated(String email) {
        ProfileEntity profileEntity = userService.getByEmail(email);
        if (profileEntity != null) {
            log.error("Email [{}] is duplicated", email);
            throw new BadRequestClientErrorException(ResponseStatus.EMAIL_DUPLICATED);
        }

        return true;
    }

    private boolean validateIdentityDuplicated(String username) {
        IdentityEntity identityEntity = identityService.getByUsername(username);
        if (identityEntity != null) {
            log.error("Username [{}] is duplicated", username);
            throw new BadRequestClientErrorException(ResponseStatus.USERNAME_DUPLICATED);
        }

        return true;
    }
}
