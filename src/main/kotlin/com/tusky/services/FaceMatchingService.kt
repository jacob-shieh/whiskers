package com.tusky.services

import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.util.IOUtils
import com.amazonaws.services.rekognition.model.CompareFacesRequest

class FaceMatchingService {

    companion object {
        const val similarityThreshold = 70f
    }

    private var rekognitionClient: AmazonRekognition

    init {
        val credentials: AWSCredentials
        try {
            credentials = ProfileCredentialsProvider().credentials
        } catch (e: Exception) {
            throw AmazonClientException("Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/Users/userid/.aws/credentials), and is in valid format.", e)
        }

        val endpoint = EndpointConfiguration("endpoint", "us-west-2")

        rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
//                .withEndpointConfiguration(endpoint)
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .build()
    }

    private fun loadImage(sourceImage: String): ByteBuffer? {
        var imageBytes: ByteBuffer? = null
        try {
            FileInputStream(File(sourceImage))
                    .use { inputStream -> imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream)) }
        } catch (e: Exception) {
            println("Failed to load source image $sourceImage")
            throw Exception("failed to load image $sourceImage")
        }
        return imageBytes
    }

    fun matchFace(sourceImage: String, targetImage: String) {
        val sourceImageBytes = loadImage(sourceImage)
        val targetImageBytes = loadImage(targetImage)

        val source = Image().withBytes(sourceImageBytes)
        val target = Image().withBytes(targetImageBytes)

        val request = CompareFacesRequest()
                .withSourceImage(source)
                .withTargetImage(target)
                .withSimilarityThreshold(similarityThreshold)

        // Call operation
        val compareFacesResult = rekognitionClient.compareFaces(request)

        // Display results
        val faceDetails = compareFacesResult.getFaceMatches()
        for (match in faceDetails) {
            val face = match.getFace()
            val position = face.getBoundingBox()
            System.out.println("Face at " + position.getLeft().toString() +
                    " " + position.getTop() +
                    " matches with " + face.getConfidence().toString() +
                    "% confidence.")
        }
        val uncompared = compareFacesResult.getUnmatchedFaces()

        println("There were " + uncompared.size +
                " that did not match")
        System.out.println("Source image rotation: " + compareFacesResult.getSourceImageOrientationCorrection())
        System.out.println("target image rotation: " + compareFacesResult.getTargetImageOrientationCorrection())
    }
}