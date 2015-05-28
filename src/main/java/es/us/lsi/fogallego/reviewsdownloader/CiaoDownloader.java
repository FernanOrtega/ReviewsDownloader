package es.us.lsi.fogallego.reviewsdownloader;

import es.us.lsi.fogallego.reviewsdownloader.utils.UtilConnection;
import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class CiaoDownloader extends AbstractDownloader {

    public static final int OFFSET_STEP = 15;
    public static final int OFFSET_LIMIT = 120;

    @Override
    protected List<String[]> extractFromSource(Source source, CategorySource categorySource) {

        List<String[]> lstReviews = new ArrayList<>();
        Set<String> setItem = new HashSet<>();

        for (String hubUrl : categorySource.getUrls()) {
            System.out.println("-- Extracting from: " + hubUrl);
            int numItems = 0;
            String urlBase = hubUrl;
            do {
                try {
                    Document doc = Jsoup.connect(urlBase).userAgent(USER_AGENT).timeout(TIMEOUT).get();
                    Elements elements = doc.select("div#leafCatContent td.prodInfo p.prodRating a.userReviews");
                    for (Element e : elements) {
                        String urlItem = e.attr("href");
                        if (validator.isValid(urlItem)) {
                            try {
                                if (!setItem.contains(urlItem)) {
                                    List<String[]> lstReviewsOfItem = downloadItemReviews(source, categorySource, urlItem);
                                    lstReviews.addAll(lstReviewsOfItem);
                                    setItem.add(urlItem);
                                    if (lstReviewsOfItem.size() > 0)
                                        numItems++;
                                }
                            } catch (Exception e1) {
                                System.err.println("Error trying to retrieve info from Item: " + urlItem + " -> " + e1.getMessage());
                            }
                        } else {
                            System.err.println(urlItem + " is not a valid link");
                        }
                    }

                    Elements elemPagination = doc.select("div#Pagination div.clearfix p a[href]");
                    Element nextElem = null;
                    switch (elemPagination.size()) {
                        case 1:
                            nextElem = elemPagination.first();
                            break;
                        case 2:
                            nextElem = elemPagination.get(1);
                    }

                    if (nextElem == null || !nextElem.text().equals("Página siguiente")) {
                        break;
                    } else {
                        urlBase = source.getSiteUrl() + nextElem.attr("href");
                    }
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 404) {
                        System.err.println("Pages finished!, exit loop.");
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Error while trying to retrieve results from ciao: " + e.getMessage());

                    System.exit(0);
                }

            } while (numItems < OFFSET_LIMIT);
        }
        return lstReviews;
    }

    private static List<String[]> downloadItemReviews(Source source, CategorySource categorySource, String urlItem) throws IOException {

        List<String[]> lstReviews = new ArrayList<>();

        Boolean endPages = false;
        int offset = 0;
        String urlPaginated = urlItem;
        do {

            Document doc = UtilConnection.getDocumentWithJsoup(urlPaginated);

//            String htmlItem = UtilPhantom.getCompleteHtmlPage(urlPaginated);
//            Document doc = Jsoup.parse(htmlItem);

            String name = doc.select("div.m-productInf span[itemProp=name]").text().replaceAll("Opiniones - ", "");
            System.out.println(name + " offset:" + offset);
            Elements elements = doc.select("div.pm-reviewsList div.m-reviewItem");

            if (elements.size() == 0 || doc.select("div.m-mainPagination").size() == 0) {
                endPages = true;
            }

            for (Element review : elements) {

                //"url_item", "name", "category", "url_review", "text", "assessment","positive_opinion", "negative_opinion"
                String urlReview = review.select("div.m-reem-content p.e-reem-seemore a[href]").attr("href");

                if (!urlReview.isEmpty()) {

                    Document docReview = UtilConnection.getDocumentWithJsoup(urlReview);
                    String html = docReview.outerHtml();

                    String[] detail = new String[9];

                    detail[0] = UUID.randomUUID().toString();
                    detail[1] = urlItem;
                    detail[2] = name;
                    detail[3] = docReview.select("#Node_BreadCrumb").text().split(" > " + detail[1])[0];
                    detail[4] = urlReview;
                    detail[5] = docReview.select("#reviewText").text();
                    detail[6] = docReview.select("p.CWFontCSubText span[property]").first().attr("content");
                    detail[7] = docReview.select("p.pros").text().replaceAll("Ventajas: ", "");
                    detail[8] = docReview.select("p.cons").text().replaceAll("Desventajas: ", "");

                    UtilFiles.saveHtmlFile(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/html",
                            detail[0], html);

                    lstReviews.add(detail);
                }
            }

            offset += OFFSET_STEP;
            urlPaginated = urlItem + "/Start/" + offset;

        } while (!endPages && offset < OFFSET_LIMIT);

        return lstReviews;
    }
}
