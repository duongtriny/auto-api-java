package api.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class StubServer {

    private static WireMockServer wireMockServer;

    public static WireMockServer getWireMockServer(){
        if(wireMockServer == null){
            wireMockServer = new WireMockServer(options().port(8089)
                    .notifier(new ConsoleNotifier(true))); //No-args constructor will start on port 8080, no HTTPS
        }
        return wireMockServer;
    }

    public static void startStubServer() {
        if (!getWireMockServer().isRunning()){
            getWireMockServer().start();
        }
    }


    public static void main(String[] args) {
        startStubServer();
    }
}
