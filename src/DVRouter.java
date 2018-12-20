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
    /*
        To map the interface the router has with the actual name of the router
        {interface : routerName}
     */
    HashMap<Integer, Integer> interfaceToRouterMapping = new HashMap<>();


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
            if(!interfaceToRouterMapping.containsKey(ifx)) {
                interfaceToRouterMapping.put(ifx, routerName);
            }
            elaborateForwardingTable(ifx);
        }
        // the routers now start to exchange the tables.
        else {
            elaborateBellmanFord(mex,ifx);
        }
    }

    public void elaborateForwardingTable(int ifx){
//

        HashMap<Integer,Double> vectorCost = new HashMap<>();
        vectorCost.put(interfaceToRouterMapping.get(ifx), link_cost(ifx));


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

        HashMap<Integer, HashMap<Integer, Double>> tempTable = ((DVMessage) mex).getMessage();

        for(Integer key : tempTable.keySet()) {
            if(!table.containsKey(key)){


//                System.out.println("tmptbl " + tempTable.keySet());
                table.put(key,tempTable.get(key));

                // need to add all the nodes not present in this routers table
                HashMap<Integer, Double> thisNodesTemp = table.get(my_address());
                System.out.println("thisNodeTemp: "+my_address()+" "+thisNodesTemp);

                for(Integer node : tempTable.keySet()){
                    if(!thisNodesTemp.containsKey(node)){

                        table.get(my_address()).put(node,  Double.POSITIVE_INFINITY);

                    }
//                    Math.min(table.get(my_address()).get(node),)
//                    if(table.get(my_address()).get(node) > )

                }
            }



        }

        System.out.println("=============================");
        System.out.println("temptable: " + tempTable);
        System.out.println("table: " + table);
        System.out.println("iRm: "+interfaceToRouterMapping);
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
