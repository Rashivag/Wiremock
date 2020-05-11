package mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.assertEquals;

public class MockScenario {

    private static final String THIRD_STATE = "third";
    private static final String SECOND_STATE = "second";
    private static final String RESPONSE_01  = "success1";
    private static final String RESPONSE_02 = "success2";
    private static final String RESPONSE_03 = "success3";
    private static final String TEXT_PLAIN = "text/plain";

    static int port = 9999;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(port);

    @Test
    public void changeStateOnEachCallTest() throws IOException {
        createWireMockStub(Scenario.STARTED, SECOND_STATE, RESPONSE_01);
        createWireMockStub(SECOND_STATE, THIRD_STATE, RESPONSE_02);
        createWireMockStub(THIRD_STATE, Scenario.STARTED, RESPONSE_03);

        assertEquals(RESPONSE_01, getApiResponse());
        assertEquals(RESPONSE_02, getApiResponse());
        //WireMock.resetAllScenarios();
        assertEquals(RESPONSE_03, getApiResponse());
    }

    private void createWireMockStub(String currentState, String nextState, String responseBody) {
        stubFor(get(urlEqualTo("/mock-scenarion-test"))
                .inScenario("mock scenario")
                .whenScenarioStateIs(currentState)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", TEXT_PLAIN)
                        .withBody(responseBody))
                .willSetStateTo(nextState)
        );
    }

    private String getApiResponse() throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(String.format("http://localhost:%s/mock-scenarion-test", port));
        HttpResponse httpResponse = httpClient.execute(request);
        return firstLineOfResponse(httpResponse);
    }

    private static String firstLineOfResponse(HttpResponse httpResponse) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))) {
            return reader.readLine();
        }
    }
}
