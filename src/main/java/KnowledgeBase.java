import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


//todo: check for redundant code blocks throughout
public class KnowledgeBase {

    private Dataset knowledgeBase;
    private String consoleFontFormat = new String();

    private int i = 0;
    private final SPARQLUtils su = new SPARQLUtils();

    private Integer portFuseki;

//    Gson gson = new Gson();

//    private Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//    private Reasoner reasoner = PelletReasonerFactory.theInstance().create();
//    private Reasoner reasoner2 = PelletReasonerFactory.theInstance().create();

//    FusekiServer fusekiServer;

    public KnowledgeBase(String gPath, String dBaseDir, Integer portFuseki) throws IOException {
//        String dBaseDir = "/home/robolab/Documents/Dataset";
        File dir = new File(dBaseDir);
        FileUtils.cleanDirectory(dir);

        this.knowledgeBase = TDBFactory.createDataset(dBaseDir);
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontModel.read(gPath, "RDF/XML");

        this.knowledgeBase.getDefaultModel().add(ontModel);
        this.portFuseki = portFuseki;
        this.consoleFontFormat = consoleFontFormat;

    }

    public Model getModel() {
        return this.knowledgeBase.getDefaultModel();
    }

    public void writeToConsole() {
        Model m = knowledgeBase.getDefaultModel();

        m.write(System.out, "turtle");
    }

    public void writeToFile(String fileName) throws IOException {
        // Mainly for Debugging purposes
        Model model = this.knowledgeBase.getDefaultModel();
//        Model model2 = this.knowledgeBase.getNamedModel("https://joedavid91.github.io/ontologies/camo-named-graph/intention");
        FileWriter out = new FileWriter(fileName);
        knowledgeBase.begin(TxnType.WRITE);

        try {
            model.write(out, "RDF/XML-ABBREV");
        } finally {
            try {
                out.close();
                knowledgeBase.commit();
                knowledgeBase.end();
            } catch (IOException closeException) {
                // ignore
            }
        }

    }

//    public void insertPartsAndBBoxCoords(Map<String, double[][]> componentName_minMaxMap) {
//
//        System.out.println(componentName_minMaxMap.size() + " parts found. Updating Parts in the Knowledge Base ..");
//
//        ArrayList<String> queries = new ArrayList<>();
//
//
//        for (var entry : componentName_minMaxMap.entrySet()) {
//
//            double[][] bboxCoords = entry.getValue();
//            double[] llc = bboxCoords[0];
//            double[] urc = bboxCoords[1];
//
//            String query = su.getPrefixes("camo", "xsd", "DUL") + "INSERT DATA {" +
//                    "camo:" + entry.getKey() + " a camo:Part. " +
//                    "camo:BB_" + entry.getKey() + " a camo:BoundingBox. " +
//                    "camo:" + entry.getKey() + "_BBXYZminmax a camo:3DBBMinMax. " +
//                    "camo:" + entry.getKey() + "_BBXYZmin a camo:XminYminZmin. " +
//                    "camo:" + entry.getKey() + "_BBXYZmax a camo:XmaxYmaxZmax. " +
//
//                    "camo:" + entry.getKey() + "_BBXYZminmax DUL:hasComponent camo:" + entry.getKey() + "_BBXYZmin. " +
//                    "camo:" + entry.getKey() + "_BBXYZminmax DUL:hasComponent camo:" + entry.getKey() + "_BBXYZmax. " +
//                    "camo:" + entry.getKey() + " camo:hasBoundingBox camo:BB_" + entry.getKey() + ". " +
//                    "camo:" + entry.getKey() + "_BBXYZmin DUL:parametrizes camo:BB_" + entry.getKey() + ". " +
//                    "camo:" + entry.getKey() + "_BBXYZmax DUL:parametrizes camo:BB_" + entry.getKey() + ". " +
//
//                    "camo:" + entry.getKey() + "_BBXYZmin camo:hasXValue \"" + llc[0] + "\"^^xsd:double. " +
//                    "camo:" + entry.getKey() + "_BBXYZmin camo:hasYValue \"" + llc[1] + "\"^^xsd:double. " +
//                    "camo:" + entry.getKey() + "_BBXYZmin camo:hasZValue \"" + llc[2] + "\"^^xsd:double. " +
//
//                    "camo:" + entry.getKey() + "_BBXYZmax camo:hasXValue \"" + urc[0] + "\"^^xsd:double. " +
//                    "camo:" + entry.getKey() + "_BBXYZmax camo:hasYValue \"" + urc[1] + "\"^^xsd:double. " +
//                    "camo:" + entry.getKey() + "_BBXYZmax camo:hasZValue \"" + urc[2] + "\"^^xsd:double. " +
//
//                    "}";
//
//            queries.add(query);
//
//
//        }
//
//        knowledgeBase.begin(TxnType.WRITE);
//
//        for (String query : queries) {
//            UpdateRequest uReq = UpdateFactory.create(query);
//            UpdateExecution.dataset(knowledgeBase).update(uReq).execute();
//        }
//
//
////        UpdateProcessor proc = UpdateExecutionFactory.create(uReq,graphStore)
//
//        knowledgeBase.commit();
//        knowledgeBase.end();
//
//
////    public void initFuseki() {
////        fusekiServer = FusekiServer.create()
////                .enableCors(true)
////                .port(portFuseki)
////                .add("/ds", knowledgeBase, true)
////                .build();
//////        System.out.println(consoleFontFormat + "Starting Fuseki on port " + portFuseki + ConsoleColors.RESET);
////        fusekiServer.start();
////    }
////
////    private void stopFusek() {
////        System.out.println("Stopping Fuseki");
////        fusekiServer.stop();
////    }
//    }

