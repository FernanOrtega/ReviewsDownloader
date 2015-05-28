package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.Pair;
import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class AmazonDownloader extends AbstractDownloader {

    private static final int OFFSET_LIMIT = 120;
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
                            lstReviews.addAll(downloadItemReviews(source, itemName, itemId, categorySource));
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

            lstPairItemInfo.add(new Pair<String, String>(itemId, itemName));
        }

        return lstPairItemInfo;
    }

    private List<String[]> downloadItemReviews(Source source, String itemName, String productId, CategorySource categorySource) throws IOException {

        System.out.println(productId + ": " + itemName);

        String itemUrl = "http://www.amazon.es/dp/" + productId;
        Document docItem = Jsoup.connect(itemUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        String itemCategory = docItem.select("div#wayfinding-breadcrumbs_feature_div").text();
        if (itemCategory.isEmpty()) {
            itemCategory = categorySource.getCategory();
        }
        String itemReviewUrl = source.getSiteUrl() + "/" + REVIEWS_URL + productId;

        List<String[]> lstReview = new ArrayList<String[]>();

        do {

            Document docReview = Jsoup.connect(itemReviewUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
            String html = docReview.outerHtml();
            Elements reviews = docReview.select("table#productReviews div[style^=margin-left]");
            for (Element e : reviews) {
                //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
                String[] detail = new String[9];
                detail[0] = UUID.randomUUID().toString();
                detail[1] = itemUrl;
                detail[2] = itemName;
                detail[3] = itemCategory;
                detail[4] = itemReviewUrl;
                detail[5] = e.select("div.reviewText").text();
                detail[6] = e.select("span.swSprite").text().split(" de un máximo de 5 estrellas")[0];
                detail[7] = "";
                detail[8] = "";
                lstReview.add(detail);

                UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                        detail[0], html);
            }

            Elements elemsNext = docReview.select("div.CMpaginate span.paging a");
            if (elemsNext.size() > 0) {
                Element elemNext = elemsNext.get(elemsNext.size() - 1);
                if (elemNext.toString().contains("Siguiente")) {
                    itemReviewUrl = elemNext.attr("href");
                } else {
                    itemReviewUrl = "";
                }
            } else {
                itemReviewUrl = "";
            }

        } while (!itemReviewUrl.isEmpty() && lstReview.size() < OFFSET_LIMIT);

        return lstReview;
    }


}
