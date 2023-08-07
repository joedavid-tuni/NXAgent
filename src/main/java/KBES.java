import nxopen.*;
import nxopen.assemblies.Component;
import nxopen.assemblies.ComponentAssembly;
import nxopen.uf.UFDisp;
import nxopen.uf.UFFltr;
import nxopen.uf.UFObj;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KBES {

    private Session theSession= null; // tempp

    private UFSession theUFSession = null;
    private ComponentAssembly componentAssembly;
    Map<String, double[][]> componentName_OBBMap;
    Map<String, double[][]> componentName_MinMaxMap;

    public Map<String, double[][]> getComponentName_OBBMap() {
        return componentName_OBBMap;
    }

    public Map<String, double[][]> getComponentName_MinMaxMap() {
        return componentName_MinMaxMap;
    }

    public KBES(NXRemoteServer server) throws NXException, RemoteException {
        this.theSession = server.session();
        this.theUFSession = server.ufSession();
    }

    public void initializeParts() throws NXException, RemoteException {
        System.out.println("Checking for parts..");
        PartCollection parts = theSession.parts();
        Part workPart = parts.work();
        componentAssembly = workPart.componentAssembly();

        // todo: make sure some part is detected

        Component rootComponent = componentAssembly.rootComponent();

        componentName_OBBMap = getBoundingBoxCoordinates(rootComponent, "8 corners");

        componentName_MinMaxMap = getBoundingBoxCoordinates(rootComponent, "2 corners");



    }

    public Map<String, double[][]> getBoundingBoxCoordinates(Component component, String form) throws NXException, RemoteException {

        Map<String, double[][]> componetBBC = new HashMap<>();

        System.out.println("\n\n Traversing Component "+ component.name());

        Component[] childComponents = component.getChildren();

        for (Component childComponent : childComponents) {
            Map tmp = getBoundingBoxCoordinates(childComponent, form);
            tmp.keySet().removeAll(componetBBC.keySet());
            componetBBC.putAll(tmp);
        }

        if(component.parent() != null) { // if this is a root component that cannot use moveComponent Function
            double[][] OBBCoords = fetchOrientedBoundingBoxCoords(component, form);

            String[] words = component.name().toLowerCase().split(" ");

            // capitalize each word
            for (int i = 0; i < words.length; i++)
            {
                words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
            }

            // rejoin back into a sentence
            String name = String.join("", words);
            componetBBC.put(name, OBBCoords);


        }

        return componetBBC;
    }
    public  void drawLine(double[] start, double[] stop, int colour) throws RemoteException, NXException {
//
        UFObj.DispProps dispProps = new UFObj.DispProps();
        dispProps.blankStatus = 1;
        dispProps.color = colour;
        dispProps.font = 1;
        dispProps.layer = 1;
        dispProps.lineWidth =1;
        dispProps.highlightStatus = true;

        theUFSession.disp().displayTemporaryLine(new Tag(1), UFDisp.ViewType.USE_ACTIVE_MINUS, start, stop,dispProps);

    }

    private  double[][] fetchOrientedBoundingBoxCoords(Component component, String form) throws NXException, RemoteException {

        // determining pose of the component reported by NX
        Component.PositionData positionData = component.getPosition();


        Matrix3x3 origOrientationMat = positionData.orientation;
        Point3d origPositionVec = positionData.position;


        double theta_z = Math.atan2(origOrientationMat.yy, origOrientationMat.xx);
        double origAngle = 1.0 / Math.tan( Math.toRadians(theta_z));

        // ========== http://www.nxjournaling.com/content/getpositionpoint3d-matrix3x3-component-class

        double RX1 = Math.atan2(origOrientationMat.yz, origOrientationMat.zz);
        double c2 = Math.sqrt(Math.pow(origOrientationMat.xx,2) + Math.pow(origOrientationMat.xy,2));
        double RY1 = Math.atan2(origOrientationMat.xz, c2);
        double s1 = Math.sin(RX1);
        double c1 = Math.cos(RX1);
        double RZ1 = Math.atan2(s1*origOrientationMat.zx - c1 * origOrientationMat.yx, c1*origOrientationMat.yy - s1 *origOrientationMat.zy);

        RX1 *= (180 / Math.PI);
        RY1 *= (180 / Math.PI);
        RZ1 *= (180 / Math.PI);

        System.out.println(RX1 + " " + RY1 + " " + RZ1);

        // ==========

        System.out.println(origAngle);
        System.out.println(Math.round(origAngle));
        System.out.println(Math.round(origAngle) % 90);


        UFFltr.AskBoxOfAssyData AABBData;


        double rotAngle;

        if (Math.round(RZ1) % 90 != 0){
            System.out.println("Rotating Section");
            // Angle by which you should rotate to make it axis-aligned to compute "minimum" bounding box

//            rotAngle = Math.abs(RZ1);
            rotateComponent(component, RZ1); //temporary rotation
            AABBData = theUFSession.fltr().askBoxOfAssy(component.tag());
            rotateComponent(component, -RZ1); // undo temporary rotation
        }
        else{
            System.out.println("Not Rotating Section");
            // No need to rotate, component already at 90 degree
            rotAngle = 0.0;
            AABBData = theUFSession.fltr().askBoxOfAssy(component.tag());


        }

        double[] centroid_AABB = AABBData.centroid;
        double[] corner_AABB = AABBData.corner;
        double[] orientation_AABB = AABBData.orientation;


        double[] lowerLeftCorner = theUFSession.vec3().sub(centroid_AABB, corner_AABB);
        double[] upperRightCorner = theUFSession.vec3().add(centroid_AABB, corner_AABB);





        double[][] AABBoxCorners = getCornersFromMinMax(lowerLeftCorner, upperRightCorner);

//        drawBBoxFrom8Corners(AABBoxCorners);

        double[][] OBBCorners = rotateCorners(AABBoxCorners, RZ1);

        if(Objects.equals(form, "2 corners")){
//            double[][] llcUrc = new double[][] {lowerLeftCorner, upperRightCorner};
            System.out.println(Arrays.deepToString(OBBCorners));
            double[][] llcUrc = {OBBCorners[0], OBBCorners[6]};
            System.out.println("llc, urc" + Arrays.deepToString(llcUrc));
            return  llcUrc;
        }

        return OBBCorners;

    }

    private int getNumberOfDifferentCoords(double[] corner1, double[] corner2){
        int n = 0;
        if(corner1[0] != corner2[0]) n+=1;

        if(corner1[1] != corner2[1]) n+=1;

        if(corner1[2] != corner2[2]) n+=1;

        return n;

    }

    private void drawBBoxFrom8Corners(double[][] aabBoxCorners, int color) throws NXException, RemoteException {

        // this part doesnt expect corners in a specific order
//        for(double[] corner1: aabBoxCorners){
//            for(double[] corner2 : aabBoxCorners){
//                if((getNumberOfDifferentCoords(corner1, corner2) > 0) && (getNumberOfDifferentCoords(corner1,corner2) <3)){
//                    drawLine(corner1,corner2,216);
//                }
//            }
//
//        }


        // this part expects corners in a specific order
        drawLine(aabBoxCorners[0], aabBoxCorners[1], color);
        drawLine(aabBoxCorners[1], aabBoxCorners[2], color);
        drawLine(aabBoxCorners[2], aabBoxCorners[3], color);
        drawLine(aabBoxCorners[3], aabBoxCorners[0], color);

        drawLine(aabBoxCorners[4], aabBoxCorners[5], color);
        drawLine(aabBoxCorners[5], aabBoxCorners[6], color);
        drawLine(aabBoxCorners[6], aabBoxCorners[7], color);
        drawLine(aabBoxCorners[7], aabBoxCorners[4], color);

        drawLine(aabBoxCorners[0], aabBoxCorners[4], color);
        drawLine(aabBoxCorners[1], aabBoxCorners[5], color);
        drawLine(aabBoxCorners[2], aabBoxCorners[6], color);
        drawLine(aabBoxCorners[3], aabBoxCorners[7], color);
    }

    private double[][] getCornersFromMinMax(double[] llc, double[] urc) {

        double[] corner1 = {llc[0], llc[1], llc[2]}; //LLC Point
        double[] corner2 = {urc[0], llc[1], llc[2]};
        double[] corner5 = {llc[0], urc[1], llc[2]};
        double[] corner6 = {urc[0], urc[1], llc[2]};

        double[] corner3 = {urc[0], llc[1], urc[2]};
        double[] corner4 = {llc[0], llc[1], urc[2]};
        double[] corner7 = {urc[0], urc[1], urc[2]};  //URC Point
        double[] corner8 = {llc[0], urc[1], urc[2]};

        return new double[][] {corner1, corner2, corner3, corner4, corner5, corner6, corner7, corner8};
    }


    public double[][] rotateCorners(double[][] corners, double rotAngle) throws NXException, RemoteException {


        double[][] rotatedCorners = new double[corners.length][3];
        int i = 0;

        rotAngle = Math.toRadians(rotAngle);

        for(double[] corner: corners){
            rotatedCorners[i][0]  = (corner[0] * Math.cos(rotAngle)) - (corner[1] * Math.sin(rotAngle));
            rotatedCorners[i][1]  = (corner[0] * Math.sin(rotAngle)) + (corner[1] * Math.cos(rotAngle));
            rotatedCorners[i][2]  = corner[2]; // keeping z unchanged
            i++;
        }


        return rotatedCorners;
    }

    public void rotateComponent(Component component, double rotAngle) throws NXException, RemoteException {


        Vector3d point3d = new Vector3d();

        Component.PositionData positionData = component.getPosition();
        Point3d origPositionVec = positionData.position;
        Matrix3x3 origOrientationMat = positionData.orientation;

        double mat_trace = origOrientationMat.xx + origOrientationMat.yy + origOrientationMat.zz;

        rotAngle = Math.toRadians(rotAngle);

        point3d.x = 0;
        point3d.y = 0;
        point3d.z = 0;

        Matrix3x3 rotMat = new Matrix3x3();
        rotMat.xx = Math.cos(rotAngle);
        rotMat.xy = -Math.sin(rotAngle);
        rotMat.xz = 0;
        rotMat.yx = Math.sin(rotAngle);
        rotMat.yy = Math.cos(rotAngle);
        rotMat.yz = 0;
        rotMat.zx = 0;
        rotMat.zy = 0;
        rotMat.zz = 1;

//        System.out.println(rotMat);

//        System.out.println("Moving Component " +component.name() );
        componentAssembly.moveComponent(component, point3d, rotMat);

    }

//    public void drawBBoxForAllParts() throws NXException, RemoteException {
//
//
//        // FOR DEBUG PURPOSES ONLY
//        for (Map.Entry<String,double[][]> entry : componentName_OBBMap.entrySet()){
//            drawBBoxFrom8Corners(entry.getValue(),216);
//        }
//
//
//    }

    public void drawBBoxForPart(String partName) throws NXException, RemoteException {


        double[][] coords8= componentName_OBBMap.get(partName);
        System.out.println(Arrays.deepToString(coords8));
        // FOR DEBUG PURPOSES ONLY
//        for (Map.Entry<String,double[][]> entry : componentName_OBBMap.entrySet()){
        drawBBoxFrom8Corners(coords8,216);
        double[][] tempLlcUrc = {coords8[0], coords8[6]};
        System.out.println("Temp LLC URC " + Arrays.deepToString(tempLlcUrc) );

        double[][] tempCorners = getCornersFromMinMax(tempLlcUrc[0], tempLlcUrc[1]);
        System.out.println("Temp Corners  " + Arrays.deepToString(tempCorners) );


//        }


    }
}