    public void insertPartsAndBBoxCoords(Map<String, double[][]> componentName_OBBMap) {

        System.out.println(componentName_OBBMap.size() + " parts found. Updating Parts in the Knowledge Base ..");

        ArrayList<String> queries = new ArrayList<>();

        for (var entry : componentName_OBBMap.entrySet()) {

            double[][] bboxCoords = entry.getValue();

            String query = su.getPrefixes("camo", "xsd", "DUL") + "INSERT DATA {" +
                    "camo:" + entry.getKey() + " a camo:Part. " +
                    "camo:BB_" + entry.getKey() + " a camo:BoundingBox. " +
                    "camo:" + entry.getKey() + " camo:hasBoundingBox camo:BB_" + entry.getKey() + ". " +
                    generateCornerPointsString(entry.getKey(), bboxCoords)+
                    "}";

            queries.add(query);


        }

        knowledgeBase.begin(TxnType.WRITE);

        for (String query : queries) {
            UpdateRequest uReq = UpdateFactory.create(query);
            UpdateExecution.dataset(knowledgeBase).update(uReq).execute();
        }


//        UpdateProcessor proc = UpdateExecutionFactory.create(uReq,graphStore)

        knowledgeBase.commit();
        knowledgeBase.end();


//    public void initFuseki() {
//        fusekiServer = FusekiServer.create()
//                .enableCors(true)
//                .port(portFuseki)
//                .add("/ds", knowledgeBase, true)
//                .build();
////        System.out.println(consoleFontFormat + "Starting Fuseki on port " + portFuseki + ConsoleColors.RESET);
//        fusekiServer.start();
//    }
//
//    private void stopFusek() {
//        System.out.println("Stopping Fuseki");
//        fusekiServer.stop();
//    }
    }

    private String generateCornerPointsString(String key, double[][] bboxCoords) {

        StringBuilder stringBuilder = new StringBuilder("");
        for(int i =0 ; i< bboxCoords.length; i++){

            stringBuilder.append("camo:").append(key).append("_BBCorner").append(i+1).append(" a camo:3DBBCorner. ")
                    .append("camo:").append(key).append("_BBCorner").append(i+1).append(" camo:parametrizes camo:BB_").append(key).append(". ")
                    .append("camo:").append(key).append("_BBCorner").append(i+1).append(" camo:hasXValue \"").append(bboxCoords[i][0]).append("\"^^xsd:double. ")
                    .append("camo:").append(key).append("_BBCorner").append(i+1).append(" camo:hasYValue \"").append(bboxCoords[i][1]).append("\"^^xsd:double. ")
                    .append("camo:").append(key).append("_BBCorner").append(i+1).append(" camo:hasZValue \"").append(bboxCoords[i][2]).append("\"^^xsd:double. ");
        }

        return stringBuilder.toString();

    }

    public boolean getPartExistence(String partName) {


        String query = su.getPrefixes("camo") + "ASK WHERE {" +
                "camo:" + partName + " a camo:Part. " +
                "}";
        boolean rs = false;


        knowledgeBase.begin(TxnType.READ);
        try (QueryExecution qExec = QueryExecution.create(query, knowledgeBase)) {
            rs = qExec.execAsk();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        knowledgeBase.end();

        return rs;


    }

    public double[][] getBBoxCoordinateOfPart(String partName) {

        System.out.println("BBox requested for part " + partName);

        boolean partexists = getPartExistence(partName);

        System.out.println("Exists? " + partexists);

        if(partexists) {

            double[][] XYZMinMax = new double[0][];

            String query = su.getPrefixes("camo","DUL") + " SELECT ?cornerPoint ?x ?y ?z WHERE { " +
                    " camo:" + partName + " a camo:Part. " +
                    " camo:" + partName + " camo:hasBoundingBox ?bbox. " +
                    "?cornerPoint camo:parametrizes ?bbox. " +
                    "?cornerPoint camo:hasXValue ?x. " +
                    "?cornerPoint camo:hasYValue ?y. " +
                    "?cornerPoint camo:hasZValue ?z. " +
                    "}";

            ArrayList<double[]> coords = new ArrayList<>();
            knowledgeBase.begin(TxnType.WRITE);

            try (QueryExecution qExec = QueryExecution.create(query, knowledgeBase)) {
                ResultSet rs = qExec.execSelect();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.nextSolution();
                    double x = sol.getLiteral("?x").getDouble();
                    double y = sol.getLiteral("?y").getDouble();
                    double z = sol.getLiteral("?z").getDouble();

                    double[] coord = new double[] {x, y, z};

                    coords.add(coord);
                }
            }
            knowledgeBase.commit();
            knowledgeBase.end();

            double [][] result = coords.toArray(new double[8][]);



            return  result;
        }
        else{
            System.out.println("Requested Part does not exist");
            return  null;
        }

    }


}