package springboot;


import dock.JsonGeneratorAndPrinter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Service1 {

    @RequestMapping("/service1")
    public String index() {
        return new JsonGeneratorAndPrinter().generateSchedule(250, "2021-04-02"); //генерируем json расписание на 150 кораблей начиная с даты 2021-04-02
    }

}
