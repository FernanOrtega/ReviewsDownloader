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

public class CasadellibroDownloader extends AbstractDownloader {

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
                    Elements elements = doc.select("div.mod-list-item div.txt");
                    if (elements.size() == 0) {
                        break;
                    }
                    for (Element e : elements) {
                        Element titleAndName = e.select("a[id^=resultListadoCategoriaLibros]").first();
                        String itemName = titleAndName.text();
                        System.out.println(itemName);
                        String itemUrl = source.getSiteUrl() + titleAndName.attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, itemName, itemUrl));
                            setItemId.add(itemName);
                        }
                    }
                    Element next = doc.select("a.gonext").first();
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
                    System.err.println("Error while trying to retrieve results from Amazon: " + e.getMessage());
                    break;
                }

            } while (lstReviews.size() < OFFSET_LIMIT);

        }

        return lstReviews;
    }

    private List<String[]> downloadItemReviews(Source source, String itemName, String itemUrl) throws IOException {

        List<String[]> lstReviews = new ArrayList<String[]>();
//        String reviewsUrl = docItem.select("div.list-opinion a.more").attr("href");
        String reviewsUrl = itemUrl.replace(source.getSiteUrl(), source.getSiteUrl() + "/opiniones-libro");
        System.out.println(reviewsUrl);
        String html = UtilPhantom.getCompleteHtmlPage(reviewsUrl);
//        Document docReviews = Jsoup.connect(reviewsUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
        Document docReviews = Jsoup.parse(html);
        Elements elemsCategory = docReviews.select("div#breadcrumbs ul.bread li");
        elemsCategory.remove(0);
        elemsCategory.remove(elemsCategory.size() - 1);

        String category = "";
        for (Element elemCategory : elemsCategory) {
            category += elemCategory.text() + " > ";
        }
        category = category.substring(0, category.lastIndexOf(" > "));
        Elements elements = docReviews.select("div.list-opinion div.list-publications-item-content-actions");
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[8];
            detail[0] = itemUrl;
            detail[1] = itemName;
            detail[2] = category;
            detail[3] = itemUrl;
            detail[4] = e.select("p.smaller").text();
            detail[5] = "";
            detail[6] = "";
            detail[7] = "";

            lstReviews.add(detail);
        }

        return lstReviews;
    }
}
