package fact;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.ProcessorList;
import stream.annotations.Parameter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Loop over values for some given processor key.
 *
 * @author alexey
 */
public class LoopValues extends ProcessorList {

    static Logger log = LoggerFactory.getLogger(LoopValues.class);

    @Parameter(required = true,
            description = "Key value ")
    private String key = "";

    @Parameter(required = true,
            description = "List of different values to be inserted for key-field")
    private String[] values = null;

    @Parameter(required = true,
            description = "List of different keys used for output.")
    private String[] outputKeys = null;

    private List<Processor> loopProcessors = new ArrayList<>(0);

    @Override
    public void init (ProcessContext context) throws Exception {
        super.init(context);

        // detect mismatch in parameter arrays length
        if (outputKeys.length != values.length) {
            String message = "Number of values for LoopValues is not the same" +
                    "as number of output keys.";
            log.error(message);
            throw new IllegalArgumentException(message);
        }

        searchProcessorsWithFields();

        // if no processor was found containing field named by key and
        // field 'outputKey', then do not start processing.
        if (loopProcessors.size() == 0) {
            String m = "No field '" + key + "' was found to be " +
                    "set within LoopValues.";
            log.error(m);
            throw new IllegalArgumentException(m);
        }
    }

    /**
     * Iterate through list of processors and identify those with a field named
     * by a key and another one field named 'outputKey'.
     */
    private void searchProcessorsWithFields () {
        for (Processor processor : processors) {
            try {
                Class<?> processorClass = processor.getClass();
                if (processorClass.getDeclaredField(key) != null) {
                    if (processorClass.getDeclaredField("outputKey") != null) {
                        loopProcessors.add(processor);
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("Processor {} doesn't contain field {}",
                        processor, key);
            }
        }
    }

    @Override
    public Data process (Data data) {
        int iteration = 0;
        for (String value : values) {
            for (Processor processor : loopProcessors) {
                try {
                    // try to set the right key-field to iterated value
                    Field declaredField = processor.getClass().getDeclaredField(key);
                    setFieldValue(value.trim(), processor, declaredField);

                    // try to set the right output-field to iterated value
                    declaredField = processor.getClass().getDeclaredField("outputKey");
                    setFieldValue(outputKeys[iteration].trim(),
                            processor, declaredField);
                } catch (NoSuchFieldException e) {
                    log.error("LoopValues could not retrieve declared " +
                            "fields {} and outputKey", key);
                    return null;
                }
            }
            data = super.process(data);
            iteration++;
        }
        return data;
    }

    /**
     * Set field of the given instance object to the given value.
     *
     * @param value    new value
     * @param instance instance to be changed
     * @param field    declared field of the object instance
     */
    private void setFieldValue (String value, Object instance, Field field) {
        Class<?> type = field.getType();
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            if (ClassUtils.isAssignable(type, String.class)) {
                field.set(instance, value);
            } else if (ClassUtils.isAssignable(type, Integer.class)) {
                Integer intValue = Integer.valueOf(value);
                field.setInt(instance, intValue);
            } else if (ClassUtils.isAssignable(type, Double.class)) {
                Double doubleValue = Double.valueOf(value);
                field.setDouble(instance, doubleValue);
            } else if (ClassUtils.isAssignable(type, Float.class)) {
                Float floatValue = Float.valueOf(value);
                field.setFloat(instance, floatValue);
            } else if (ClassUtils.isAssignable(type, Short.class)) {
                Short shortValue = Short.valueOf(value);
                field.setShort(instance, shortValue);
            } else if (ClassUtils.isAssignable(type, Long.class)) {
                Long longValue = Long.valueOf(value);
                field.setLong(instance, longValue);
            }
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            log.error("Field {} could not have been set", key);
        } catch (NumberFormatException e) {
            log.error("It was not possible to correctly get {} value out of {}",
                    type.getName(), value);
        }
    }
}

