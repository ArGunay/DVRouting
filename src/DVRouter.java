import simplenet.*;

import java.util.HashMap;


public class DVRouter extends simplenet.Router{


//    =========================   VARIABLES ===============================

    HashMap<Integer, HashMap<Integer, Double>> table = new HashMap<>();


//    ========================== METHODS ======================================

// INITIALIZATE ------------------------
    @Override
    public void initialize() {
        HashMap<Integer, Double> cost = new HashMap<>();
        table.put(my_address(), cost);
        for(int i = 0; i< interfaces(); i++){
            send_message(new nameRevealMessage(my_address()),i);
        }
    }

//    MESSAGE PROCESSING -------------
    @Override
    public void process_routing_message(RoutingMessage mex, int ifx) {

        if(mex instanceof nameRevealMessage){


            int routerName = ((nameRevealMessage) mex).getRoutername();
//            HashMap<Integer, Double> mytable = table.get(my_address());
            if(table.get(my_address()).containsKey(routerName)){
                if(table.get(my_address()).get(routerName) > link_cost(ifx)){
                    table.get(my_address()).put(routerName,link_cost(ifx));
                    set_forwarding_entry(routerName,ifx);
                }
            }else{
                table.get(my_address()).put(routerName,link_cost(ifx));
                set_forwarding_entry(routerName,ifx);
            }

            for (int i = 0; i < interfaces(); i++){
                send_message(new DVMessage(table, my_address()),i);
            }
        }
        else {

            HashMap<Integer, HashMap<Integer, Double>> receivedTable = ((DVMessage) mex).getMessage();

            int routername = ((DVMessage) mex).getRoutername();

            for(Integer router : receivedTable.get(routername).keySet()) {
                if (!table.get(my_address()).containsKey(router)) {
//                System.out.println("adding received table to my table");
                    table.get(my_address()).put(router, (link_cost(ifx)+receivedTable.get(routername).get(router)));
                    set_forwarding_entry(router,ifx);
                    for (int i = 0; i < interfaces(); i++) {
                        send_message(new DVMessage(table, my_address()), ifx);
                    }
                }
                else {
                    if(table.get(my_address()).get(router) >
                            table.get(my_address()).get(routername) +
                                    receivedTable.get(routername).get(router))
                    {
                        set_forwarding_entry(router,ifx);
                        for (int i = 0; i < interfaces(); i++) {
                            send_message(new DVMessage(table, my_address()), ifx);
                        }
                    }
                }
            }
        }
    }
}


// =========================================  MESSSAGING CLASSES ===========================

// This class is used for sending the tables of the routers
class DVMessage extends RoutingMessage{

    public HashMap<Integer, HashMap<Integer, Double>> table;
    public int routername;


    public DVMessage(HashMap<Integer, HashMap<Integer, Double>> table, int routername){
        this.table = table;
        this.routername = routername;
    }

    public HashMap<Integer, HashMap<Integer, Double>> getMessage(){
        return this.table;
    }

    public int getRoutername() {
        return this.routername;
    }
}


// Class used for the first exchange of router names to neighbors
class nameRevealMessage  extends RoutingMessage{
    private int routername;

    public nameRevealMessage(int routername){
        this.routername = routername;
    }

    public int getRoutername() {
        return routername;
    }
}