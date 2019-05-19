package Ejercicio_1_2;

import static simlib.SimLib.*;

import java.io.IOException;

import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class Ejercicio_1_2 {

    static final int BUSY = 1, IDLE = 1, STREAM_ARRIVE = 1, STREAM_DEPARTURE = 2;
    static final byte EVENT_ARRIVAL = 1, EVENT_DEPARTURE = 2, EVENT_END_SIMULATION = 3;
    static final float MEAN_ARRIVAL = 1.25f, MIN_TIME_UNLOADED = 0.5f,
            MAX_TIME_UNLOADED = 1.5f, LENGTH_SIMULATION = 900;
    static float minTimeShip, maxTimeShip, totalTimeShips;
    static int uploadedShips;

    static SimReader reader;
    static SimWriter writer;

    static Queue<Ship> queueShips;
    static Berth[] berths;
    static Crane[] cranes;

    public static void main(String[] args) throws IOException {

//        reader = new SimReader("jobshop2.in");
//        writer = new SimWriter("jobshop2.out");
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

        initSimlib();

        ///Primer barco
        eventSchedule(unifrm(MIN_TIME_UNLOADED, MAX_TIME_UNLOADED, STREAM_ARRIVE), EVENT_ARRIVAL);
        // Fin simulacion
        eventSchedule(LENGTH_SIMULATION, EVENT_END_SIMULATION);

        do {

            timing();
            switch (eventType) {
                case EVENT_ARRIVAL:
                    arriveHarbor();

                    break;
                case EVENT_DEPARTURE:
                    depart();
                    break;
                case EVENT_END_SIMULATION:
                    report();
                    break;
            }
        } while (eventType != EVENT_END_SIMULATION);

//        reader.close();
//        writer.close();
    }

    static void arriveHarbor() {
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
            outOfHarbor(true);
        } else {
            outOfHarbor(true);/*la funcion que busca amarre vacio no funcionaria correctamente si se usa false*/
            /*barco sale de la cola*/
            Ship s = queueShips.poll();
            /*se ubica el barco  s en un amarre libre*/
            shipInBerth(s);
        }
    }

    static public void outOfHarbor(boolean queueEmpty) {
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
                /*como la cola esta vacia se pone el amarre en IDLE*/
                uploadedShips++;
                if (queueEmpty) {
                    berths[i].state = IDLE;
                }
                break;
            }
        }
    }

    static public void shipInBerth(Ship s) {
        int emptyBerth = posBerthIdle();
        float departureTime = simTime + unifrm(MAX_TIME_UNLOADED, MAX_TIME_UNLOADED, STREAM_DEPARTURE);
        berths[emptyBerth].addShip(s, simTime, departureTime);
        /*se avisa de llegada de barco a gruas*/
        for (int i = 1; i < cranes.length; i++) {
            cranes[i].notifyNewShip(simTime, emptyBerth);
        }
        /*se programa su salida*/
        eventSchedule(departureTime, EVENT_DEPARTURE);
    }

    static void report() {
        System.out.println("NUMERO DE BARCOS: " + uploadedShips);
        System.out.println("TIEMPO MINIMO: " + minTimeShip);
        System.out.println("TIEMPO MAXIMO: " + maxTimeShip);
        /*solo se tienen en cuenta los que acabaron su proceso*/
        System.out.println("TIEMPO PROMEDIO: " + (totalTimeShips/uploadedShips));
        for (int i = 1; i < cranes.length; i++) {
            System.out.println("UTILIZACION AMARRE " + i + ": " + berths[i].getUsetime(simTime));
            System.out.println("UTILIZACION GRUA " + i + ": " + cranes[i].getUsetime(simTime));
        }
    }

    static boolean berthsBusy() {
        return berths[1].busy() && berths[2].busy();
    }

    static int posBerthIdle() {
        if (!berths[1].busy() && !berths[2].busy()) {
            double r = Math.random();
            return (r > 0.5) ? 1 : 2;
        } else if (!berths[1].busy()) {
            return 1;
        } else {
            return 2;
        }
    }

}
