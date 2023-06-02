package io.github.shaksternano.borgar.image.imagemedia;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.borgar.image.util.AwtFrame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

class AnimatedImageTest {

    private final BufferedImage image1 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage image2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage image3 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage image4 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage image5 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    @Test
    void toNormalisedImages() {
        AnimatedImage animatedImage1 = new AnimatedImage(ImmutableList.of(
                new AwtFrame(image1, 20),
                new AwtFrame(image2, 20),
                new AwtFrame(image3, 20),
                new AwtFrame(image4, 20),
                new AwtFrame(image5, 20)
        ));

        AnimatedImage animatedImage2 = new AnimatedImage(ImmutableList.of(
                new AwtFrame(image1, 23),
                new AwtFrame(image2, 40),
                new AwtFrame(image3, 35),
                new AwtFrame(image4, 2),
                new AwtFrame(image5, 3)
        ));

        AnimatedImage animatedImage3 = new AnimatedImage(ImmutableList.of(
                new AwtFrame(image1, 1),
                new AwtFrame(image2, 1),
                new AwtFrame(image3, 1),
                new AwtFrame(image4, 1),
                new AwtFrame(image5, 1)
        ));

        Assertions.assertEquals(List.of(
                image1,
                image2,
                image3,
                image4,
                image5
        ), animatedImage1.toNormalisedImages());
        Assertions.assertEquals(List.of(
                image1,
                image2,
                image2,
                image3,
                image4
        ), animatedImage2.toNormalisedImages());
        Assertions.assertEquals(List.of(image1), animatedImage3.toNormalisedImages());
    }
}
