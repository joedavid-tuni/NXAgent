import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

public class TestFuseki {


    public static void main(String[] args){
        String directory = "C:\\Users\\david\\Documents\\MyDatabases\\Dataset2";
        Dataset dataset = TDBFactory.createDataset(directory);
        String ontPath = "C:\\Users\\david\\OneDrive - TUNI.fi\\HRC\\Ontologies\\JoeNew\\product.owl";

        Model KB = RDFDataMgr.loadModel(ontPath);
        KB.write(System.out, "RDF/XML");
        dataset.addNamedModel("knowledgebase",KB);




        FusekiServer fusekiServer = FusekiServer.create()
                .port(3001)
                .add("/ds", dataset, true)
                .build();

        fusekiServer.start();




    }


}
