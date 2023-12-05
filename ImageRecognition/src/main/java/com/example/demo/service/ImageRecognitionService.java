package com.example.demo.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.types.UInt8;

import com.example.*;
import com.example.demo.model.RecognitionData;


public class ImageRecognitionService {

    private static final String MONGO_COLLECTION_NAME = "recognition_data";

    private final MongoTemplate mongoTemplate;

    public ImageRecognitionService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public RecognitionData recognizeImage(MultipartFile file) throws IOException {
        // Load the TensorFlow library
        TensorFlow.loadLibrary("tensorflow_jni");

        // Load the pre-trained model
        try (Graph graph = new Graph()) {
            byte[] graphBytes = readAllBytesOrExit(Paths.get(MODEL_PATH));
            graph.importGraphDef(graphBytes);

            // Perform image recognition
            try (Session session = new Session(graph);
                 Tensor<UInt8> tensor = makeImageTensor(ImageIO.read((File) file))) {

                Tensor<?> result = session.runner()
                        .feed("input", tensor)
                        .fetch("output")
                        .run()
                        .get(0);

                // Decode the result
                String label = "example_label"; // Replace this with the label you get from your model
                float score = result.floatValue(); // Replace this with the score you get from your model

                return new RecognitionData(label, score);
            }
        }
    }

    public Tensor<UInt8> makeImageTensor(BufferedImage image) {
        // Convert BufferedImage to a tensor
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        return UInt8.tensorOfBuffer(image.getType(), pixels);
    }
    public void storeRecognitionData(RecognitionData recognitionData) {
        // Store data in MongoDB
        mongoTemplate.insert(recognitionData, MONGO_COLLECTION_NAME);
    }
 
}