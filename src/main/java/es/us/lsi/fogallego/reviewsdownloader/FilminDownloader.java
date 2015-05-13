package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.UtilPhantom;
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

public class FilminDownloader extends AbstractDownloader {
    private static final int OFFSET_LIMIT = 30;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {
        List<String[]> lstReviews = new ArrayList<String[]>();
        Set<String> setItemId = new HashSet<String>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);

            // Each page
            try {
//                Document doc = Jsoup.connect(hubUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();

                String html = UtilPhantom.getCompleteHtmlPage(hubUrl);
                Document doc = Jsoup.parse(html);

                // Name, UrlReviews
                Elements elements = doc.select("ul#films_list li.film");
                if (elements.size() == 0) {
                    break;
                }
                for (Element e : elements) {
                    String itemName = e.select("div.film_info span.title").text();
                    System.out.println(itemName);
                    String itemUrl = source.getSiteUrl() + e.select("a").first().attr("href");
                    if (!setItemId.contains(itemName)) {
                        lstReviews.addAll(downloadItemReviews(source, itemName, itemUrl));
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

    private List<String[]> downloadItemReviews(Source source, String itemName, String itemUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
        Document docItem = Jsoup.connect(itemUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        String category = "films";
        Elements elements = docItem.select("ul.main_user_comments.films_comments > li");
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[8];
            detail[0] = itemUrl;
            detail[1] = itemName;
            detail[2] = category;
            detail[3] = itemUrl;
            detail[4] = e.select("span.text").text();
            String textAssesment = e.select("div.main_progress_bar.user_rank").text();
            if (textAssesment != null && !textAssesment.isEmpty()) {
                double assesment = Double.valueOf(textAssesment) / 2;
                detail[5] = String.valueOf(assesment);
            } else {
                detail[5] = "";
            }
            detail[6] = "";
            detail[7] = "";

            lstReviews.add(detail);
        }

        return lstReviews;
    }
}
