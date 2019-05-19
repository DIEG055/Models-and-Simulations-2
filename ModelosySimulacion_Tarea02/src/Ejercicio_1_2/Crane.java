/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ejercicio_1_2;

/**
 *
 * @author Juan Diego Medina
 */
public class Crane {

    int BUSY = 1;
    int IDLE = 0;
    float usetime;
    float shipArrivalTime;

    int favoriteBerth;
    int actualBerth;
    int state;

    public Crane(int favoriteberth, int state) {
        this.usetime = 0;
        this.favoriteBerth = favoriteberth;
        this.shipArrivalTime = 0;
        this.state = state;
    }

    
    public float getUsetime(float simTime) {
        return this.usetime / simTime;
    }

    public float notifyNewShip(float simtime, int berth, Berth[] berths) {
       if (berth == favoriteBerth) {
            if (state == BUSY) { /*Estaba ocupada en el otro amarre*/
                /*duplicar tiempo del otro amarre */
                 int b = (berth == 1) ? 2:1;
                  System.out.println("Duplique en amarre "+ b);
            
                actualBerth = berth;

                usetime += (simtime - shipArrivalTime);/*termina de contar el tiempo de uso del otro barco*/
                shipArrivalTime = simtime;
                return berths[b].duplicateDeparture(simtime);
            } else {
                state = BUSY;
                actualBerth = berth;
                shipArrivalTime = simtime;
            }
        } else {
            if (state == IDLE) {
                /*dividir el tiempo de amarre a la mitad*/           
                System.out.println("Reduci em amarre " + berth);
             
                state = BUSY;
                actualBerth = berth;
                shipArrivalTime = simtime;
                return berths[berth].reduceDeparture(simtime);
            }
        }
        return 0;
    }

    public void endTask(float simtime) {
        this.usetime += (simtime - shipArrivalTime);
        this.shipArrivalTime = 0;
        this.state = IDLE;
    }

}
