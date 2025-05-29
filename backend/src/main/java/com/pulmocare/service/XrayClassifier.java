package com.pulmocare.service;

import ai.onnxruntime.*;
import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.util.Collections;

@Service
public class XrayClassifier {

    private final OrtEnvironment env;
    private final OrtSession session;

    public XrayClassifier() throws Exception {
        env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        session = env.createSession("src/main/resources/resnet50_clahe.onnx", opts);
    }

    public String classify(float[] inputTensorObject) throws Exception {
        long[] shape = {1, 3, 224, 224}; // Expected shape for ResNet50
        float[] inputTensorData;

        // Debug input object type
        System.out.println("Input class: " + (inputTensorObject == null ? "null" : inputTensorObject.getClass().getName()));

         if (inputTensorObject instanceof float[]) {
            inputTensorData = inputTensorObject;
        } else {
            throw new IllegalArgumentException("Expected input of type float[] or float[][], but got: " +
                    (inputTensorObject == null ? "null" : inputTensorObject.getClass().getName()));
        }

        // Check the final size
        int expectedLength = (int) (shape[0] * shape[1] * shape[2] * shape[3]);
        if (inputTensorData.length != expectedLength) {
            throw new IllegalArgumentException("Invalid input length: expected " + expectedLength +
                    ", but got " + inputTensorData.length);
        }

        // Run the ONNX model
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputTensorData), shape)) {
            OrtSession.Result result = session.run(Collections.singletonMap("input", tensor));
            float[] scores = flattenArray((float[][]) result.get(0).getValue());
            int predicted = argMax(scores);

            if(predicted == 0) {
                return "Normal";
            }else if(predicted == 1){
                return "Covid-19";
            }else if(predicted == 2){
                return "Bacterial Pneumonia";
            }else{
                return "Unknown";
            }
        }
    }

    private int argMax(float[] arr) {
        int maxIdx = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIdx]) maxIdx = i;
        }
        return maxIdx;
    }
        private float[] flattenArray(float[][] data2D) {
        // Calculate total length
        int totalLength = 0;
        for (float[] row : data2D) {
            totalLength += row.length;
        }
        
        // Create flattened array
        float[] flattened = new float[totalLength];
        int pos = 0;
        for (float[] row : data2D) {
            for (float val : row) {
                flattened[pos++] = val;
            }
        }
        
        System.out.println("Flattened 2D array to 1D with length: " + flattened.length);
        return flattened;
    }
}
