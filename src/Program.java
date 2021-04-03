import com.fasterxml.jackson.core.JsonProcessingException;

public class Program {
    public static void main(String[] args) throws JsonProcessingException {

        //start date
        //end date


        ScheduleGenerator gnr = new ScheduleGenerator(
                Config.capacityContainerUnitPerHour,
                Config.capacityDryKgPerHour,
                Config.capacityLiquidKgPerHour);

        for(int i = 0; i < 5; i++) {
            gnr.generate().print();
            System.out.println();
        }


        //s1
        //s2
        //s3


        //s3.(s2.generate())

//        JsonGeneratorAndPrinter jgap = new JsonGeneratorAndPrinter();
//        System.out.println(jgap.generateJsonFromConsole());

    }
}
