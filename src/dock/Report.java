package dock;

import java.util.ArrayList;

public class Report {
    ArrayList<UnloadingReport> unloadingList; //список кораблей на разгрузке
    Statistics statistics; //статистика

    public Report(ArrayList<UnloadingReport> unloadingList, Statistics statistics) {
        this.unloadingList = unloadingList;
        this.statistics = statistics;
    }
}
