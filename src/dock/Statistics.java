package dock;

public class Statistics {
    long countUnloadedShips; //кол-во разгруженных кораблей
    long averageWaitingHours; //среднее время ожидания на разгрузку
    long maxUnloadingDelay; //максимальное отставание в разгрузке
    long averageUnloadingDelay; //среднее отставание в разгрузке
    long penaltyChargeSum; //сумма всех штрафов
    long countLiquidLift; //необходимое количество Liquid кранов
    long countContainerLift; //необходимое количество Dry кранов
    long countDryLift; //необходимое количество Container кранов

    public Statistics(long countUnloadedShips, long averageWaitingHours, long maxUnloadingDelay, long averageUnloadingDelay, long penaltyChargeSum, long countLiquidLift, long countContainerLift, long countDryLift) {
        this.countUnloadedShips = countUnloadedShips;
        this.averageWaitingHours = averageWaitingHours;
        this.maxUnloadingDelay = maxUnloadingDelay;
        this.averageUnloadingDelay = averageUnloadingDelay;
        this.penaltyChargeSum = penaltyChargeSum;
        this.countLiquidLift = countLiquidLift;
        this.countContainerLift = countContainerLift;
        this.countDryLift = countDryLift;
    }
}