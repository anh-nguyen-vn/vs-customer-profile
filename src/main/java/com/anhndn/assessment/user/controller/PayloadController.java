package com.anhndn.assessment.user.controller;

import com.anhndn.assessment.user.aspect.LogAround;
import com.anhndn.assessment.user.controller.request.PayloadCreateRequest;
import com.anhndn.assessment.user.domain.GeneralResponse;
import com.anhndn.assessment.user.domain.Payload;
import com.anhndn.assessment.user.domain.ResponseStatus;
import com.anhndn.assessment.user.domain.UserType;
import com.anhndn.assessment.user.exception.BadRequestClientErrorException;
import com.anhndn.assessment.user.exception.RsaDecryptException;
import com.anhndn.assessment.user.factory.ResponseFactory;
import com.anhndn.assessment.user.repository.entity.IdentityEntity;
import com.anhndn.assessment.user.repository.entity.ProfileEntity;
import com.anhndn.assessment.user.service.IdentityService;
import com.anhndn.assessment.user.service.RsaDecryptService;
import com.anhndn.assessment.user.service.UserService;
import com.anhndn.assessment.user.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class PayloadController {

    @Autowired
    private UserService userService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RsaDecryptService rsaDecryptService;

    @Autowired
    private ResponseFactory responseFactory;

    @LogAround(message = "create payload")
    @PostMapping(value = "internal/users/payloads", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GeneralResponse<Payload>> createPayload(@Valid @RequestBody PayloadCreateRequest request) {
        ResponseEntity responseEntity;

        log.info("Create payload for user with username [{}]", request.getUsername());
        String plainPassword;
        try {
            plainPassword = rsaDecryptService.decrypt(request.getPassword());
        } catch (RsaDecryptException ex) {
            log.error("Can not decrypt password", ex);
            throw new BadRequestClientErrorException(ResponseStatus.RSA_DECRYPT_ERROR);
        }

        IdentityEntity identityEntity = identityService.getByUsername(request.getUsername());

        if (identityEntity == null) {
            log.error("No user identity found with username [{}]", request.getUsername());
            throw new BadRequestClientErrorException(ResponseStatus.INVALID_CREDENTIAL);
        } else {
            String encryptedPassword = EncryptionUtil.sha256(plainPassword);
            String currentPassword = identityEntity.getPassword();

            if (currentPassword.equals(encryptedPassword)) {
                ProfileEntity profileEntity = userService.getById(identityEntity.getProfileId());
                if (profileEntity.getIsDeleted()) {
                    log.error("No active user found");
                    throw new BadRequestClientErrorException(ResponseStatus.INVALID_CREDENTIAL);
                } else {
                    Payload payloadCreateResponse = new Payload(profileEntity.getId(), identityEntity.getUserName(), UserType.USER);
                    responseEntity = responseFactory.success(payloadCreateResponse, Payload.class);
                }
            } else {
                log.error("Unrecognized password");
                throw new BadRequestClientErrorException(ResponseStatus.INVALID_CREDENTIAL);
            }
        }

        return responseEntity;
    }
}
