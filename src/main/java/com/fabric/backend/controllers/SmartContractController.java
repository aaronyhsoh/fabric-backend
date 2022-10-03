package com.fabric.backend.controllers;

import com.fabric.backend.dto.request.EvaluateTransactionRequestDto;
import com.fabric.backend.services.SmartContractService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.hyperledger.fabric.client.*;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@RestController
@RequestMapping("smart-contract")
public class SmartContractController {

    @Autowired
    SmartContractService smartContractService;
    @Value("${org.msp.id}")
    private String mspID;
    @Value("${peer.endpoint.url}")
    private String peerEndpoint;
    @Value("${peer.override.auth}")
    private String overrideAuth;
    @Value("${fabric.node.cert.path}")
    private String certPath;
    @Value("${fabric.node.private.key.path}")
    private String privateKeyPath;
    @Value("${fabric.node.peer.tls.cert.path}")
    private String tlsCertPath;

    @GetMapping("/test")
    public ResponseEntity testHealthCheck() {
        return ResponseEntity.ok("Server alive");
    }

    @PostMapping("/evaluate-transaction/channel/{channelName}/contract/{contractName}/user/{user}")
    @ResponseBody
    public ResponseEntity evaluateTransaction(@RequestBody EvaluateTransactionRequestDto dto)
            throws GatewayException, CertificateException, IOException, InvalidKeyException, InterruptedException
    {
        // TODO: Add error handling
        String transactionResult = smartContractService.evaluateTransaction(dto.getChannelName(), dto.getContractName(), dto.getUser(), dto.getFunctionName());
        return ResponseEntity.ok(transactionResult);
    }

}
