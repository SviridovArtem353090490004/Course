package springboot;

import com.google.gson.Gson;
import dock.DockModel;
import dock.Report;
import dock.Schedule;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;

@RestController
public class Service3 {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @GetMapping("/service3/model")
    public String index() {
        HttpGet request = new HttpGet("http://localhost:8080/service2/create_json?name=test.json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String name = EntityUtils.toString(entity);
                HttpGet req = new HttpGet("http://localhost:8080/service2/get_by_name?name=" + name);
                try (CloseableHttpResponse res = httpClient.execute(req)) {
                    HttpEntity ent = res.getEntity();

                    if (ent != null) {
                        String data = EntityUtils.toString(ent);
                        Schedule sc = new Gson().fromJson(data, Schedule.class); //создаем объект класса расписание и даем ему json файл

                        DockModel dm = new DockModel(100, 30000, 31); //создаем модель работы порта

                        Report r = null; //стартуем модель работы порта
                        try {
                            r = dm.startModel(sc, LocalDateTime.of(2021, Month.APRIL, 2, 1, 1, 1));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String result = new Gson().toJson(r);

                        HttpPost post = new HttpPost("http://localhost:8080/service2/post?name=result.json");
                        post.setHeader("Content-type", "application/json");
                        try {
                            StringEntity stringEntity = new StringEntity(result);
                            post.getRequestLine();
                            post.setEntity(stringEntity);

                            httpClient.execute(post);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return "Done";
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}