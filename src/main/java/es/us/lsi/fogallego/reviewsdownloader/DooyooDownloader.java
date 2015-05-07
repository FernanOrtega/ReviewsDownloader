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

public class DooyooDownloader extends AbstractDownloader {

    private static final int OFFSET_LIMIT = 30;

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
                    // Name, UrlReviews
                    Elements elements = doc.select("table.ptProdList tr.product");
                    if (elements.size() == 0) {
                        break;
                    }
                    for (Element e : elements) {
                        String itemName = e.select("div[id^=label-] ").text();
                        System.out.println(itemName);
                        String itemReviewsUrl = source.getSiteUrl() + e.select("a[style]").attr("href");
                        if (!setItemId.contains(itemName)) {
                            List<String[]> lstReviewsAux;
                            String itemReviewsUrlAux = itemReviewsUrl;
                            int offset = 1;
                            do {
                                lstReviewsAux = downloadItemReviews(source, itemName, itemReviewsUrlAux);
                                lstReviews.addAll(lstReviewsAux);
                                if (itemReviewsUrlAux.contains("#rev")) {
                                    break;
                                }
                                itemReviewsUrlAux = itemReviewsUrl + offset + "/";
                                offset++;
                            } while (lstReviewsAux.size() > 0);
                            setItemId.add(itemName);
                        }
                        productOffset++;
                    }
                    urlPage = hubUrl + productOffset + "/";
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 404) {
                        System.err.println("Pages finished!, exit loop.");
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Error while trying to retrieve results from Dooyoo: " + e.getMessage());
                    break;
                }

            } while (lstReviews.size() < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    // We download only the first page of reviews
    private List<String[]> downloadItemReviews(Source source, String itemName, String itemReviewsUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
        Document docHubReviews = Jsoup.connect(itemReviewsUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        Elements reviewsUrl = docHubReviews.select("p.review.description a[href]");
        for (Element reviewUrl : reviewsUrl) {

            String absoluteReviewUrl = source.getSiteUrl() + reviewUrl.attr("href");
            Document docReview = Jsoup.connect(absoluteReviewUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[8];
            detail[0] = absoluteReviewUrl;
            detail[1] = itemName;
            detail[2] = docReview.select("div.breadCrumbs").text().split(itemName)[0];
            detail[3] = absoluteReviewUrl;
            detail[4] = docReview.select("div.description").text();
            detail[5] = docReview.select("div.rating span.value-title").attr("title");
            Elements divContentP = docReview.select("div#content > p");
            detail[6] = divContentP.get(0).text().replace("Ventajas: ", "");
            detail[7] = divContentP.get(1).text().replace("Desventajas: ", "");

            lstReviews.add(detail);
        }

        return lstReviews;
    }
}
