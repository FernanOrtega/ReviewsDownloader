package es.us.lsi.fogallego.reviewsdownloader;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TripadvisorDownloader extends AbstractDownloader {
    private static final int OFFSET_LIMIT = 30;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {
        List<String[]> lstReviews = new ArrayList<String[]>();
        Set<String> setItemId = new HashSet<String>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);

            String urlPage = hubUrl;
            do {
                // Each page
                try {
                    Document doc = Jsoup.connect(urlPage).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    // Name, UrlReviews
                    Elements elements = doc.select("div[id^=hotel_].listing.easyClear.p13n_imperfect");
                    if (elements.size() == 0) {
                        break;
                    }
                    for (Element e : elements) {
                        String itemName = e.select("div.listing_title a").first().text();
                        String itemUrl = source.getSiteUrl() + e.select("div.listing_title a").first().attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, itemName, itemUrl));
                            setItemId.add(itemName);
                        }
                    }
                    Element next = doc.select("div.unified.pagination a.next").first();
                    if (next != null) {
                        urlPage = source.getSiteUrl() + next.attr("href");
                    } else {
                        break;
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

            } while (lstReviews.size() < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    private List<String[]> downloadItemReviews(Source source, String itemName, String itemUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
        Document docReviews = Jsoup.connect(itemUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
//        String html = UtilPhantom.getCompleteHtmlPage(itemUrl);
//        Document docReviews = Jsoup.connect(reviewsUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
//        Document docReviews = Jsoup.parse(html);
        String category = "hotels";
        Elements elements = docReviews.select("div#REVIEWS div[id^=review_] div.innerBubble");
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[8];
            detail[0] = itemUrl;
            detail[1] = itemName;
            detail[2] = category;
            detail[3] = itemUrl;
            detail[4] = e.select("div.entry").text();
            detail[5] = e.select("div.rating.reviewItemInline img").attr("alt").replace(" de 5 estrellas", "");
            detail[6] = "";
            detail[7] = "";

            lstReviews.add(detail);
        }

        return lstReviews;
    }
}
