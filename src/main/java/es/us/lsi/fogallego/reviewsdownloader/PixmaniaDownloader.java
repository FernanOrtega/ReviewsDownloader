package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import es.us.lsi.fogallego.reviewsdownloader.utils.UtilPhantom;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class PixmaniaDownloader extends AbstractDownloader {

    private static final int OFFSET_LIMIT = 120;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {
        List<String[]> lstReviews = new ArrayList<String[]>();
        Set<String> setItemId = new HashSet<String>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);

            int productOffset = 0;
            String urlPage = hubUrl;
            do {
                // Each page
                try {
                    Document doc = Jsoup.connect(urlPage).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    Document doc2 = Jsoup.connect(urlPage).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    if (!doc.toString().equals(doc2.toString())) {
                        doc = Jsoup.connect(urlPage).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    }
                    // Name, UrlReviews
                    Elements elements = doc.select("p.productReviews a");
                    for (Element e : elements) {
                        String itemName = e.attr("title");
                        System.out.println(itemName);
                        String itemReviewsUrl = e.attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, categorySource, itemName, itemReviewsUrl));
                            setItemId.add(itemName);
                        }
                        productOffset++;
                    }
                    Element next = doc.select("ul.pagination a.active").get(0).parent().nextElementSibling();
                    if (next != null) {
                        urlPage = next.select("a[href]").attr("href");
                    } else {
                        break;
                    }
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 404) {
                        System.err.println("Pages finished!, exit loop.");
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Error while trying to retrieve results from Pixmania: " + e.getMessage());
                    break;
                }

            } while (lstReviews.size() < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    private List<String[]> downloadItemReviews(Source source, CategorySource categorySource, String itemName, String itemReviewsUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
        String html = UtilPhantom.getCompleteHtmlPage(itemReviewsUrl);
//        Document docReviews = Jsoup.connect(itemReviewsUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        Document docReviews = Jsoup.parse(html);
        String category = docReviews.select("div.breadcrumb").text().replace(itemName, "");
        int pipePosition = category.lastIndexOf("|");
        if (category.length() > pipePosition) {
            category = category.substring(0, pipePosition);
        }
        Elements reviews = docReviews.select("div.bv-content-core");
        for (Element review : reviews) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[9];
            detail[0] = UUID.randomUUID().toString();
            detail[1] = itemReviewsUrl;
            detail[2] = itemName;
            detail[3] = category;
            detail[4] = itemReviewsUrl;
            detail[5] = review.select("div.bv-content-summary-body-text").text();
            detail[6] = review.select("span.bv-content-rating meta[itemprop=ratingvalue]").attr("content");
            Elements divContentP = review.select("div.bv-content-product-questions > dl");
            String advantage = "";
            String disadvantage = "";
            switch (divContentP.size()) {
                case 2:
                    advantage = divContentP.get(0).text().replace("Ventaja ", "");
                    disadvantage = divContentP.get(1).text().replace("Inconveniente ", "");
                    break;
                case 1:
                    String aux = divContentP.get(0).text();
                    if (aux.contains("Ventaja")) {
                        advantage = aux.replace("Ventaja ", "");
                    } else if (aux.contains("Inconveniente")) {
                        disadvantage = aux.replace("Inconveniente ", "");
                    }
                    break;
            }
            detail[7] = advantage;
            detail[8] = disadvantage;

            lstReviews.add(detail);

            UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                    detail[0], docReviews.outerHtml());
        }

        return lstReviews;
    }
}
