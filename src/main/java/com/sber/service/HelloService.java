package com.sber.service;

import com.google.protobuf.Timestamp;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class HelloService {
    @GrpcClient("gooseClient")
    GooseService gooseService;

    void onStart(@Observes StartupEvent ev) throws ExecutionException, InterruptedException {
        makeCall();
    }

    public void makeCall() throws ExecutionException, InterruptedException {
        var request = MessageRequest.newBuilder().setHello("Hello from client!")
                .setTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build()).build();
        var responseUni = gooseService.gooseHello(request);
        responseUni.onItem().invoke(response -> {
                    var localDate = Instant.ofEpochSecond(response.getTime().getSeconds())
                            .atZone( ZoneId.of( "America/Montreal" ) )
                            .toLocalDate();
                    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                    var timeStr = formatter.format(localDate);
                    System.out.println(response.getHello() + " " + timeStr);
                }).subscribe().asCompletionStage().get();
    }
}
