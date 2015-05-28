package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class FilmaffinityDownloader extends AbstractDownloader {
    private static final int OFFSET_LIMIT = 240;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {
        List<String[]> lstReviews = new ArrayList<String[]>();
        Set<String> setItemId = new HashSet<String>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);

            // Each page
            try {
                Document doc = Jsoup.connect(hubUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                // Name, UrlReviews
                Elements elements = doc.select("div.wrapper-movie");
                if (elements.size() == 0) {
                    break;
                }
                for (Element e : elements) {
                    String itemName = e.select("h3 a").first().text();
                    System.out.println(itemName);
                    String itemUrl = source.getSiteUrl() + e.select("h3 a").first().attr("href");
                    if (!setItemId.contains(itemName)) {
                        lstReviews.addAll(downloadItemReviews(source, categorySource, itemName, itemUrl));
                        setItemId.add(itemName);
                    }
                }
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.err.println("Pages finished!, exit loop.");
                    break;
                }
            } catch (IOException e) {
                System.err.println("Error while trying to retrieve results from Hotelescom: " + e.getMessage());
                break;
            }

        }

        return lstReviews;
    }

    private List<String[]> downloadItemReviews(Source source, CategorySource categorySource, String itemName, String itemUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
        Document docItem = Jsoup.connect(itemUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        String reviewsUrl = source.getSiteUrl() + docItem.select("div.margin-ntabs ul.ntabs li:eq(1) > a").attr("href");
        Document docReviews = Jsoup.connect(reviewsUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        String category = "films";
        Elements elements = docReviews.select("div[data-review-id].rw-item");
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[9];
            detail[0] = UUID.randomUUID().toString();
            detail[1] = itemUrl;
            detail[2] = itemName;
            detail[3] = category;
            detail[4] = reviewsUrl;
            detail[5] = e.select("div.review-text1").text();
            String textAssesment = e.select("div.user-reviews-movie-rating").text();
            if (textAssesment != null && !textAssesment.isEmpty()) {
                double assesment = Double.valueOf(textAssesment) / 2;
                detail[6] = String.valueOf(assesment);
            } else {
                detail[6] = "";
            }
            detail[7] = "";
            detail[8] = "";

            lstReviews.add(detail);

            UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                    detail[0], docReviews.outerHtml());
        }

        return lstReviews;
    }
}
