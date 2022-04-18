package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.DelayedImage;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GifManipulator implements MediaManipulator {

    /**
     * 8MB
     */
    private static final long TARGET_FILE_SIZE = 8388608;

    @Override
    public File caption(File media, String caption) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.captionImage(image, caption, Fonts.getCaptionFont()), "captioned");
    }

    @Override
    public File stretch(File media, float widthMultiplier, float heightMultiplier) throws IOException {
        return applyToEachFrame(media, image -> ImageUtil.stretch(image, widthMultiplier, heightMultiplier), "stretched");
    }

    @Override
    public File makeGif(File media) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "gif"
        );
    }

    private static File applyToEachFrame(File media, Function<BufferedImage, BufferedImage> operation, String operationName) throws IOException {
        List<DelayedImage> frames = readGifFrames(media);
        frames = ImageUtil.removeFrames(frames, media.length(), TARGET_FILE_SIZE);

        frames.parallelStream().forEach(
                delayedImage -> {
                    BufferedImage uneditedImage = delayedImage.getImage();
                    BufferedImage image = operation.apply(uneditedImage);
                    delayedImage.setImage(image);
                    uneditedImage.flush();
                }
        );

        File imageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());
        writeFramesToGifFile(frames, imageFile);
        return imageFile;
    }

    private static List<DelayedImage> readGifFrames(File media) throws IOException {
        List<DelayedImage> frames = new ArrayList<>();
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(media));

        for (int i = 0; i < gif.getFrameCount(); i++) {
            BufferedImage frame = gif.getFrame(i).awt();
            int delay = (int) gif.getDelay(i).toMillis();
            frames.add(new DelayedImage(frame, delay));
        }

        return frames;
    }

    private static void writeFramesToGifFile(List<DelayedImage> frames, File outputFile){
        StreamingGifWriter writer = new StreamingGifWriter();
        try (StreamingGifWriter.GifStream gif = writer.prepareStream(outputFile, BufferedImage.TYPE_INT_ARGB)) {
            for (DelayedImage frame : frames) {
                gif.writeFrame(ImmutableImage.wrapAwt(frame.getImage()), Duration.ofMillis(frame.getDelay()), DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error writing GIF file", e);
        }
    }
}
