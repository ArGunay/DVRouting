import simplenet.*;

import java.util.HashMap;

public class VRouter extends simplenet.Router{


    // hold separately my table
    HashMap<Integer,Double> routingTable = new HashMap<>();


    @Override
    public void process_routing_message(RoutingMessage message, int ifx) {
//        int revealingName = ((VectorMessage) routingMessage).routerName;

        if (message instanceof NameMessage){

            int rn = ((NameMessage) message).routerName;

            if(routingTable.containsKey(rn)){
                Double actual = routingTable.get(rn);
                if(actual > link_cost(ifx)){
                    routingTable.put(rn, link_cost(ifx));
                    set_forwarding_entry(rn,ifx);
                }
            }else{
                routingTable.put(rn, link_cost(ifx));
                set_forwarding_entry(rn,ifx);
            }
            
            for(int i = 0; i < interfaces(); i++){
                VectorMessage mex = new VectorMessage();
                mex.routerName = my_address();
                mex.table = routingTable;
                send_message(mex,i);

            }
        }else{
            HashMap<Integer,Double> tabl = ((VectorMessage) message).table;
            int rtr = ((VectorMessage)message).routerName;
            System.out.println("IAM: "+ my_address()+", "+ rtr+" tabl "+tabl);
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
