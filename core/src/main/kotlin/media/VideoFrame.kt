package com.shakster.borgar.core.media

import org.bytedeco.javacv.Frame
import java.awt.image.BufferedImage
import kotlin.time.Duration

data class VideoFrame<T>(
    val content: T,
    val duration: Duration,
    val timestamp: Duration,
)

typealias ImageFrame = VideoFrame<BufferedImage>
typealias AudioFrame = VideoFrame<Frame>
