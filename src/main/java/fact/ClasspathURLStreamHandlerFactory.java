package fact;


import java.io.IOException;
import java.net.*;

public class ClasspathURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("classpath".equals(protocol)) {
            return new ClasspathURLStreamHandler();
        }

        return null;
    }

    public class ClasspathURLStreamHandler extends URLStreamHandler {
        /** The classloader to find resources from. */


        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            final URL resourceUrl =   ClasspathURLStreamHandler.class.getResource(u.getPath());
            return resourceUrl.openConnection();
        }

    }
}
