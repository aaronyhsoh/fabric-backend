package com.fabric.backend.services;

import org.hyperledger.fabric.client.GatewayException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;

public interface SmartContractService {

    public String evaluateTransaction(String channelName, String contractName, String user, String functionName) throws CertificateException, IOException, InterruptedException, GatewayException, InvalidKeyException;
}
