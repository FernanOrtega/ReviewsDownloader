package es.us.lsi.fogallego.reviewsdownloader;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import es.us.lsi.fogallego.reviewsdownloader.utils.UtilsJavaRefl;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class DownloaderExecutor {

    public static void main(String[] args) throws YamlException, FileNotFoundException {
        if (args.length >= 1) {
            for (String file : args) {
                download(file);
            }
        } else {
            System.out.println("Usage: [file_source_1] ...");
        }
    }

    private static void download(String file) throws YamlException, FileNotFoundException {
        YamlReader reader = new YamlReader(new FileReader(file));
        Source source = reader.read(Source.class);
        AbstractDownloader downloader = (AbstractDownloader) UtilsJavaRefl.createObject(source.getDownloaderClass());
        downloader.hook(source);
    }

}
