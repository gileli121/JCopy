package classes;

import common.CommonHelpers;
import javafx.scene.image.Image;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.StrongTextEncryptor;

public class ClipboardDataFormats {

    private boolean isEncrypted = false;
    private String plainText = null;
    private String html = null;
    private String rtf = null;
    private String url = null;
    private Image image = null;

    public ClipboardDataFormats() {}

    public ClipboardDataFormats(ClipboardDataFormats copy) {
        this.plainText = copy.plainText;
        this.html = copy.html;
        this.rtf = copy.rtf;
        this.url = copy.url;
        this.image = copy.image;
        this.isEncrypted = copy.isEncrypted;
    }

    public ClipboardDataFormats(String plainText) {
        this(plainText,null,null,null,null);
    }


    public ClipboardDataFormats(String plainText, String html, String rtf, String url, Image image) {
        this(false,plainText,html,rtf,url,image);
    }

    public ClipboardDataFormats(boolean isEncrypted,String plainText, String html, String rtf, String url, Image image) {
        this.isEncrypted = isEncrypted;
        this.plainText = plainText;
        this.html = html;
        this.rtf = rtf;
        this.url = url;
        this.image = image;
    }


    public void setEncryption(String password) {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(password);
        if (plainText != null) plainText = textEncryptor.encrypt(plainText);
        if (html != null) html = textEncryptor.encrypt(html);
        if (rtf != null) rtf = textEncryptor.encrypt(rtf);
        if (url != null) url = textEncryptor.encrypt(url);
        isEncrypted = true;
    }

    public boolean removeEncryption(String password) {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(password);

        try {
            if (plainText != null) plainText = textEncryptor.decrypt(plainText);
            if (html != null) html = textEncryptor.decrypt(html);
            if (rtf != null) rtf = textEncryptor.decrypt(rtf);
            if (url != null) url = textEncryptor.decrypt(url);
            isEncrypted = false;
            return true;
        } catch (EncryptionOperationNotPossibleException e) {
            return false;
        }
    }

    public ClipboardDataFormats getDecryptedObject(String password) {
        ClipboardDataFormats decryptedDataFormats = new ClipboardDataFormats(this);
        decryptedDataFormats.removeEncryption(password);
        return decryptedDataFormats;
    }

    public boolean equals(ClipboardDataFormats clipboardDataFormats) {

        if (clipboardDataFormats == null) return false;

        if (plainText != null || clipboardDataFormats.plainText != null) {
            if (plainText == null || clipboardDataFormats.plainText == null) return false;
            if (!plainText.equals(clipboardDataFormats.plainText)) return false;
        }

        if (html != null || clipboardDataFormats.html != null) {
            if (html == null || clipboardDataFormats.html == null) return false;
            if (!html.equals(clipboardDataFormats.html)) return false;
        }


        if (rtf != null || clipboardDataFormats.rtf != null) {
            if (rtf == null || clipboardDataFormats.rtf == null) return false;
            if (!rtf.equals(clipboardDataFormats.rtf)) return false;
        }

//        if (image != null || clipboardDataFormats.image != null) {
//            if (image == null || clipboardDataFormats.image == null) return false;
//            if (!ImageCompare.isImagesEquals(image,clipboardDataFormats.image)) return false;
//        }

        return true;
    }

    public boolean contains(String text) {

        if (image != null) return false;
        text = text.toLowerCase();

        if (plainText != null) {
            return plainText.toLowerCase().contains(text);
        }
        else if (url != null) {
            return url.toLowerCase().contains(text);
        }
        else if (html != null && html.toLowerCase().contains(text))
            return true;
        else
            return rtf != null && rtf.toLowerCase().contains(text);

    }



    public String getPlainText() {
        return plainText;
    }
    public String getHtml() {
        return html;
    }
    public String getRtf() { return rtf; }
    public String getUrl() {
        return url;
    }
    public Image getImage() {
        return image;
    }
    public boolean getIsEncrypted() {return isEncrypted;}


    public void setPlainText(String plainText) {
        if (plainText != null && plainText.isEmpty()) plainText = null;
        this.plainText = plainText;
    }
    public void setHtml(String html) {
        if (html != null && (html.isEmpty() || CommonHelpers.isHtmlEmpty(html))) html = null;
        this.html = html;
    }
    public void setRtf(String rtf) {
        if (rtf != null && rtf.isEmpty()) rtf = null;
        this.rtf = rtf;
    }
    public void setUrl(String url) {
        if (url != null && url.isEmpty()) url = null;
        this.url = url;
    }
    public void setImage(Image image) {
        this.image = image;
    }





}