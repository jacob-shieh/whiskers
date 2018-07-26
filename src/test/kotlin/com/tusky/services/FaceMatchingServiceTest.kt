package com.tusky.services

import org.junit.Test
import java.nio.file.Paths

class FaceMatchingServiceTest {

    @Test
    fun matchFace() {
        val sourceImagePath = Paths.get(ClassLoader.getSystemResource("pictures/source.jpg").toURI()).toString()
        val targetImagePath = Paths.get(ClassLoader.getSystemResource("pictures/target.jpg").toURI()).toString()
        FaceMatchingService().matchFace(sourceImagePath, targetImagePath)
    }
}