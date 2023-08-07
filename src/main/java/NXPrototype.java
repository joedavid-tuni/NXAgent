import nxopen.*;
import nxopen.assemblies.Component;
import nxopen.assemblies.ComponentAssembly;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class NXPrototype {
    private static Session theSession;
    private static UFSession theUFSession;
    private static ListingWindow lw;

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

    public static void traverseAssembly(Component c) throws NXException, RemoteException {
        lw.writeLine(c.name());
        for(Component _c:c.getChildren())
            traverseAssembly(_c);
    }

    public static void main(String[] args) throws Exception {

        NXRemoteServer server = lookupServer();
        theSession = server.session();
        theUFSession = server.ufSession();
        lw = theSession.listingWindow();
        lw.writeLine("NXRemoting Setup successfully via JAVA RMI");


        PartCollection parts = theSession.parts();
        Part workPart = parts.work();

        ComponentAssembly componentAssembly = workPart.componentAssembly();
        lw.writeLine(componentAssembly.name());

        Component rootComponent = componentAssembly.rootComponent();
        lw.writeLine(rootComponent.displayName());

        Component[] childComponents = rootComponent.getChildren();

        traverseAssembly(rootComponent);





    }
}
