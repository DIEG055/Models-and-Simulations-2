package Ejercicio_1_2;

import static simlib.SimLib.*;

import java.io.IOException;

import simlib.io.*;
import simlib.collection.*;

public class Ejercicio_1_2 {

    static final int BUSY = 1, IDLE = 0, STREAM_ARRIVE = 5, STREAM_DEPARTURE = 7,
            STREAM_ELECTION_BERTH = 51;
    static final byte EVENT_ARRIVAL = 1, EVENT_DEPARTURE = 2, EVENT_END_SIMULATION = 3;
//    static  float MEAN_ARRIVAL = 1.25f, MIN_TIME_UNLOADED = 0.5f,
//            MAX_TIME_UNLOADED = 1.5f, LENGTH_SIMULATION = 90;
    static float MEAN_ARRIVAL, MIN_TIME_UNLOADED, MAX_TIME_UNLOADED, LENGTH_SIMULATION;
    static float minTimeShip, maxTimeShip, totalTimeShips;
    static int uploadedShips;
    static SimReader reader;
    static SimWriter writer;
    static Queue<Ship> queueShips;
    static Berth[] berths;
    static Crane[] cranes;

    public static void main(String[] args) throws IOException {

        reader = new SimReader("src/Ejercicio_1_2/Ejercicio_1_2.in");
        writer = new SimWriter("src/Ejercicio_1_2/Ejercicio_1_2.out");
        queueShips = new Queue<>("Ship Queue");
        berths = new Berth[3];/*se maneja desde 1*/
        cranes = new Crane[3];/*se maneja desde 1*/
        minTimeShip = 0;
        maxTimeShip = 0;
        totalTimeShips = 0;
        for (int i = 1; i < cranes.length; i++) {
            cranes[i] = new Crane(i, IDLE);
            berths[i] = new Berth();
        }
        MEAN_ARRIVAL = reader.readFloat();
        MIN_TIME_UNLOADED = reader.readFloat();
        MAX_TIME_UNLOADED = reader.readFloat();
        LENGTH_SIMULATION = reader.readFloat();
//        System.out.println("                 HARBOR SYSTEM          ");
//        System.out.println("------------------------------------------------");
//        System.out.println("                   DATA INPUT           ");
//        System.out.println("------------------------------------------------");
//        System.out.println("   Mean Arrival:                    " + MEAN_ARRIVAL + "   ");
//        System.out.println("   Min Time Unloaded:               " + MIN_TIME_UNLOADED + "    ");
//        System.out.println("   Max Time Unloaded:               " + MAX_TIME_UNLOADED + "    ");
//        System.out.println("   Length simulation:               " + LENGTH_SIMULATION + "   ");
//        System.out.println("------------------------------------------------");

        writer.write("                 HARBOR SYSTEM          " + "\n");
        writer.write("------------------------------------------------" + "\n");
        writer.write("                   DATA INPUT           " + "\n");
        writer.write("------------------------------------------------" + "\n");
        writer.write("   Mean Arrival:                    " + MEAN_ARRIVAL + "   " + "\n");
        writer.write("   Min Time Unloaded:               " + MIN_TIME_UNLOADED + "    " + "\n");
        writer.write("   Max Time Unloaded:               " + MAX_TIME_UNLOADED + "    " + "\n");
        writer.write("   Length simulation:               " + LENGTH_SIMULATION + "   " + "\n");
        writer.write("------------------------------------------------" + "\n");
        initSimlib();

        ///Primer barco
        eventSchedule(unifrm(MIN_TIME_UNLOADED, MAX_TIME_UNLOADED, STREAM_ARRIVE), EVENT_ARRIVAL);
        // Fin simulacion
        eventSchedule(LENGTH_SIMULATION, EVENT_END_SIMULATION);

        do {
            timing();
            switch (eventType) {
                case EVENT_ARRIVAL:
                    arrive();
                    break;
                case EVENT_DEPARTURE:
                    depart();
                    break;
                case EVENT_END_SIMULATION:
                    report();
                    break;
            }
        } while (eventType != EVENT_END_SIMULATION);

        reader.close();
        writer.close();
    }

