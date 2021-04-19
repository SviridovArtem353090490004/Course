import java.util.ArrayList;

public class Schedule{
    public ArrayList<ShipInfo> ships;

    public Schedule(){
        this.ships = new ArrayList<>();
    }

    public void addShip(ShipInfo shipInfo) {
        ships.add(shipInfo);
    }

    public void print(){
        for(ShipInfo s : ships){
            s.print();
        }
    }
}