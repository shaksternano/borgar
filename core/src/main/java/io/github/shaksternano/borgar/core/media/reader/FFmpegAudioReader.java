package io.github.shaksternano.borgar.core.media.reader;

import io.github.shaksternano.borgar.core.collect.ClosableIterator;
import io.github.shaksternano.borgar.core.media.AudioFrame;
import io.github.shaksternano.borgar.core.media.MediaReaderFactory;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FFmpegAudioReader extends FFmpegMediaReader<AudioFrame> {

    public FFmpegAudioReader(File input, String format) throws IOException {
        super(input, format);
        fixAudioBitrate();
    }

    public FFmpegAudioReader(InputStream input, String format) throws IOException {
        super(input, format);
        fixAudioBitrate();
    }

    private void fixAudioBitrate() {
        if (audioChannels > 0 && audioBitrate == 0) {
            audioBitrate = 128000;
        }
    }

    @Override
    protected void setTimestamp(long timestamp, FFmpegFrameGrabber grabber) throws IOException {
        grabber.setAudioTimestamp(timestamp);
    }

    @Nullable
    @Override
    protected Frame grabFrame(FFmpegFrameGrabber grabber) throws IOException {
        return grabber.grabSamples();
    }

    @Override
    protected AudioFrame convertFrame(Frame frame) {
        return new AudioFrame(
            frame,
            frameDuration(),
            frame.timestamp
        );
    }

    @Override
    public MediaReader<AudioFrame> reversed() throws IOException {
        return new Reversed();
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

    private class Reversed extends BaseMediaReader<AudioFrame> {

        private final List<AudioFrame> reversedFrames;

        private Reversed() throws IOException {
            super(FFmpegAudioReader.this.format());
            frameRate = FFmpegAudioReader.this.frameRate;
            frameCount = FFmpegAudioReader.this.frameCount;
            duration = FFmpegAudioReader.this.duration;
            frameDuration = FFmpegAudioReader.this.frameDuration;
            audioChannels = FFmpegAudioReader.this.audioChannels;
            audioSampleRate = FFmpegAudioReader.this.audioSampleRate;
            audioBitrate = FFmpegAudioReader.this.audioBitrate;
            width = FFmpegAudioReader.this.width;
            height = FFmpegAudioReader.this.height;
            reversedFrames = reverseFrames();
        }

        private List<AudioFrame> reverseFrames() throws IOException {
            try (
                var iterator = FFmpegAudioReader.this.iterator();
                var reverseFilter = new FFmpegFrameFilter("areverse", FFmpegAudioReader.this.audioChannels())
            ) {
                reverseFilter.start();
                var sampleRate = -1;
                while (iterator.hasNext()) {
                    var frame = iterator.next();
                    reverseFilter.push(frame.getContent());
                    if (sampleRate < 0) {
                        sampleRate = frame.getContent().sampleRate;
                    }
                }
                List<AudioFrame> reversedFrames = new ArrayList<>(frameCount);
                Frame reversedFrame;
                while ((reversedFrame = reverseFilter.pullSamples()) != null) {
                    // The sample rate gets messed up by the filter, so we reset it.
                    reversedFrame.sampleRate = sampleRate;
                    reversedFrames.add(new AudioFrame(reversedFrame.clone(), frameDuration(), reversedFrame.timestamp));
                }
                return Collections.unmodifiableList(reversedFrames);
            }
        }

        @Override
        public AudioFrame readFrame(long timestamp) {
            return MediaUtil.frameAtTime(timestamp, reversedFrames, duration);
        }

        @Override
        public MediaReader<AudioFrame> reversed() {
            return FFmpegAudioReader.this;
        }

        @Override
        public void close() throws IOException {
            FFmpegAudioReader.this.close();
        }

        @Override
        public ClosableIterator<AudioFrame> iterator() {
            return ClosableIterator.wrap(reversedFrames.iterator());
        }
    }
}