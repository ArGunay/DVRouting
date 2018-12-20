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

    HashMap<Integer, Integer> nextHop = new HashMap<>();

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

        /**
         * we need to map the interface to the routers to know
         * which address maps to which interface
         * */
        if(mex instanceof nameRevealMessage){

            int routerName = ((nameRevealMessage) mex).getRoutername();
            if(!routerInterfaceMap.containsKey(routerName)) {
                routerInterfaceMap.put(routerName, ifx);
            }
            elaborateForwardingTable(routerName, ifx);
        }
        // the routers now start to exchange the tables.
        else {
            elaborateBellmanFord(mex,ifx);
        }
    }

    public void elaborateForwardingTable(int routername, int ifx){
//

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

    public void elaborateBellmanFord(RoutingMessage mex, int ifx){

        System.out.println("============ "+my_address()+" BELMANNING NOW! ===============");

        HashMap<Integer, HashMap<Integer, Double>> receivedTable = ((DVMessage) mex).getMessage();
        int senderAddress=  ((DVMessage)mex).address;
        System.out.println("receivedTable: " + receivedTable);

        System.out.println("containskey?: " +table.containsKey(senderAddress));

        for(Integer router : receivedTable.keySet()) {
            if (!table.containsKey(router)) {
                System.out.println("adding received table to my table");
                System.out.println("senderAddress: " + senderAddress);
                table.put(router, receivedTable.get(router));

            }
            else
            {
                // if the table already contains this router
                // we have received a new one,
                // must replace it with the new weights of this route
                // and compute the new weights if necessary

            }
        }



//        for(Integer key : tempTable.keySet()) {
//            if(!table.containsKey(key)){
//
//
////                System.out.println("tmptbl " + tempTable.keySet());
//                table.put(key,tempTable.get(key));
//
//                // need to add all the nodes not present in this routers table
//                HashMap<Integer, Double> thisNodesTemp = table.get(my_address());
//                System.out.println("thisNodeTemp: "+my_address()+" "+thisNodesTemp);
//
//                for(Integer node : tempTable.keySet()){
//                    if(!thisNodesTemp.containsKey(node)){
//                        table.get(my_address()).put(node,  Double.POSITIVE_INFINITY);
//                    }
////                    Math.min(table.get(my_address()).get(node),)
////                    if(table.get(my_address()).get(node) > )
//                }
//            }
//        }

        System.out.println("table:     " + table);
        System.out.println("iRm:       "+ routerInterfaceMap);
        System.out.println("=============================");

    }



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
