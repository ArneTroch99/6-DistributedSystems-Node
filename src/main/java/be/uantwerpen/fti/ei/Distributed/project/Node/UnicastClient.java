package be.uantwerpen.fti.ei.Distributed.project.Node;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UnicastClient {

    private final RestTemplate restTemplate;

    public UnicastClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    void postIP(String localIP, String goalIP) {
        System.out.println("sending");
        String url = "http://" + goalIP + ":8081/postIP?ip=" + localIP;
        restTemplate.put(url, String.class);
    }
}
