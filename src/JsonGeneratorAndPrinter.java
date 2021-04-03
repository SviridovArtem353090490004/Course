

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Scanner;

//Service 2
public class JsonGeneratorAndPrinter {

    private String ScheduleJson;

    public String generateJson(Schedule sc) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(sc);

    }

    public String generateJsonFromConsole() throws JsonProcessingException {
        Instant arriving_date;
        String name;
        long weightKg;
        CargoType type;
        long days;

        Scanner sc = new Scanner(System.in);
        System.out.println("Input arriving datetime (ex. 2010-04-15T03:57:03Z): ");
        arriving_date = Instant.parse(sc.next());

        System.out.println("Input name: ");
        name = sc.next();

        System.out.println("Input weight (kg): ");
        weightKg = sc.nextLong();

        while(true) {
            System.out.println("Choose cargo type: ");
            System.out.println("1. Liquid");
            System.out.println("2. Container");
            System.out.println("3. Dry");
            int i = sc.nextInt() - 1;
            if(i < 0 || i >= CargoType.values().length){
                System.out.println("Wrong number! Try Again!");
                continue;
            }

            type = CargoType.values()[i];
            break;
        }

        System.out.println("Input Days");
        days = sc.nextLong();

        return generateJson(new Schedule(arriving_date, name, weightKg, type, days));

    }

    public void printSchedule(){

    }

}
