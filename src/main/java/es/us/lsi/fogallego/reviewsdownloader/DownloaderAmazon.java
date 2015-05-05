package es.us.lsi.fogallego.reviewsdownloader;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DownloaderAmazon extends AbstractDownloader {

    private static final UrlValidator validator = new UrlValidator();
    private static final int TIMEOUT = 30000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
    private static final int OFFSET_LIMIT = 30;
    public static final String REVIEWS_URL = "product-reviews/";

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {

        // TODO Some hub pages has different html structure

        List<String[]> lstReviews = new ArrayList<String[]>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);

            int productOffset = 0;
            String urlPage = hubUrl;
            do {
                // Each page
                try {
                    Document doc = Jsoup.connect(urlPage).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    Elements elements = doc.select("div#mainResults div[id^=result_].fstRowGrid");
                    if (elements.isEmpty()) {
                        break;
                    }
                    for (Element e : elements) {
                        String productId = e.attr("name");
                        lstReviews.addAll(downloadFromProductId(source.getSiteUrl() + "/" + REVIEWS_URL + productId));
                        productOffset++;
                    }

                    urlPage = source.getSiteUrl() + "/" + doc.select("a.pagnNextLink").attr("href");
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 404) {
                        System.err.println("Pages finished!, exit loop.");
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Error while trying to retrieve results from Amazon: " + e.getMessage());
                    break;
                }

            } while (productOffset < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    private List<String[]> downloadFromProductId(String productReviewUrl) throws IOException {

        List<String[]> lstString = new ArrayList<String[]>();

        Document doc = Jsoup.connect(productReviewUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        Elements reviews = doc.select("table#productReviews div[style=\"margin-left:0.5em;\"]");
        for (Element e : reviews) {
            String[] detail = new String[8];
            // TODO
            lstString.add(detail);
        }


        return lstString;
    }


}
