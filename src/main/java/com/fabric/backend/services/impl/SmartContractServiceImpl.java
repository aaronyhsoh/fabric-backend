package com.fabric.backend.services.impl;

import com.fabric.backend.services.SmartContractService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service
public class SmartContractServiceImpl implements SmartContractService {
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

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String evaluateTransaction(String channelName, String contractName, String user, String functionName) throws CertificateException, IOException, InterruptedException, GatewayException, InvalidKeyException {
        ManagedChannel channel = newGrpcConnection();
        Gateway.Builder builder = Gateway.newInstance().identity(newIdentity(user)).signer(newSigner(user)).connection(channel)
                // Default timeouts for different gRPC calls
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        try (Gateway gateway = builder.connect()) {
            Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(contractName);
            System.out.println("\n--> Evaluate Transaction: " +  functionName);

            byte[] evaluateResult = contract.evaluateTransaction(functionName);

            System.out.println("*** Result:" + prettyJson(evaluateResult));
            return prettyJson(evaluateResult);
        } catch(Exception e) {
            throw e;
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private ManagedChannel newGrpcConnection() throws IOException, CertificateException {
        Reader tlsCertReader = Files.newBufferedReader(Paths.get(tlsCertPath));
        X509Certificate tlsCert = Identities.readX509Certificate(tlsCertReader);

        return NettyChannelBuilder.forTarget(peerEndpoint)
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build()).overrideAuthority(overrideAuth)
                .build();
    }

    private Identity newIdentity(String user) throws IOException, CertificateException {
        Reader certReader = Files.newBufferedReader(Paths.get(certPath + "/" + user + "/cert.pem"));
        X509Certificate certificate = Identities.readX509Certificate(certReader);

        return new X509Identity(mspID, certificate);
    }

    private Signer newSigner(String user) throws IOException, InvalidKeyException {
        Reader keyReader = Files.newBufferedReader(getPrivateKeyPath(user));
        PrivateKey privateKey = Identities.readPrivateKey(keyReader);

        return Signers.newPrivateKeySigner(privateKey);
    }

    private String prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private String prettyJson(final String json) {
        JsonElement parsedJson = JsonParser.parseString(json);
        return gson.toJson(parsedJson);
    }

    private Path getPrivateKeyPath(String user) throws IOException {
        try (Stream<Path> keyFiles = Files.list(Paths.get(privateKeyPath + "/" + user + "/priKey"))) {
            return keyFiles.findFirst().orElseThrow(NullPointerException::new);
        }
    }
}
