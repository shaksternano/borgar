package io.github.shaksternano.mediamanipulator.media.graphics;

import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.media.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;

public class ShapeFilesGenerator {

    private static final Logger LOGGER = MiscUtil.createLogger("Shape Files Generator");

    private static final String SHAPE_FILES_DIRECTORY = "src/main/resources/" + FileUtil.getResourcePathInRootPackage("shape");

    public static void main(String[] args) {
        generateShapeFileFromImage("image/containerimage/thinking_bubble_edge_trimmed.png", new Coordinate(10, 120), new Coordinate(30, 90), new Coordinate(70, 40));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateShapeFileFromImage(String imageResourcePath, Coordinate... fillStartPositions) {
        LOGGER.info("Starting!");
        long startTime = System.currentTimeMillis();

        File directory = new File(SHAPE_FILES_DIRECTORY);
        directory.mkdirs();
        if (directory.isDirectory()) {
            try (InputStream inputStream = FileUtil.getResourceInRootPackage(imageResourcePath)) {
                BufferedImage image = ImageIO.read(inputStream);
                for (Coordinate fillStartPosition : fillStartPositions) {
                    image = ImageUtil.floodFill(image, fillStartPosition.x(), fillStartPosition.y(), Color.WHITE);
                }

                Shape shape = ImageUtil.getArea(image);
                image.flush();
                Shape serializableShape = createSerializableShape(shape);

                String shapeFileName = FileUtil.changeExtension(imageResourcePath, "javaobject");
                File shapeFile = new File(directory, shapeFileName);

                try (ObjectOutput output = new ObjectOutputStream(new FileOutputStream(shapeFile))) {
                    output.writeObject(serializableShape);

                    long totalTime = System.currentTimeMillis() - startTime;
                    LOGGER.info("Created shape file \"" + shapeFile + "\" in " + totalTime + " ms!");
                } catch (IOException e) {
                    LOGGER.error("Error while writing shape file under \"" + shapeFile + "\"!", e);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load image under \"" + imageResourcePath + "\"!", e);
            }
        } else if (directory.isFile()) {
            LOGGER.error("Failed to create shape files! The directory path \"" + directory + "\" already exists as a file!");
        } else {
            LOGGER.error("Failed to create shape files! Could not create parent directory \"" + directory + "\"!");
        }
    }

    private static Shape createSerializableShape(Shape shape) {
        return AffineTransform.getTranslateInstance(0, 0).createTransformedShape(shape);
    }

    private record Coordinate(int x, int y) {
    }
}
