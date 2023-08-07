import com.google.gson.Gson;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import nxopen.ListingWindow;
import nxopen.NXException;
import nxopen.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.java_websocket.client.WebSocketClient;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

class CheckForO2A extends CyclicBehaviour {

    @Override
    public void action() {

        O2AMessage object = (O2AMessage) myAgent.getO2AObject();

        if(object != null){
            if(object.type.equals("communication")){
                ACLMessage msg = object.obj;
                System.out.println("[" + myAgent.getAID().getLocalName() + "] " + " O2A Message Received " + msg.getContent());
                myAgent.send(msg);
            }
        }
        else{
            block();
        }
    }
}




public class NXAgent extends Agent {

    WebSocketClient agent = null;
    Session theSession;
    protected KnowledgeBase kb;
    protected KBES nx;
    Gson gson = new Gson();
//    initKB kbe_kb = new initKB();

    private static NXRemoteServer lookupServer() throws Exception
    {
        NXRemoteServer server = null;
        String host = System.getProperty("nxexamples.remoting.host");
        if ( host == null || host.equals(""))
            host = "localhost";
        String serverName = System.getProperty("nxexamples.remoting.servername");
        if ( serverName == null )
            serverName = "NXServer";
        int bindTimeout = 0;
        if ( System.getProperty("nxexamples.remoting.rmilookuptimeout") != null)
            bindTimeout = Integer.parseInt(System.getProperty("nxexamples.remoting.rmilookuptimeout"));

        String name = "//" + host + "/" + serverName;
        System.out.println("Looking up name of server");
        int time = 0;
        // Look up the server.  Keep trying until it is found or
        // the amount of time we have tried exceeds the amount specified
        // in the property nxexamples.remoting.rmilookuptimeout
        do
        {
            try
            {
                server = (NXRemoteServer) Naming.lookup(name);
            }
            catch ( NotBoundException e )
            {
                time += 1000;
                if ( time > bindTimeout )
                    throw e;
                Thread.sleep(1000);
            }
            catch ( ConnectException e )
            {
                time += 1000;
                if ( time > bindTimeout )
                    throw e;
                Thread.sleep(1000);
            }

        }
        while(server == null);
        System.out.println("Name of server found");
        return server;
    }


