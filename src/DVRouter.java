import simplenet.*;

import java.util.HashMap;


public class DVRouter extends simplenet.Router{



    public DVRouter(){



    }
//    =========================   VARIABLES ===============================

    /**
     * Table for message transmission of DV
     * format is
     * {my_address : {router_to : link_cost(i)}}
     */
    HashMap<Integer, HashMap<Integer, Double>> table = new HashMap<>();

    HashMap<Integer, Integer> nextHop = new HashMap<>();
    /*
        To map the interface the router has with the actual name of the router
        {interface : routerName}
     */
    HashMap<Integer, Integer> interfaceToRouterMapping = new HashMap<>();

//    =========================================================================

//    ========================== METHODS ======================================

// INITIALIZATE ------------------------
    @Override
    public void initialize() {
        sendNameAwarenessMessage();
    }


//    MESSAGE PROCESSING -------------
    @Override
    public void process_routing_message(RoutingMessage mex, int ifx) {

        /**
         * we need to map the interface to the routers to know
         * which address maps to which interface
         * */
        if(mex instanceof nameRevealMessage){

            int routerName = ((nameRevealMessage) mex).getRoutername();
            interfaceToRouterMapping.put(ifx ,routerName);
            elaborateForwardingTable(ifx);
        }
        // the routers now start to exchange the tables.
        else {

            elaborateBellmanFord(mex,ifx);

//            System.out.println("===============================================");
//            System.out.println("iAm " + my_address() + ": incoming message from " + ifx);
//            System.out.println("Message = " + ((DVMessage) mex).getMessage());
//            System.out.println("youAre : " + ((DVMessage) mex).getYouare());
//            System.out.println("===============================================");
        }
    }

    public void elaborateForwardingTable(int ifx){
        System.out.println("first table to send");
        HashMap<Integer,Double> vectorCost = new HashMap<>();
        vectorCost.put(interfaceToRouterMapping.get(ifx), link_cost(ifx));
        table.put(my_address(),vectorCost);

        for (int i = 0; i < interfaces(); i++){
            send_message(new DVMessage(table, my_address()),i);
        }


    }

    public void elaborateBellmanFord(RoutingMessage mex, int ifx){
        System.out.println("============ BELMANNING NOW! ===============");
        if(!interfaceToRouterMapping.containsKey(ifx)){
            interfaceToRouterMapping.put(ifx, ((DVMessage) mex).getAddress());
        }


        HashMap<Integer, HashMap<Integer, Double>> temptable = ((DVMessage) mex).getMessage();
        System.out.println("table: " + temptable);
    }



    // First avareness message to let neighbors know router name
    public void sendNameAwarenessMessage(){
        for(int i = 0; i< interfaces(); i++){
            send_message(new nameRevealMessage(my_address()),i);
        }
    }

//     Create a vector table for this router ---------------

    public void initializeTables(){
        System.out.println("creating Routing table");
        if (table.isEmpty()){
            HashMap<Integer,Double> vectorCost = new HashMap<>();

            for (int i = 0; i < interfaces(); i++){
                // create vector cost hash map
                vectorCost.put(i, link_cost(i));
                // initialize next hop table
                nextHop.put(i,i);
            }
            table.put(my_address(), vectorCost);
        }
        System.out.println(table);
        System.out.println(nextHop);
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
