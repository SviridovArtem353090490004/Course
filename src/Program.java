import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class Program {
    public static void main(String[] args) {

        //start date
        //end date


        String json = new JsonGeneratorAndPrinter().generateSchedule(12);



//        System.out.println(json);
//
        Schedule sc = new Gson().fromJson(json, Schedule.class);
        sc.print();
        System.out.println("========================================");

        DockModel dm = new DockModel();

        dm.startModel(sc, "");

//
//        sc.print();

    }
}
