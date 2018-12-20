import simplenet.*;

import java.util.HashMap;


public class DVRouter extends simplenet.Router{


//    =========================   VARIABLES ===============================

    /**
     * Table for message transmission of DV
     * format is
     * {my_address : {router_to : link_cost(i)}}
     */
    HashMap<Integer, HashMap<Integer, Double>> table = new HashMap<>();

    //    To map the interface the router has with the actual name of the router
    //   {routerName: interface}
    HashMap<Integer, Integer> routerInterfaceMap = new HashMap<>();


//    ========================== METHODS ======================================

// INITIALIZATE ------------------------
    @Override
    public void initialize() {
        sendNameAwarenessMessage();
    }

//    MESSAGE PROCESSING -------------
    @Override
    public void process_routing_message(RoutingMessage mex, int ifx) {

        if(mex instanceof nameRevealMessage){
            // meet

            int routerName = ((nameRevealMessage) mex).getRoutername();

            if(!routerInterfaceMap.containsKey(routerName)) {
                routerInterfaceMap.put(routerName, ifx);

            }
            elaborateForwardingTable(routerName, ifx);
        }
        else {
            elaborateBellmanFord(mex,ifx);
        }
        System.out.println(my_address()+ " tabFinale??: " + table);
    }


    public void elaborateBellmanFord(RoutingMessage mex, int ifx){

        System.out.println("============ "+my_address()+" BELMANNING NOW! ===============");

        HashMap<Integer, HashMap<Integer, Double>> receivedTable = ((DVMessage) mex).getMessage();

        int senderAddress = ((DVMessage) mex).address;
        System.out.println(senderAddress+" sent table: " + receivedTable);

        System.out.println("tableBefore:     " + table);


        for(Integer router : receivedTable.keySet()) {
            if (!table.containsKey(router)) {
//                System.out.println("adding received table to my table");
                System.out.println("senderAddress: " + senderAddress);
                table.put(router, receivedTable.get(router));

            }
            // if the table already contains this router
            // we have received a new one,
            // must replace it with the new weights of this route
            // and compute the new weights if necessary

            // replacing old router table with new one
            else {
                table.remove(router);
                HashMap<Integer, Double> newRouting = receivedTable.get(router);
                table.put(router, newRouting);

                System.out.println("newRouting "+ newRouting);
            }
        }


        System.out.println(my_address()+" TableAfter:     " + table);
        System.out.println("r = i:     "+ routerInterfaceMap);
        System.out.println("=============================");

    }

    // ======================================================================

    public void elaborateForwardingTable(int routername, int ifx){
        System.out.println(my_address() +  " Message from " + routername);
        HashMap<Integer,Double> vectorCost = new HashMap<>();
        vectorCost.put(routername, link_cost(ifx));

        //initialize self as cost zero
        vectorCost.put(my_address(), 0.0);
        // insert first value inside the table
        table.put(my_address(),vectorCost);

        for (int i = 0; i < interfaces(); i++){
            send_message(new DVMessage(table, my_address()),i);
        }

    }
// ======================================================================


    // First avareness message to let neighbors know router name
    public void sendNameAwarenessMessage(){
        for(int i = 0; i< interfaces(); i++){
            send_message(new nameRevealMessage(my_address()),i);
        }
    }


}


// =========================================  MESSSAGING CLASSES ===========================

// This class is used for sending the tables of the routers
class DVMessage extends RoutingMessage{

    public HashMap<Integer, HashMap<Integer, Double>> table;
    public int address;


    public DVMessage(HashMap<Integer, HashMap<Integer, Double>> table, int address){
        this.table = table;
        this.address = address;
    }

    public HashMap<Integer, HashMap<Integer, Double>> getMessage(){
        return this.table;
    }

    public int getAddress() {
        return this.address;
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