package es.us.lsi.fogallego.reviewsdownloader;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CiaoDownloader extends AbstractDownloader {

    public static final int OFFSET_STEP = 15;
    public static final int OFFSET_LIMIT = 100;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {
       Map<String,String[]> mapReviews = new HashMap<String, String[]>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);
            mapReviews.putAll(extractFromCiao(hubUrl, categorySource.getCategory(), source.getFolderOut()));
        }
        return new ArrayList<String[]>(mapReviews.values());
    }

    private static Map<String,String[]> extractFromCiao(String hubUrl, String category, String folder) {
        Map<String,String[]> mapReviews = new HashMap<String, String[]>();
        // Blog, post link, fecha, titulo, texto, nº comentarios, etiquetas
        int offset = 0;
        do {
            try {
                String urlBase = hubUrl + "/Start/" + offset;
                Document doc = Jsoup.connect(urlBase).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                Elements elements = doc.select("a.review[href]");
                for (Element e : elements) {
                    String urlReview = e.attr("href");
                    if (validator.isValid(urlReview)) {
                        try {
                            if (!mapReviews.containsKey(urlReview)) {
                                String[] dataReview = getDetail(urlReview);
                                mapReviews.put(urlReview, dataReview);
                            }
                        } catch (Exception e1) {
                            System.err.println("Error trying to retrieve info from Review: "+urlReview+" -> "+e1.getMessage());
                        }
                    } else {
                        System.err.println(urlReview + " is not a valid link");
                    }
                }
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.err.println("Pages finished!, exit loop.");
                    break;
                }
            }catch (IOException e) {
                System.err.println("Error while trying to retrieve results from ciao: "+e.getMessage());

                System.exit(0);
            }
            offset += OFFSET_STEP;
        } while (offset < OFFSET_LIMIT);

        return mapReviews;
    }

    private static String[] getDetail(String urlReview) throws IOException {

        //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
        String[] detail = new String[8];

        Document doc = Jsoup.connect(urlReview).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        detail[0] = doc.select("a.prodOverview").attr("href");
        detail[1] = doc.select("a.prodOverview span").text();
        detail[2] = doc.select("#Node_BreadCrumb").text().split(" > "+detail[1])[0];
        detail[3] = urlReview;
        detail[4] = doc.select("#reviewText").text();
        detail[5] = doc.select("p.CWFontCSubText span[property]").first().attr("content");
        detail[6] = doc.select("p.pros").text().replaceAll("Ventajas: ","");
        detail[7] = doc.select("p.cons").text().replaceAll("Desventajas: ", "");

        return detail;
    }
}
