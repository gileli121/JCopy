package common;

public class CommonHelpers {
    public static boolean isHtmlEmpty(String html) {
        return (html == null || getTextFromHtml(html).replace("\n","").replace("\r","").isEmpty());
    }

    public static String getTextFromHtml(String html) {
        return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
    }

    public static String removeNewLines(String str) {
        return str.replace("\n","").replace("\r","");
    }

}
