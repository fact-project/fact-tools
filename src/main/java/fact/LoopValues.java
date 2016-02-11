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

/**
 * Loop over values for some given processor key.
 *
 * @author alexey
 */
public class LoopValues extends ProcessorList {

    static Logger log = LoggerFactory.getLogger(LoopValues.class);

    @Parameter(required = true)
    private String key = "";

    @Parameter(required = true)
    private String[] values = null;

    @Parameter(required = true)
    private String[] outputKeys = null;
    // liste von outputKeys


    @Override
    public void init (ProcessContext context) throws Exception {
        super.init(context);
        if (outputKeys.length != values.length) {
            String message = "Number of values for LoopValues is not the same" +
                    "as number of output keys.";
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public Data process (Data data) {
        int iteration = 0;
        for (String value : values) {
            for (Processor processor : processors) {
                Field[] declaredFields = processor.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    // try to set the right key-field to a given value
                    if (declaredField.getName().equals(key)) {
                        setFieldValue(value.trim(), processor, declaredField);
                    }

                    // try to set the right output-field to iterated value
                    if (declaredField.getName().equals("outputKey")) {
                        setFieldValue(outputKeys[iteration].trim(),
                                processor, declaredField);
                    }
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

