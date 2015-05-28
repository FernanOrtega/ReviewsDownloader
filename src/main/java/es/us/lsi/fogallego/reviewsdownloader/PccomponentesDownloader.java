package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import es.us.lsi.fogallego.reviewsdownloader.utils.UtilPhantom;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class PccomponentesDownloader extends AbstractDownloader {

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
                    Elements elements = doc.select("div#productos li[itemscope]");
                    if (elements.size() == 0) {
                        break;
                    }
                    for (Element e : elements) {
                        String itemName = e.select("span.nombre").text();
                        System.out.println(itemName);
                        String itemUrl = e.select("span.nombre a[href]").attr("href");
                        if (!setItemId.contains(itemName)) {
                            lstReviews.addAll(downloadItemReviews(source, categorySource, itemName, itemUrl));
                            setItemId.add(itemName);
                        }
                    }
                    Element nextLink = doc.select("div.pagination li").last();
                    if (nextLink != null && !nextLink.attr("class").equals("active") && nextLink.text().contains("Siguiente")) {
                        urlPage = nextLink.select("a[href]").attr("href");
                    } else {
                        break;
                    }
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 404) {
                        System.err.println("Pages finished!, exit loop.");
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Error while trying to retrieve results from Pccomponentes: " + e.getMessage());
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
        String category = document.select("div.hilo-navegacion").text().replace(itemName, "");
        Elements elements = document.select("div.caja-comentarios");
        System.out.println(itemUrl + " -> " + elements.size());
        for (Element e : elements) {
            //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
            String[] detail = new String[9];
            detail[0] = UUID.randomUUID().toString();
            detail[1] = itemUrl;
            detail[2] = itemName;
            detail[3] = category;
            detail[4] = itemUrl;
            Element wholeText = e.select("div.txt > div.caja").first();
            detail[5] = StringEscapeUtils.unescapeHtml4(wholeText.childNode(0).outerHtml().replace("\n ", ""));
            detail[6] = e.select("div.info-valoracion div.rank li.current-rating").text().replace("Currently ", "").replace("00/5 Stars.", "");
            detail[7] = StringEscapeUtils.unescapeHtml4(wholeText.childNode(4).outerHtml());
            detail[8] = StringEscapeUtils.unescapeHtml4(wholeText.childNode(8).outerHtml());

            lstReviews.add(detail);

            UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                    detail[0], html);
        }

        return lstReviews;
    }
}
