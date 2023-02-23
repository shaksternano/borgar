package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FFmpegAudioReader extends FFmpegMediaReader<Frame> {

    public FFmpegAudioReader(File input) throws IOException {
        super(input);
    }

    public FFmpegAudioReader(InputStream input) throws IOException {
        super(input);
    }

    @Nullable
    @Override
    protected Frame grabFrame() throws IOException {
        return grabber.grabSamples();
    }

    @Nullable
    @Override
    public Frame getNextFrame() throws IOException {
        return grabber.grabSamples();
    }

    public enum Factory implements MediaReaderFactory<Frame> {

        INSTANCE;

        @Override
        public MediaReader<Frame> createReader(File media) throws IOException {
            return new FFmpegAudioReader(media);
        }

        @Override
        public MediaReader<Frame> createReader(InputStream media) throws IOException {
            return new FFmpegAudioReader(media);
        }
    }
}
