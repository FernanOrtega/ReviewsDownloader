import es.us.lsi.fogallego.reviewsdownloader.utils.UtilPhantom;

import java.io.IOException;

/**
 * TODO
 */
public class Test {

    protected static final int TIMEOUT = 30000;
    protected static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

    public static void main(String[] args) throws IOException {
        String html = UtilPhantom.getCompleteHtmlPage("http://www.pccomponentes.com/canon_eos_1200d___ef_s_18_55_is_ii.html");
        System.out.println();
    }
}
