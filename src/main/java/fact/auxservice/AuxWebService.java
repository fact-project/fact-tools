package fact.auxservice;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import stream.service.Service;
import us.monoid.web.Resty;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


/**
 *
 * Created by kai on 01.03.15.
 */
public class AuxWebService implements Service {
    private ImmutableSet<String> s;

    boolean isInit = false;

    Resty r = new Resty();
    Gson gson = new Gson();

    private void init(){

        try {
            ArrayList<String> services = new ArrayList<>();
            String json = r.text("http://127.0.0.1:5000/services").toString();
            services = gson.fromJson(json, services.getClass());
            s = new ImmutableSet.Builder<String>().addAll(services).build();
            isInit = true;
        } catch (IOException e) {
            isInit = false;
            e.printStackTrace();
        }
    }
    /**
     * TODO find out mysterious conversion between facttime and Date
     * @param serviceName
     * @param from
     * @param to
     * @return
     */
    public ArrayList<Map<String, Serializable>> getAuxiliaryData(String serviceName, Date from, Date to) throws IOException {
        if(!isInit){
            init();
        }
        if(!s.contains(serviceName)){
            throw new ServiceDoesNotExistException();
        }

        ArrayList<Map<String, Serializable>> result = new ArrayList<>();
        String json = r.text("http://127.0.0.1:5000/aux/" + serviceName +"?from=16400.0&to=16400.5").toString();
//        System.out.println(json);
        result = gson.fromJson(json, result.getClass());
        System.out.println(result);
        return null;
    }
    @Override
    public void reset() throws Exception {

    }

    public class ServiceDoesNotExistException extends RuntimeException {
    }
}
