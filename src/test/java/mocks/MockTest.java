package mocks;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MockTest {
    private static final String WIREMOCK_PATH = "/sample/wiremock";
    private static final String APPLICATION_JSON = "application/json";
    int port = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(port); // No-args constructor defaults to port 8080

    @Test
    public void verifyWhenMatchingURL() throws IOException {

        // Creating a REST stub for a service endpoint using regular expression
        WireMock.stubFor(get(urlPathMatching("/sample/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("\"testing-library\": \"WireMock\"")));

        // Make a GET HTTP api request
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(String.format("http://localhost:%s/sample/wiremock", port));
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);

        // Verify the response returned
        verify(getRequestedFor(urlEqualTo(WIREMOCK_PATH)));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals(APPLICATION_JSON, httpResponse.getFirstHeader("Content-Type").getValue());
        assertEquals("\"testing-library\": \"WireMock\"", stringResponse);
    }

    @Test
    public void verifyWhenMatchingHeaders() throws IOException {
        stubFor(get(urlPathEqualTo(WIREMOCK_PATH))
                .withHeader("Accept", matching("text/.*"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "text/html")
                        .withBody("!!! Service Unavailable !!!")));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(String.format("http://localhost:%s/sample/wiremock", port));
        request.addHeader("Accept", "text/html");
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);

        verify(getRequestedFor(urlEqualTo(WIREMOCK_PATH)));
        assertEquals(503, httpResponse.getStatusLine().getStatusCode());
        assertEquals("text/html", httpResponse.getFirstHeader("Content-Type").getValue());
        assertEquals("!!! Service Unavailable !!!", stringResponse);
    }

    @Test
    public void verifyWhenMatchingBody() throws IOException {
        stubFor(post(urlEqualTo(WIREMOCK_PATH))
                .withHeader("Content-Type", equalTo(APPLICATION_JSON))
                .withRequestBody(containing("\"testing-library\": \"WireMock\""))
                .withRequestBody(containing("\"creator\": \"Tom Akehurst\""))
                .withRequestBody(containing("\"website\": \"wiremock.org\""))
                .willReturn(aResponse().
                        withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("\"success\": \"true\"")));

        InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("wiremock_intro.json");
        String jsonString = convertInputStreamToString(jsonInputStream);
        StringEntity entity = new StringEntity(jsonString);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(String.format("http://localhost:%s/sample/wiremock", port));
        request.addHeader("Content-Type", APPLICATION_JSON);
        request.setEntity(entity);
        HttpResponse response = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(response);

        verify(postRequestedFor(urlEqualTo(WIREMOCK_PATH))
                .withHeader("Content-Type", equalTo(APPLICATION_JSON)));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("\"success\": \"true\"", stringResponse);

    }

    @Test
    public void verifyWhenNotUsingPriority() throws IOException {
        stubFor(get(urlPathMatching("/sample/.*")).willReturn(aResponse().withStatus(200)));
        stubFor(get(urlPathEqualTo(WIREMOCK_PATH)).withHeader("Accept", matching("text/.*")).willReturn(aResponse().withStatus(503)));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(String.format("http://localhost:%s/sample/wiremock", port));
        request.addHeader("Accept", "text/xml");
        HttpResponse httpResponse = httpClient.execute(request);

        verify(getRequestedFor(urlEqualTo(WIREMOCK_PATH)));
        assertEquals(503, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void verifyWhenUsingPriority() throws IOException {
        stubFor(get(urlPathMatching("/sample/.*")).atPriority(1).willReturn(aResponse().withStatus(200)));
        stubFor(get(urlPathEqualTo(WIREMOCK_PATH)).atPriority(2).withHeader("Accept", matching("text/.*")).willReturn(aResponse().withStatus(503)));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(String.format("http://localhost:%s/sample/wiremock", port));
        request.addHeader("Accept", "text/xml");
        HttpResponse httpResponse = httpClient.execute(request);

        verify(getRequestedFor(urlEqualTo(WIREMOCK_PATH)));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    private static String convertHttpResponseToString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        return convertInputStreamToString(inputStream);
    }

    private static String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String string = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return string;
    }


}
