package io.github.shaksternano.borgar.discord.util;

import org.bytedeco.javacv.Frame;

import java.util.Arrays;
import java.util.Objects;

public class JavaCVUtil {

    public static int hashFrame(Frame frame) {
        if (frame == null) {
            return 0;
        }
        return Objects.hash(
            frame.keyFrame,
            frame.pictType,
            frame.imageWidth,
            frame.imageHeight,
            frame.imageDepth,
            frame.imageChannels,
            frame.imageStride,
            Arrays.hashCode(frame.image),
            frame.sampleRate,
            frame.audioChannels,
            Arrays.hashCode(frame.samples),
            frame.data,
            frame.streamIndex,
            frame.type,
            frame.opaque,
            frame.timestamp
        );
    }

    public static boolean frameEquals(Frame frame1, Frame frame2) {
        if (Objects.equals(frame1, frame2)) {
            return true;
        }
        if (frame1 == null || frame2 == null) {
            return false;
        }
        return frame1.keyFrame == frame2.keyFrame
            && frame1.pictType == frame2.pictType
            && frame1.imageWidth == frame2.imageWidth
            && frame1.imageHeight == frame2.imageHeight
            && frame1.imageDepth == frame2.imageDepth
            && frame1.imageChannels == frame2.imageChannels
            && frame1.imageStride == frame2.imageStride
            && Arrays.equals(frame1.image, frame2.image)
            && frame1.sampleRate == frame2.sampleRate
            && frame1.audioChannels == frame2.audioChannels
            && Arrays.equals(frame1.samples, frame2.samples)
            && Objects.equals(frame1.data, frame2.data)
            && frame1.streamIndex == frame2.streamIndex
            && frame1.type == frame2.type
            && Objects.equals(frame1.opaque, frame2.opaque)
            && frame1.timestamp == frame2.timestamp;
    }
}
