package nitinka.jmetrics.util;

import org.codehaus.jackson.map.*;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class ObjectMapperUtil {
    private static ObjectMapper objectMapper;

    public static ObjectMapper instance() {
        if(objectMapper == null)
            objectMapper = new ObjectMapper();
        return objectMapper;
    }
}
