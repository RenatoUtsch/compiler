package org.parallelme.samples;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.parallelme.userlibrary.function.*;
import org.parallelme.userlibrary.image.Image;
import org.parallelme.userlibrary.image.RGB;

/**
 * @author Wilson de Carvalho.
 */
public class BitmapLoaderTest {
    public Bitmap load(Bitmap bitmap) {
        BitmapImage image = new BitmapImage(bitmap);
        // to Yxy
        image.par().foreach(new Foreach<Pixel>() {
            @Override
            public void function(Pixel pixel) {
				pixel.red /= 255;
				pixel.green /= 255;
				pixel.blue /= 255;
                float foo_r, foo_g, foo_b;
                foo_r = foo_g = foo_b = 0.0f;
                foo_r += 0.5141364f * pixel.red;
                foo_r += 0.3238786f * pixel.green;
                foo_r += 0.16036376f * pixel.blue;
                foo_g += 0.265068f * pixel.red;
                foo_g += 0.67023428f * pixel.green;
                foo_g += 0.06409157f * pixel.blue;
                foo_b += 0.0241188f * pixel.red;
                foo_b += 0.1228178f * pixel.green;
                foo_b += 0.84442666f * pixel.blue;
                float w = foo_r + foo_g + foo_b;
                if (w > 0.0f) {
                    pixel.red = foo_g;
                    pixel.green = foo_r / w;
                    pixel.blue = foo_g / w;
                } else {
                    pixel.red = pixel.green = pixel.blue = 0.0f;
                }
            }
        });
        // to RGB
        image.par().foreach(new Foreach<Pixel>() {
            @Override
            public void function(Pixel pixel) {
                float xVal, zVal;
                float yVal = pixel.red;       // Y

                if (yVal > 0.0f && pixel.green > 0.0f && pixel.blue > 0.0f) {
                    xVal = pixel.green * yVal / pixel.blue;
                    zVal = xVal / pixel.green - xVal - yVal;
                } else {
                    xVal = zVal = 0.0f;
                }
                pixel.red = pixel.green = pixel.blue = 0.0f;
                pixel.red += 2.5651f * xVal;
                pixel.red += -1.1665f * yVal;
                pixel.red += -0.3986f * zVal;
                pixel.green += -1.0217f * xVal;
                pixel.green += 1.9777f * yVal;
                pixel.green += 0.0439f * zVal;
                pixel.blue += 0.0753f * xVal;
                pixel.blue += -0.2543f * yVal;
                pixel.blue += 1.1892f * zVal;
				pixel.red *= 255;
				pixel.green *= 255;
				pixel.blue *= 255;
            }
        });
        image.toBitmap(bitmap);

        return bitmap;
    }
}
