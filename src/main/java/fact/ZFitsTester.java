package fact;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fact.io.hdureader.FITSStream;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to automaticly test whether a file could be read by the HduReader
 * Creates a json file (in the execution folder, name: filetestoutput.json)
 * which contains the result of the test.
 * If no error was found adds the amount of events read into the info key
 * Otherwise the info key contains the error message.
 * <p>
 * usage:
 * java -cp fact-tools.jar fact.ZFitsTester pathToFz1 pathToFz2
 */
public class ZFitsTester {
    private static class FileInfo {
        public String filename;
        public String status;
        public String info;

        public FileInfo(String filename, String status, String info) {
            this.filename = filename;
            this.status = status;
            this.info = info;
        }

        public void setError(String errorMsg) {
            this.status = "ERROR";
            this.info = errorMsg;
        }
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static void main(String[] args) {

        List<FileInfo> fileInfos = new ArrayList<FileInfo>();

        // process each file
        for (String arg : args) {
            System.out.println("Process: '" + arg + "'");
            File currentTestFile = new File(arg);
            String filename = currentTestFile.getName();
            FileInfo info = new FileInfo(filename, "IN WORK", "");

            if (!currentTestFile.exists()) {
                info.setError("File doesn't exists");
                fileInfos.add(info);
                continue;
            }
            SourceURL url = null;
            try {
                url = new SourceURL(currentTestFile.toURI().toURL());
            } catch (MalformedURLException e) {
                info.setError("Malformed URL Exception");
                fileInfos.add(info);
                continue;
            }
            FITSStream stream = new FITSStream(url);
            int eventCount = 0;
            try {
                stream.init();
                Data item = stream.read();
                while (item != null) {
                    eventCount++;
                    item = stream.read();
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                errorMsg += ", STACK[";
                errorMsg += getStackTrace(e);
                errorMsg += "]STACK";
                info.setError(errorMsg);
                fileInfos.add(info);
                continue;
            }
            info.status = "PROCESSED";
            info.info = "eventCount= " + eventCount + "";
            fileInfos.add(info);
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String output = gson.toJson(fileInfos);


        try {
            Files.write(Paths.get("filetestoutput.json"), output.getBytes());
        } catch (IOException e) {
            System.out.println("Error writing file");
            e.printStackTrace();
        }
    }
}
