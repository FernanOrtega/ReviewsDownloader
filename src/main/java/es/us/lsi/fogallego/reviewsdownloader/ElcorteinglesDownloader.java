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

public class ElcorteinglesDownloader extends AbstractDownloader {

    private static final int TIMEOUT = 30000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
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
                    Elements elements = doc.select("div.BVRRSProductsInfo div[id^=BVRRSExternalProductData]");
                    if (elements.size() == 0) {
                        break;
                    }
                    for (Element e : elements) {
                        Element titleAndName = e.select("div.BVRRSExternalSubjectTitle.BVRRSExternalProductTitle a").first();
                        String itemName = titleAndName.attr("title");
                        System.out.println(itemName);
                        String itemUrl = titleAndName.attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, itemName, itemUrl));
                            setItemId.add(itemName);
                        }
                    }
                    Element next = doc.select("span.BVRRPageLink.BVRRNextPage > a[title=siguiente]").first();
                    if (next != null) {
                        urlPage = next.attr("href");
                    } else {
                        break;
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

            } while (lstReviews.size() < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    private List<String[]> downloadItemReviews(Source source, String itemName, String itemUrl) {

        List<String[]> lstReviews = new ArrayList<String[]>();
        String html = UtilPhantom.getCompleteHtmlPage(itemUrl);
        Document document = Jsoup.parse(html);
        String category = document.select("div.BVRRSCategoryBreadcrumbNav").text().replace("Página principal de opiniones > ", "");
        Elements elements = document.select("div[id^=BVRRDisplayContentReviewID_]");
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[8];
            detail[0] = itemUrl;
            detail[1] = itemName;
            detail[2] = category;
            detail[3] = itemUrl;
            detail[4] = e.select("div.BVRRReviewDisplayStyle5Text").text();
            detail[5] = e.select("div.BVRRRatingNormalImage img").attr("title").replace(" de 5", "");
            detail[6] = e.select("span.BVRRValue.BVRRReviewProTags") != null ? e.select("span.BVRRValue.BVRRReviewProTags").text() : "";
            detail[7] = e.select("span.BVRRValue.BVRRReviewConTags") != null ? e.select("span.BVRRValue.BVRRReviewConTags").text() : "";

            lstReviews.add(detail);
        }

        return lstReviews;
    }
}
