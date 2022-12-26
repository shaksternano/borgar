package io.github.shaksternano.mediamanipulator.io;

import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class FFmpegAudioReader extends FFmpegMediaReader<Frame> {

    public FFmpegAudioReader(File input) throws IOException {
        super(input);
    }

    @Nullable
    @Override
    protected Frame getNextFrame() throws IOException {
        return grabber.grabSamples();
    }
}
