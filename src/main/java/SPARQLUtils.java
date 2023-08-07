import java.util.HashMap;
import java.util.Map;

public class SPARQLUtils {
    String pfx_camo = "https://joedavid91.github.io/ontologies/camo/product";
    String pfx_xsd = "http://www.w3.org/2001/XMLSchema";

    String pfx_dul = "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl";
    String pfx_sh = "http://www.w3.org/ns/shacl";
    String pfx_cm = "http://resourcedescription.tut.fi/ontology/capabilityModel";

    String pfx_rdfs = "http://www.w3.org/2000/01/rdf-schema";
    String pfx_owl = "http://www.w3.org/2002/07/owl";
    String separator = "#";

    Map<String, String> prefixMap = new HashMap<>();

    public SPARQLUtils() {
        // there is Jena utility functions for this
        prefixMap.put("camo", pfx_camo);
        prefixMap.put("xsd", pfx_xsd);
        prefixMap.put("DUL", pfx_dul);
        prefixMap.put("sh", pfx_sh);
        prefixMap.put("cm", pfx_cm);
        prefixMap.put("rdfs", pfx_rdfs);
        prefixMap.put("owl", pfx_owl);


    }

    public String getPrefixes(String... prefixes) {
        StringBuilder str = new StringBuilder("");


        for (String prefix : prefixes) {
            if (prefixMap.containsKey(prefix)) {
                str.append("PREFIX ").append(prefix).append(":<").append(prefixMap.get(prefix)).append(separator).append("> ");
            }
        }
        return str.toString();
    }
}