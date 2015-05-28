package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ZavviDownloader extends AbstractDownloader {

    private static final int OFFSET_LIMIT = 240;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {
        List<String[]> lstReviews = new ArrayList<String[]>();
        Set<String> setItemId = new HashSet<String>();
        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);

            String urlPage = hubUrl;
            int numPage = 1;
            do {
                // Each page
                try {
                    Document doc = Jsoup.connect(urlPage).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    // Name, UrlReviews
                    Elements elements = doc.select("div.productlist div.item");
                    if (elements.size() == 0) {
                        break;
                    }
                    for (Element e : elements) {
                        String itemName = e.select("p.product-name a").attr("title");
                        System.out.println(itemName);
                        String itemUrl = e.select("p.product-name a").attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, categorySource, itemName, itemUrl));
                            setItemId.add(itemName);
                        }
                    }
                    numPage++;
                    urlPage = hubUrl + "?pageNumber=" + numPage;
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

    private List<String[]> downloadItemReviews(Source source, CategorySource categorySource, String itemName, String itemUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
//        String html = UtilPhantom.getCompleteHtmlPage(itemUrl);
//        Document document = Jsoup.parse(html);
        Document document = Jsoup.connect(itemUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        String category = itemUrl.replace(source.getSiteUrl(), "");
        Elements elements = document.select("div.review-content div.review-block");
        System.out.println(itemUrl + " -> " + elements.size());
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[9];
            detail[0] = UUID.randomUUID().toString();
            detail[1] = itemUrl;
            detail[2] = itemName;
            detail[3] = category;
            detail[4] = itemUrl;
            detail[5] = e.select("p.review-description").text();
            detail[6] = e.select("div.rating-holder span.rating-stars").text();
            detail[7] = "";
            detail[8] = "";

            lstReviews.add(detail);

            UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                    detail[0], document.outerHtml());
        }

        return lstReviews;
    }
}
