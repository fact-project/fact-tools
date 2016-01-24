package fact.rta;

import stream.service.Service;

import static spark.Spark.get;

/**
 * Created by kai on 24.01.16.
 */
public class RTAWebService implements Service {

    public RTAWebService() {
        System.out.println("Starting webservice!");
        get("/hello", (request, response) -> "Hello World!");
    }

    @Override
    public void reset() throws Exception {

    }
}
