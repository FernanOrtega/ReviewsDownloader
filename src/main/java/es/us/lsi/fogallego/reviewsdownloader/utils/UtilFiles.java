package es.us.lsi.fogallego.reviewsdownloader.utils;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class UtilFiles {
    public static void saveToCSV(String fileLocation, String fileName, List<String[]> lstRows)
            throws IOException {
        File f = new File(fileLocation);
        if (!f.exists() && !f.mkdirs()) {
            return;
        }
        CSVWriter csvWriter = new CSVWriter(
                new FileWriterWithEncoding(fileLocation+"/"+fileName+".csv",
                        Charset.forName("UTF-8")), ';');
        csvWriter.writeAll(lstRows);
        csvWriter.flush();
        csvWriter.close();

    }
}
