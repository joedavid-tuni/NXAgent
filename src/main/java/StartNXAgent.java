import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class StartNXAgent {





    public static void main(String[] args) throws StaleProxyException {

        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, "KBE-Software-Container");
        profile.setParameter(Profile.MAIN_HOST, "192.168.1.115");


        ContainerController kbe = runtime.createAgentContainer(profile);

        AgentController kbe_ac = kbe.createNewAgent("NXAgent", "NXAgent", new Object[0]);
        kbe_ac.start();

//        System.out.println(componentNameOBBMap);
//        Component[] childComponents = rootComponent.getChildren();
//
//        String interestedComponent = "ROCKERARM7";
//
//        for(Component c:childComponents){
//            if(Objects.equals(c.name(), interestedComponent)){
//                double [][] boundingBoxCoords = fetchOrientedBoundingBoxCoords(c);
//                drawBBoxFrom8Corners(boundingBoxCoords, 216);
//            }
//        }


    }
}
