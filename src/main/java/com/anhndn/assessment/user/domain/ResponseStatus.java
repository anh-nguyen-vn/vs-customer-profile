package com.anhndn.assessment.user.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ResponseStatus {
    private String code;
    private String message;

    public static final ResponseStatus SUCCESS = new ResponseStatus("success", "Success");
    public static final ResponseStatus GENERAL_ERROR = new ResponseStatus("020001", "Any error occur");
    public static final ResponseStatus INVALID_DATA_TYPE = new ResponseStatus("020002", "Field %s is in invalid data type");
    public static final ResponseStatus RSA_DECRYPT_ERROR = new ResponseStatus("020003", "Invalid RSA encrypted string");
    public static final ResponseStatus INVALID_CREDENTIAL = new ResponseStatus("020004", "Invalid credential");
    public static final ResponseStatus PASSWORD_REQUIRED = new ResponseStatus("020005", "Password is not allowed as empty");
    public static final ResponseStatus EMAIL_DUPLICATED = new ResponseStatus("020006", "Email is duplicated");
    public static final ResponseStatus USERNAME_DUPLICATED = new ResponseStatus("020007", "Username is duplicated");
    public static final ResponseStatus REQUEST_VALIDATION_ERROR = new ResponseStatus("020008", "One or multiple fields are not valid");
}
