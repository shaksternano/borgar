package io.github.shaksternano.mediamanipulator.util;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;

public class JavaCVUtil {

    public static FFmpegFrameRecorder createFFmpegRecorder(File file, String format, int imageWidth, int imageHeight, int audioChannels, double fps) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, imageWidth, imageHeight, audioChannels);
        recorder.setInterleaved(true);

        // decrease "startup" latency in FFMPEG (see:
        // https://trac.ffmpeg.org/wiki/StreamingGuide)
        recorder.setVideoOption("tune", "zerolatency");
        // tradeoff between quality and encode speed
        // possible values are ultrafast, superfast, veryfast, faster, fast,
        // medium, slow, slower, veryslow
        // ultrafast offers us the least amount of compression (lower encoder
        // CPU) at the cost of a larger stream size
        // at the other end, veryslow provides the best compression (high
        // encoder CPU) while lowering the stream size
        // (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("preset", "ultrafast");
        // Constant Rate Factor (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("crf", "28");
        recorder.setOption("hwaccel", "nvenc");
        // 2000 kb/s, reasonable "sane" area for 720
        recorder.setVideoBitrate(2000000);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat(format);
        // FPS (frames per second)
        recorder.setFrameRate(fps);
        // Key frame interval, in our case every 2 seconds -> 30 (fps) * 2 = 60
        // (gop length)
        recorder.setGopSize((int) (fps * 2));

        // We don't want variable bitrate audio
        recorder.setAudioOption("crf", "0");
        // Highest quality
        recorder.setAudioQuality(0);
        // 192 Kbps
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        return recorder;
    }
}
