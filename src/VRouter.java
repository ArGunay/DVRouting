import simplenet.*;

import java.util.HashMap;

public class VRouter extends simplenet.Router{


    // hold separately my table
    HashMap<Integer,Double> routingTable = new HashMap<>();

    @Override
    public void process_routing_message(RoutingMessage message, int ifx) {

        if (message instanceof NameMessage){
            int rn = ((NameMessage) message).routerName;
            if(routingTable.containsKey(rn)){
                if(routingTable.get(rn) > link_cost(ifx)){
                    routingTable.put(rn, link_cost(ifx));
                    set_forwarding_entry(rn,ifx);
                }
            }else{
                routingTable.put(rn, link_cost(ifx));
                set_forwarding_entry(rn,ifx);
            }
            sendRoutingMessage();
        }else{
            //Here we get the table of a neigbor router with his weights.
            // GET TABLE
            HashMap<Integer,Double> tabl = ((VectorMessage) message).table;
            // GET ROUTERNAME
            int rtr = ((VectorMessage)message).routerName;

            for(Integer key : tabl.keySet()){
                // If I have not seen this router I save it in my table
                if(!routingTable.containsKey(key)){
                    routingTable.put(key, link_cost(ifx)+tabl.get(key));
                    set_forwarding_entry(key,ifx);
                    sendRoutingMessage();
                }else{
                    if(routingTable.get(key) > (routingTable.get(rtr)+tabl.get(key))){
                        clear_forwarding_entry(key);
                        set_forwarding_entry(key,ifx);
                        sendRoutingMessage();
                    }
                }
            }
        }
    }

    public void sendRoutingMessage(){
        for(int i = 0; i < interfaces(); i++){
            VectorMessage mex = new VectorMessage();
            mex.routerName = my_address();
            mex.table = routingTable;
            send_message(mex,i);
        }
    }

    @Override
    public void initialize() {
        for(int i = 0; i < interfaces();i++){
            NameMessage message = new NameMessage(my_address());
            message.routerName = my_address();
            send_message(message,i);
        }
    }

}


class NameMessage  extends RoutingMessage {
    public int routerName;
    public NameMessage(int routername) {
        this.routerName = routername;
    }
}

class VectorMessage extends RoutingMessage{
    public int routerName;
    public HashMap<Integer,Double> table;
}
