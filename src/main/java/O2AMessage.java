import jade.lang.acl.ACLMessage;

public class O2AMessage {

    String type;
    ACLMessage obj;

    public O2AMessage (String communication, ACLMessage msg){
        type = communication;
        obj = msg;
    }
}
