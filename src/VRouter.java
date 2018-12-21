import simplenet.*;

import java.util.HashMap;

public class VRouter extends simplenet.Router{


    // hold separately my table
    HashMap<Integer,Double> routingTable = new HashMap<>();

    @Override
    public void initialize() {
        routingTable.put(my_address(),0.0);
        sendRoutingMessage(true);
    }

    @Override
    public void process_routing_message(RoutingMessage message, int interfx) {

        if (((VectorMessage)message).revealingName){
            int routerName = ((VectorMessage) message).routerName;
            if(!routingTable.containsKey(routerName)){
                routingTable.put(routerName, link_cost(interfx));
                set_forwarding_entry(routerName,interfx);
            }
//            else{
//                if(routingTable.get(routerName) > link_cost(interfx)){
//                    routingTable.put(routerName, link_cost(interfx));
//                    set_forwarding_entry(routerName,interfx);
//                }
//            }
            sendRoutingMessage(false);
        }else {

            HashMap<Integer,Double> table = ((VectorMessage) message).table;

            int routerName = ((VectorMessage)message).routerName;

            for(Integer key : table.keySet()){
                // If I have not seen this router I save it in my table
                if(!routingTable.containsKey(key)){
                    // I need to put the key with the weight to 
                    routingTable.put(key, link_cost(interfx)+table.get(key));
                    set_forwarding_entry(key,interfx);
                    sendRoutingMessage(false);
                }else{
                    if(routingTable.get(key) > (routingTable.get(routerName)+table.get(key))){
                        clear_forwarding_entry(key);
                        set_forwarding_entry(key,interfx);
                        sendRoutingMessage(false);
                    }
                }
            }
        }
    }

    public void sendRoutingMessage(boolean revealName){
        for(int i = 0; i < interfaces(); i++){
            VectorMessage mex = new VectorMessage();
            mex.routerName = my_address();
            mex.table = this.routingTable;
            mex.revealingName = revealName;
            send_message(mex,i);
        }
    }
}

class VectorMessage extends RoutingMessage{
    boolean revealingName;
    public int routerName;
    public HashMap<Integer,Double> table;
}
