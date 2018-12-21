import simplenet.*;

import java.util.HashMap;


public class DVRouter extends simplenet.Router{



//  Routing table for this router will hold the weights for each neighbor
    HashMap<Integer, HashMap<Integer, Double>> table = new HashMap<>();



// ---------------------------- INITIALIZATE ------------------------
    @Override
    public void initialize() {
        // Initializing the table to avoid nullpointer once the first name message comes
        HashMap<Integer, Double> cost = new HashMap<>();
        table.put(my_address(), cost);

        // Sending to all neighbors this routers name
        for(int i = 0; i< interfaces(); i++){
            send_message(new nameRevealMessage(my_address()),i);
        }
    }

//    MESSAGE PROCESSING -------------
    @Override
    public void process_routing_message(RoutingMessage mex, int ifx) {


        // If the first  incoming message is revealing the name
        if(mex instanceof nameRevealMessage){


            int routerName = ((nameRevealMessage) mex).getRoutername();

            // If this routers table does not already contains the routername
            // that just received add it to the table
            if(!table.get(my_address()).containsKey(routerName)){
                table.get(my_address()).put(routerName,link_cost(ifx));
                set_forwarding_entry(routerName,ifx);
            }
            // Send a message to all neighbors with the new table
            for (int i = 0; i < interfaces(); i++){
                send_message(new DVMessage(table, my_address()),i);
            }
        }
        else {

            // Receiving a table
            HashMap<Integer, HashMap<Integer, Double>> receivedTable = ((DVMessage) mex).getMessage();

            int routername = ((DVMessage) mex).getRoutername();

            for(Integer router : receivedTable.get(routername).keySet()) {

                // If my table does not contain this router i add it
                if (!table.get(my_address()).containsKey(router)) {
//                System.out.println("adding received table to my table");
                    table.get(my_address()).put(router, (link_cost(ifx)+receivedTable.get(routername).get(router)));
                    set_forwarding_entry(router,ifx);
                    for (int i = 0; i < interfaces(); i++) {
                        send_message(new DVMessage(table, my_address()), ifx);
                    }
                }
                // If i have the table i compare it with mine and save the value with less weight
                else {
                    // This is Bellman-Ford
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

// This class is used for sending the routing tables of the routers
class DVMessage extends RoutingMessage{

    public HashMap<Integer, HashMap<Integer, Double>> table;
    public int routername;


    // Constructor DVMessage
    public DVMessage(HashMap<Integer, HashMap<Integer, Double>> table, int routername){
        this.table = table;
        this.routername = routername;
    }

    // Getters
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