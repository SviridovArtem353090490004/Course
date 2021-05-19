package springboot;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


@RestController
public class Service2 {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @GetMapping("/service2/create_json")
    public String createJson(String name) {
        HttpGet request = new HttpGet("http://localhost:8080/service1");

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String json = EntityUtils.toString(entity);

                FileWriter fw = new FileWriter(new File(name));
                fw.write(json);
                fw.close();
                return name;

            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("/service2/get_by_name")
    public String getJsonByName(String name) {
        try {
            File f = new File(name);
            try (Scanner s = new Scanner(f).useDelimiter("\\n")) {
                String line = "";
                while (s.hasNext()) {
                    line = line + s.next();
                }
                return line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "error";
        }

    }

    @PostMapping("/service2/post")
    @ResponseStatus(HttpStatus.CREATED)
    public void createJsonFile(@RequestBody String json, String name) {
        try {
            FileWriter fw = new FileWriter(new File(name));
            fw.write(json);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

