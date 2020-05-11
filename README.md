# Wiremock steps

1.Stubbing is a technique that allows us to configure the HTTP response that is returned by our WireMock server when it receives a specific HTTP request.
2. We can stub HTTP requests with WireMock by using the static givenThat() or stubFor() methods of the WireMock class.
3. We can create the returned HTTP response by creating a new ResponseDefinitionBuilder object.
4. When we want to configure the returned HTTP response, we have to invoke the willReturn() method of the MappingBuilder interface and pass a ResponseDefinitionBuilder object as a method parameter.
5. We can create new ResponseDefinitionBuilder objects by using either the manual approach or by leveraging the static factory methods of the WireMock class.
