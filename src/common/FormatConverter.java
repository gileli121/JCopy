package common;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.*;

public class FormatConverter {
    public static String rtfToHtml(String rtfText) throws IOException {
        Reader rtf = new StringReader(rtfText);
        JEditorPane p = new JEditorPane();
        p.setContentType("text/rtf");
        EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
        try {
            kitRtf.read(rtf, p.getDocument(), 0);
            kitRtf = null;
            EditorKit kitHtml = p.getEditorKitForContentType("text/html");
            Writer writer = new StringWriter();
            kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
            String html = writer.toString();
            //if (isEmpty(html)) return null;
            return html;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }




}
