package es.us.lsi.fogallego.reviewsdownloader.utils;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class UtilPhantom {

    private final static long GLOBAL_WAIT_TIME = 100000l;

    static {
        // Desactivamos los logger innecesarios
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger(PhantomJSDriverService.class.getName()).setLevel(Level.OFF);
    }

    public static String getCompleteHtmlPage(String url) {
        String html = null;
        DesiredCapabilities dCaps = new DesiredCapabilities();
        // No funciona un carajo esto. Sigue logueando cosas. Pero menos
        String[] phantomArgs = new String[]{"--webdriver-loglevel=ERROR"};
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        PhantomJSDriver driver = new PhantomJSDriver(dCaps);

        driver.manage().timeouts().implicitlyWait(GLOBAL_WAIT_TIME, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().pageLoadTimeout(GLOBAL_WAIT_TIME, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().setScriptTimeout(GLOBAL_WAIT_TIME, TimeUnit.MILLISECONDS);
        try {
            driver.get(url);
            html = driver.getPageSource();
        } finally {
            driver.close();
            driver.quit();
        }

        return html;
    }

}
