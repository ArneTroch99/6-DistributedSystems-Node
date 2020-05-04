package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class UnicastClient {

    private final RestTemplate restTemplate;

    public UnicastClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    void postIP(String localIP, String goalIP) {
        System.out.println("sending");
        String url = "https://" + goalIP + ":8081/postIP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> requestUpdate = new HttpEntity<>(localIP, headers);
        restTemplate.put(url, requestUpdate);
    }
}