    public void setup(){

        System.out.println("Hello from JADE Agent");
        System.out.println("My local name is " + getAID().getLocalName());
        System.out.println("My GUID name is " + getAID().getName());
        System.out.println("Proceeding to establish communication with KBE Software. If successful, you should" +
                "see a message from me there.");


//          COMMENTED TO PREVENT NX USAGE WHILE DEVELOPING OTHER PARTS
        try {
            NXRemoteServer server = lookupServer();
            nx = new KBES(server);
            Session theSession = server.session();
            ListingWindow lw = theSession.listingWindow();
            lw.writeLine("Hi, this is " + getAID().getLocalName() + ". I have been able to successfully establish communication" +
                    "with NX.");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
//        try {
//            KnowledgeBase kb = new KnowledgeBase("C:\\Users\\david\\Documents\\Ontologies\\productCapOv3.rdf","C:\\Users\\david\\Documents\\Dataset",5000);
//            kb.writeToConsole();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        addBehaviour( new AchieveREResponder(this, mt){
//            protected ACLMessage prepareResultNotification(ACLMessage request,ACLMessage response) {
//
//                ACLMessage informDone = request.createReply();
//                informDone.setPerformative(ACLMessage.INFORM);
//                informDone.setContent("inform done");
//                return informDone;
//            }
//        });




        // Couldn't get connection to remote websocket server to work 24.11.2021

//        try{
////            agent = new WSClient( new URI("ws://127.0.0.1:8887"));
//            agent = new WSClient( new URI("ws://192.168.10.108:8887"));
//            agent.connect();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

        setEnabledO2ACommunication(true, 0);
        addBehaviour(new CheckForO2A());
        addBehaviour(new initKB());
        addBehaviour(new CheckForMessages());

//        COMMENTED DUE TO ERROR WITH STARTING FUSEKI WHICH AGAIN WAS NOT NEEDED 28.09
//        addBehaviour(kbe_kb);

//        Object[] args = getArguments();
//        System.out.println(args);
//        theSession  = (Session) args[0];


    }
//    @Override
//    protected void takeDown(){
//        System.out.println("[" + getAID().getLocalName() + "] Shutting Down ..."  );
//        System.out.println(" Doing Clean up");
//        kbe_kb.stopFuseki();
//    }

    class initKB extends OneShotBehaviour {

        public FusekiServer fusekiServer;



        @Override
        public void action(){
            System.out.println("Attempting to initialize Agent KB");

            try {
                kb = new KnowledgeBase("C:\\Users\\david\\Documents\\Ontologies\\productCapOv3.rdf","C:\\Users\\david\\Documents\\Dataset",5000);
                nx.initializeParts();
//                nx.drawBBoxForPart("RockerArm");
                kb.insertPartsAndBBoxCoords(nx.getComponentName_OBBMap());

                kb.writeToFile("Debug.rdf");

            } catch (IOException | NXException e) {
                e.printStackTrace();
            }

//            String directory = "C:\\Users\\david\\Documents\\MyDatabases\\Dataset2";
//            Dataset dataset = TDBFactory.createDataset(directory);
//            String ontPath = "C:\\Users\\david\\OneDrive - TUNI.fi\\HRC\\Ontologies\\JoeNew\\productCap.owl";
//
//            Model KB = RDFDataMgr.loadModel(ontPath);
//            dataset.addNamedModel("knowledgebase",KB);
//
//            fusekiServer = FusekiServer.create()
//                    .enableCors(true)
//                    .port(3002)
//                    .add("/ds", dataset, true)
//                    .build();
//
//            System.out.println("Starting KB");
//            fusekiServer.start();

        }

        public void stopFuseki(){
            System.out.println("Stopping Fuseki");
            fusekiServer.stop();
        }
    }


    class CheckForMessages extends CyclicBehaviour{


        @Override
        public void action(){

            MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
            MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);

            ACLMessage queryIfMsg = myAgent.receive(mt1);// todo: remove, currently not being used
            ACLMessage queryRefMsg = myAgent.receive(mt2);

            if(queryIfMsg!=null){

                System.out.println("[" + myAgent.getAID().getLocalName() + "] Message Received from " + queryIfMsg.getSender().getLocalName() + ":  " + queryIfMsg.getContent());

                if(Objects.equals(queryIfMsg.getConversationId(),"Query Part Existence")) {
                    boolean existence = false;
                    String partName = queryIfMsg.getContent();
                    existence = kb.getPartExistence(partName);

                    // All messages at the point of coding 28.11 are supposed to be requiring for bbox coords, if more kinds come in future have additional logic
                    ACLMessage reply = queryIfMsg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(String.valueOf(existence));

                    myAgent.send(reply);
                }
                else {

                    System.out.println("[" + myAgent.getAID().getLocalName() + "] Unknown query " + queryIfMsg.getSender().getLocalName() + ":  " + queryIfMsg.getContent());

                    ACLMessage nu = queryIfMsg.createReply();
                    nu.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    nu.setContent("I have not understood your query");
                    myAgent.send(nu);

                }
            }
            else if (queryRefMsg!=null) {

                System.out.println("[" + myAgent.getAID().getLocalName() + "] Message Received from " + queryRefMsg.getSender().getLocalName() + ":  " + queryRefMsg.getContent());

                if(Objects.equals(queryRefMsg.getConversationId(),"Query Part Existence")){

                    ArrayList<String> temp = new ArrayList<>();
                    String partNamesString = queryRefMsg.getContent();
                    String[] partNames = partNamesString.split(",");
                    for(String partName:partNames){
                        double[][] coordinates = kb.getBBoxCoordinateOfPart(partName);
                        temp.add(Arrays.deepToString(coordinates));
                    }
                    System.out.println("DEBUG==========" +temp);
                    System.out.println("DEBUG==========" +temp.size());

                    if(temp.size()>0){
                        ACLMessage reply = queryRefMsg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
//                        reply.setConversationId("Bounding Box Coordinates");

                        reply.setContent(StringUtils.join(temp,","));

                        myAgent.send(reply);
                    }
                    else{

                        ACLMessage reply = queryRefMsg.createReply();
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setConversationId("Bounding Box Coordinates");
                        reply.setContent("null");
                        myAgent.send(reply);
                    }
                }
            }
        }
    }
}
