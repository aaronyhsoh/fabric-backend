package com.fabric.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EvaluateTransactionRequestDto {
    private String channelName;
    private String contractName;
    private String user;
    private String functionName;

}
