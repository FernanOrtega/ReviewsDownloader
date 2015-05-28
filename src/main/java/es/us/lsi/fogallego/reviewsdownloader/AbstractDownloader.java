package es.us.lsi.fogallego.reviewsdownloader;

import com.esotericsoftware.yamlbeans.YamlException;
import es.us.lsi.fogallego.reviewsdownloader.utils.UtilFiles;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDownloader {

    protected static final UrlValidator validator = new UrlValidator();
    protected static final int TIMEOUT = 30000;
    protected static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

    public void hook(Source source) throws FileNotFoundException, YamlException {
        for (CategorySource categorySource : source.getSources()) {
            System.out.println("-> Category: "+categorySource.getCategory());
            List<String[]> lstReviewsToCsv = new ArrayList<String[]>();
            String[] header = {"uuid", "url_item", "name", "category", "url_review", "text", "assessment",
                    "positive_opinion", "negative_opinion"};
            lstReviewsToCsv.add(header);
            lstReviewsToCsv.addAll(extractFromSource(source, categorySource));
            System.out.println("Num reviews: "+(lstReviewsToCsv.size() - 1));
            try {
                UtilFiles.saveToCSV(source.getFolderOut() + categorySource.getCategory() + "\\" + source.getSite() + "/datasets", String.valueOf(System.currentTimeMillis()), lstReviewsToCsv);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    protected abstract List<String[]> extractFromSource(Source source, CategorySource categorySource);

}