    static void arrive() {
        eventSchedule(simTime + expon(MEAN_ARRIVAL, STREAM_ARRIVE),
                EVENT_ARRIVAL);
        if (berthsBusy()) {
            queueShips.offer(new Ship(simTime));
        } else {
            /*se crea y ubica el barco en un amarre libre*/
            shipInBerth(new Ship(simTime));
        }
    }

    static void depart() {
        if (queueShips.isEmpty()) {
            outOfHarbor();
        } else {
            outOfHarbor();/*la funcion que busca amarre vacio no funcionaria correctamente si se usa false*/
 /*barco sale de la cola*/
            Ship s = queueShips.poll();
            /*se ubica el barco  s en un amarre libre*/
            shipInBerth(s);
        }
    }

    static public void outOfHarbor() {
        /*se busca el amarre del cual sale el barco*/
        for (int i = 1; i < berths.length; i++) {
            if (berths[i].shipDepartureTime == simTime) {
                /*se saca al barco*/
                Ship s = berths[i].unloadedShip(simTime);
                float timeInHarbor = s.timeInSystem(simTime);
                /*se finaliza el trabajo en ese amarre*/
                for (int j = 1; j < cranes.length; j++) {
                    if (cranes[j].actualBerth == i) {
                        cranes[j].endTask(simTime);
                    }
                }
                /*se guardan las estadisticas del barco */
                totalTimeShips += timeInHarbor;
                if (timeInHarbor >= maxTimeShip) {
                    maxTimeShip = timeInHarbor;
                }
                if (timeInHarbor < minTimeShip || minTimeShip == 0) {
                    minTimeShip = timeInHarbor;
                }
                uploadedShips++;
                berths[i].state = IDLE;
            }
        }
    }

    static public void shipInBerth(Ship s) {
        int emptyBerth = posBerthIdle();
        float departureTime = simTime + unifrm(MAX_TIME_UNLOADED, MAX_TIME_UNLOADED, STREAM_DEPARTURE);
        berths[emptyBerth].addShip(s, simTime, departureTime);
//        System.out.println("ENTRO al amarre " + emptyBerth+ " en el tiempo " + simTime);
        /*se avisa de llegada de barco a  gruas*/
        for (int i = 1; i < cranes.length; i++) {
            float newdeparture = cranes[i].notifyNewShip(simTime, emptyBerth, berths);
            if (newdeparture != 0) {
                eventSchedule(newdeparture, EVENT_DEPARTURE);
            }
        }
        /*se programa su salida*/
        eventSchedule(departureTime, EVENT_DEPARTURE);
    }

    static void report() throws IOException {
        writer.write("                   DATA OUTPUT           " + "\n");
        writer.write("------------------------------------------------" + "\n");
        writer.write("   Ships in Harbor:                 " + uploadedShips + "\n" + "\n");
        writer.write("   Min Time Ship:                   " + minTimeShip + "\n");
        writer.write("   Max Time Ship:                   " + maxTimeShip + "\n");
        /*solo se tienen en cuenta los que acabaron su proceso*/
        writer.write("   Average Time in Harbor Ships:    " + (totalTimeShips / uploadedShips) + "\n" + "\n");
        for (int i = 1; i < cranes.length; i++) {
            writer.write("   Crane " + i + " Utilization:             " + cranes[i].getUsetime(simTime) + "\n");
        }
        writer.write("\n");
        for (int i = 1; i < berths.length; i++) {
            writer.write("   Berth " + i + " Utilization:             " + berths[i].getUsetime(simTime) + "\n");
        }
        writer.write("------------------------------------------------" + "\n");
    }

    static boolean berthsBusy() {
        return berths[1].busy() && berths[2].busy();
    }

    static int posBerthIdle() {
        if (!berths[1].busy() && !berths[2].busy()) {
            double r = rand(STREAM_ELECTION_BERTH);
            return (r > 0.5) ? 1 : 2;
        } else if (!berths[1].busy()) {
            return 1;
        } else {
            return 2;
        }
    }

}
