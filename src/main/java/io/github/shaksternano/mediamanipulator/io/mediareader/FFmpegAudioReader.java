package io.github.shaksternano.mediamanipulator.io.mediareader;

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
}
