package org.cloud.gateway.transport.netty;


public class Bootstrap {

    public static void main(String[] args) {
        new Bootstrap().start();
    }

    public void start() {
        System.out.println("Zuul Sample: starting up.");
        long startTime = System.currentTimeMillis();
        int exitCode = 0;

        Server server = null;

        try {
       
            
            BaseServerStartup serverStartup = new SampleServerStartup();
            
            server = serverStartup.server();

            long startupDuration = System.currentTimeMillis() - startTime;
            System.out.println("finished startup. Duration = " + startupDuration + " ms");

            server.start(true);
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.err.println("###############");
            System.err.println("initialization failed. Forcing shutdown now.");
            System.err.println("###############");
            exitCode = 1;
        }
        finally {
            // server shutdown
            if (server != null) server.stop();

            System.exit(exitCode);
        }
    }
}

