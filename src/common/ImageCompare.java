package common;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class ImageCompare {

    public static boolean isImagesEquals(Image imageA, Image imageB) {
        if (imageA.getWidth() != imageB.getWidth() || imageA.getHeight() != imageB.getHeight())
            return false;

        PixelReader imageARead = imageA.getPixelReader();
        PixelReader imageBRead = imageB.getPixelReader();

        int width = (int)imageA.getWidth();
        int highest = (int)imageA.getHeight();

        for (int y = 0; y < highest; y++) {
            for (int x = 0; x < width; x++) {
                if (!imageARead.getColor(x, y).equals(imageBRead.getColor(x, y)))
                    return false;
            }
        }

        return true;

    }
}
