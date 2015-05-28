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

public class ElcorteinglesDownloader extends AbstractDownloader {

    private static final int OFFSET_LIMIT = 240;

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
                        String itemUrl = titleAndName.attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, categorySource, itemName, itemUrl));
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
                    System.err.println("Error while trying to retrieve results from ElCorteIngles: " + e.getMessage());
                    break;
                }

            } while (lstReviews.size() < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    private List<String[]> downloadItemReviews(Source source, CategorySource categorySource, String itemName, String itemUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
        String html = UtilPhantom.getCompleteHtmlPage(itemUrl);
        Document document = Jsoup.parse(html);
        String category = document.select("div.BVRRSCategoryBreadcrumbNav").text().replace("P�gina principal de opiniones > ", "");


        do {

            Elements elements = document.select("div[id^=BVRRDisplayContentReviewID_]");

            System.out.println(itemName + " -> " + elements.size());

            for (Element e : elements) {
                //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
                String[] detail = new String[9];
                detail[0] = UUID.randomUUID().toString();
                detail[1] = itemUrl;
                detail[2] = itemName;
                detail[3] = category;
                detail[4] = itemUrl;
                detail[5] = e.select("div.BVRRReviewDisplayStyle5Text").text();
                detail[6] = e.select("div.BVRRRatingNormalImage img").attr("title").replace(" de 5", "");
                detail[7] = e.select("span.BVRRValue.BVRRReviewProTags") != null ? e.select("span.BVRRValue.BVRRReviewProTags").text() : "";
                detail[8] = e.select("span.BVRRValue.BVRRReviewConTags") != null ? e.select("span.BVRRValue.BVRRReviewConTags").text() : "";

                lstReviews.add(detail);

                UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                        detail[0], html);
            }

            Elements elemsNext = document.select("span.BVRRPageLink BVRRPageNumber a[title=siguiente]");

            if (elemsNext.size() == 0) {
                document = null;
            } else {
                itemUrl = elemsNext.attr("href");
                html = UtilPhantom.getCompleteHtmlPage(itemUrl);
                document = Jsoup.parse(html);
            }

        } while (document != null);

        return lstReviews;
    }
}
