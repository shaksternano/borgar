package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaderFactory;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FFmpegAudioReader extends FFmpegMediaReader<AudioFrame> {

    public FFmpegAudioReader(File input, String format) throws IOException {
        super(input, format);
    }

    public FFmpegAudioReader(InputStream input, String format) throws IOException {
        super(input, format);
    }

    @Nullable
    @Override
    protected Frame grabFrame() throws IOException {
        return grabber.grabSamples();
    }

    @Nullable
    @Override
    protected AudioFrame getNextFrame() throws IOException {
        var frame = grabFrame();
        if (frame == null) {
            return null;
        } else {
            return new AudioFrame(frame, frameDuration(), frame.timestamp);
        }
    }

    public enum Factory implements MediaReaderFactory<AudioFrame> {

        INSTANCE;

        @Override
        public MediaReader<AudioFrame> createReader(File media, String format) throws IOException {
            return new FFmpegAudioReader(media, format);
        }

        @Override
        public MediaReader<AudioFrame> createReader(InputStream media, String format) throws IOException {
            return new FFmpegAudioReader(media, format);
        }
    }
}
