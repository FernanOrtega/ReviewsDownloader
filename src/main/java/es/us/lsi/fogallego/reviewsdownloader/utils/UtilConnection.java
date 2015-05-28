package es.us.lsi.fogallego.reviewsdownloader.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class UtilConnection {

    protected static final int TIMEOUT = 30000;
    protected static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

    public static Document getDocumentWithJsoup(String url) {

        Document doc = null;

        while (doc == null) {
            try {
                doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                Thread.sleep(2000);
            } catch (IOException e) {
                System.err.println("Error connecting to URL, trying again -> " + e.getMessage());
            } catch (InterruptedException e) {
                System.err.println("Error sleeping thread...");
            }
        }

        return doc;
    }

}
