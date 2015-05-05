package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.Pair;
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

public class DownloaderAmazon extends AbstractDownloader {

    private static final int TIMEOUT = 30000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
    private static final int OFFSET_LIMIT = 30;
    public static final String REVIEWS_URL = "product-reviews/";
    public static final String ITEM_ID_SELECTOR_1 = "name";
    public static final String ITEM_NAME_SELECTOR_1 = "h3";
    public static final String ITEM_ID_SELECTOR_2 = "data-asin";
    public static final String ITEM_NAME_SELECTOR_2 = "a.s-access-detail-page h2";

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

                    List<Pair<String, String>> lstPairItemInfo = getListPairItemInfo(doc);
                    if (lstPairItemInfo.isEmpty()) {
                        break;
                    }
                    for (Pair<String, String> item : lstPairItemInfo) {
                        String itemId = item.getFirst();
                        String itemName = item.getSecond();
                        if (!setItemId.contains(itemId)) {
                            lstReviews.addAll(downloadItemReviews(source, itemName, itemId));
                            setItemId.add(itemId);
                        }
                        productOffset++;
                    }
                    String nextPage = doc.select("a.pagnNextLink").attr("href");
                    if (!nextPage.isEmpty()) {
                        urlPage = source.getSiteUrl() + "/" + nextPage;
                    }
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

    private List<Pair<String, String>> getListPairItemInfo(Document doc) {

        List<Pair<String, String>> lstPairItemInfo = new ArrayList<Pair<String, String>>();
        Elements elements = doc.select("div#mainResults div[id^=result_]");
        String itemIdSelector;
        String itemNameSelector;
        if (elements.isEmpty()) { // only oven
            elements = doc.select("div li[id^=result_]");
            itemIdSelector = ITEM_ID_SELECTOR_2;
            itemNameSelector = ITEM_NAME_SELECTOR_2; // "a.s-access-detail-page h2"
        } else {
            itemIdSelector = ITEM_ID_SELECTOR_1;
            itemNameSelector = ITEM_NAME_SELECTOR_1;// "h3.title"
        }

        for (Element e : elements) {
            String itemId = e.attr(itemIdSelector);
            String itemName = e.select(itemNameSelector).text();

            System.out.println(itemId + ": " + itemName);

            lstPairItemInfo.add(new Pair<String, String>(itemId, itemName));
        }

        return lstPairItemInfo;
    }

    private List<String[]> downloadItemReviews(Source source, String itemName, String productId) throws IOException {

        String itemUrl = "http://www.amazon.es/dp/" + productId;
        Document docItem = Jsoup.connect(itemUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        String itemCategory = docItem.select("div#wayfinding-breadcrumbs_feature_div").text();
        String itemReviewUrl = source.getSiteUrl() + "/" + REVIEWS_URL + productId;

        List<String[]> lstReview = new ArrayList<String[]>();

        Document docReview = Jsoup.connect(itemReviewUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        Elements reviews = docReview.select("table#productReviews div[style^=margin-left]");
        for (Element e : reviews) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[8];
            detail[0] = itemUrl;
            detail[1] = itemName;
            detail[2] = itemCategory;
            detail[3] = itemReviewUrl;
            detail[4] = e.select("div.reviewText").text();
            detail[5] = e.select("span.swSprite").text().split(" de un máximo de 5 estrellas")[0];
            detail[6] = "";
            detail[7] = "";
            lstReview.add(detail);
        }

        return lstReview;
    }


}
