package fact.features;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.AbstractProcessor;
import stream.Data;
import stream.annotations.Parameter;

import java.io.Serializable;
import java.util.HashMap;


/**
 * Generate feature with a given name using list of parameters in some calculation with optional
 * mathematical parameters
 *
 * @author alexey
 */
public class GenerateFeatures extends AbstractProcessor {

    static Logger log = LoggerFactory.getLogger(GenerateFeatures.class);

    /**
     * List of parameters in a form "name1::type1,name2::type2"
     */
    String parameterList = "";

    /**
     * Name of the new feature
     */
    @Parameter(required = true)
    String name = "";

    /**
     * Mathematical expression that is to be used to calculate the value of the new feature. The
     * parameters used there should match those in the list of parameters.
     */
    @Parameter(required = true)
    String calculation = "";

    /**
     * Some additional mathematical parameters such as 'pi' or 'e' need to be mentioned they are
     * used in the expression as a comma separated list as 'pi, e'. They will be added to the list
     * of substituted parameters for the expression.
     */
    String mathParameters = "";

    /**
     * Prefix is optional and can be defined to be added in front of the name of the new feature,
     * such as "feature"
     */
    String prefix = "";

    @Override
    public Data process (Data input) {

        // parameterList, calculation and name must be declared.
        if (parameterList.equals("") || name.equals("") || calculation.equals("")) {
            log.warn("Parameters \"parameterList\", \"calculation\" and \"name\" must all be set.");
        } else if (calculation.contains(",")) {
            log.warn("Parameter 'calculation' contains ',' (comma):\n" + calculation
                    + "\nPlease use '.' for decimal and float numbers.");
        } else {
            if (require(parameterList, input)) {
                String[] vars = parameterList.split(",");
                HashMap<String, Double> params = new HashMap<>();
                for (String s : vars) {
                    String kv = s.split("::")[0].trim();
                    params.put(kv, Double.valueOf(input.get(kv).toString()));
                }

                for (String mathParam : mathParameters.split(",")) {
                    switch (mathParam.toLowerCase().trim()) {
                        case "pi":
                            params.put("pi", Math.PI);
                            break;
                        case "e":
                            params.put("e", Math.E);
                            break;
                        default:
                    }
                }

                try {
                    double result = parseCalculation(calculation, params);
                    log.debug("Adding feature: " + getPrefix() + name + " with " + result);
                    input.put(getPrefix() + name, result);
                } catch (IllegalArgumentException exc) {
                    log.error("Unable to set variables for the expression: \n"
                            + "Given feature calculation: " + calculation + "\n"
                            + "Given parameter list: " + params.toString() + "\n"
                            + name + " feature was not set.");
                }

            }
        }
        return input;
    }

    /**
     * Using a string and map of parameter names with values this method can calculate the value of
     * the given expression.
     *
     * @param calculation    mathematical expression to calculate the value of the new feature
     * @param parameter_list map of parameters with values that should match those used in
     *                       mathematical expression
     * @return double value calculated with the "calculation" parameter
     * @throws IllegalArgumentException exception can be thrown in the case when the list of
     *                                  parameters doesn't match those parameters used in the
     *                                  "calculation" string
     */
    private double parseCalculation (String calculation, HashMap<String, Double> parameter_list)
            throws IllegalArgumentException {
        Function sgn = new Function("signum", 1) {
            @Override
            public double apply(double... args) {
                if (args[0] > 0)
                    return 1;
                else if (args[0] < 0)
                    return -1;
                else
                    return 0;
            }
        };
        Expression e = new ExpressionBuilder(calculation)
                .function(sgn)
                .variables(parameter_list.keySet()).build()
                .setVariables(parameter_list);
        return e.evaluate();
    }

    /**
     * Check if data item contains list of parameters with certain types.
     *
     * @param paramList list of parameters given in a form
     *                  'name1::type1,name2::type2,...'
     * @param item      data item
     */
    private boolean require (String paramList, Data item) {
        int errors = 0;

        // split pairs of parameters
        String[] vars = paramList.split(",");

        final int NAME = 0;
        final int TYPE = 1;
        for (String s : vars) {

            // split parameter pair to name and type
            String[] kv = s.split("::");

            if (kv.length == 2) {
                kv[NAME] = kv[NAME].trim();
                kv[TYPE] = kv[TYPE].trim();
                if (!item.containsKey(kv[NAME])) {
                    log.warn("Missing attribute '{}'", kv[NAME]);
                    errors++;
                    continue;
                }

                Serializable value = item.get(kv[NAME]);
                String type = value.getClass().getName().replace("java.lang.", "");
                if (!type.equalsIgnoreCase(kv[TYPE])) {
                    log.warn(
                            "Type '{}' does not match required type '{}' for key '"
                                    + kv[NAME] + "'", type, kv[TYPE]);
                    errors++;
                }
            } else {
                log.warn("Wrong usage of the parameter list {}. " +
                        "Preferred one is 'name1::type1,name2::type2...'", s);
                errors++;
            }
        }

        return errors == 0;
    }

    public void setParameterList (String parameterList) {
        this.parameterList = parameterList;
    }

    public String getParameterList () {
        return parameterList;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return name;
    }

    public void setCalculation (String calculation) {
        this.calculation = calculation;
    }

    public String getCalculation () {
        return calculation;
    }

    public void setPrefix (String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix () {
        return prefix.equals("") ? "" : prefix + ":";
    }

    public String getMathParameters () {
        return mathParameters;
    }

    public void setMathParameters (String mathParameters) {
        this.mathParameters = mathParameters;
    }
}
