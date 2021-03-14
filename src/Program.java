public class Program {
    public static void main(String[] args) {
        ScheduleGenerator gnr = new ScheduleGenerator(
                Config.capacityContainerUnitPerDay,
                Config.capacityDryKgPerDay,
                Config.capacityLiquidKgPerDay);

        for(int i = 0; i < 5; i++) {
            gnr.generate().print();
            System.out.println();
        }
    }
}
