package fact.utils;

import stream.Keys;
import stream.Processor;
import stream.Data;
import stream.annotations.Parameter;

import java.io.Serializable;


/**
 * Processor that converts all keys to snake case and replaces mars class name stuff and hungarian notation.
 */
public class SanitizeKeys implements Processor {

    @Parameter(description = "Keys to convert to snake case", defaultValue = "* (all)")
    public Keys keys = new Keys("*");

    @Override
    public Data process(Data data) {


        for (String key: keys.select(data)) {

            Serializable value = data.remove(key);
            data.put(renameKey(key), value);

        }

        return data;
    }

    static String camelToSnakeCase(String string) {
        string = string.replaceAll("(.)([A-Z][a-z]+)", "$1_$2");
        string = string.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        string = string.toLowerCase();
        return string;
    }

    static String renameKey(String key) {
        if (key.equals("NPIX")) {
            return "num_pixel";
        }
        if (key.equals("RUNID")) {
            return "run_id";
        }
        if (key.equals("NROI")) {
            return "roi";
        }

        key = key.replaceFirst(".fVal$", "");
        key = key.replace(".f", "");
        key = camelToSnakeCase(key);

        key = key.replaceAll("_evt", "_event");

        key = key.replace("m_mc_event_", "ceres_event_");
        key = key.replace("m_pointing_pos_", "ceres_pointing_");
        key = key.replace("m_raw_", "ceres_raw_");
        key = key.replace("m_sim_source_pos_", "ceres_source_");
        key = key.replaceFirst("^m_", "");

        key = key.replace("_phot_elfrom", "_photo_electrons_from");
        key = key.replace("_phot_elin", "_photo_electrons_in");
        key = key.replace("_elec_", "_electron_");
        key = key.replace("_cph_", "_cherenkov_photon_");
        key = key.replace("_phot_", "_photon_");
        key = key.replace("_cher_", "_cherenkov_");
        key = key.replace("_arr_time_", "_arrival_time_");
        return key;
    }
}
