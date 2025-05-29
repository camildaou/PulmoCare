package com.pulmocare.util;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_imgproc.CLAHE;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

public class PreprocessingUtils {
    
    public static float[] prepareImage(MultipartFile file) throws Exception {
        // Save file temporarily
        File tempFile = File.createTempFile("xray_", ".png");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        try {
            // Read image using OpenCV
            Mat img = opencv_imgcodecs.imread(tempFile.getAbsolutePath());
            if (img.empty()) {
                throw new IllegalArgumentException("Could not read the image file");
            }

            // Resize to 224x224
            Mat resized = new Mat();
            opencv_imgproc.resize(img, resized, new Size(224, 224));

            // Convert to LAB
            Mat lab = new Mat();
            opencv_imgproc.cvtColor(resized, lab, opencv_imgproc.COLOR_BGR2Lab);

            // Split channels
            MatVector labChannels = new MatVector(3);
            opencv_core.split(lab, labChannels);

            // Apply CLAHE to L channel
            CLAHE clahe = opencv_imgproc.createCLAHE(2.0, new Size(8, 8));
            Mat l_eq = new Mat();
            clahe.apply(labChannels.get(0), l_eq);

            // Merge channels back
            MatVector channels = new MatVector(3);
            channels.put(0, l_eq);
            channels.put(1, labChannels.get(1));
            channels.put(2, labChannels.get(2));
            
            Mat labEq = new Mat();
            opencv_core.merge(channels, labEq);

            // Convert back to BGR
            Mat bgrEq = new Mat();
            opencv_imgproc.cvtColor(labEq, bgrEq, opencv_imgproc.COLOR_Lab2BGR);
            
            // Create a 1D float array (flatten NCHW format)
            float[] tensor = new float[3 * 224 * 224];
            UByteIndexer indexer = bgrEq.createIndexer();
            
            int idx = 0;
            for (int c = 0; c < 3; c++) {
                for (int y = 0; y < 224; y++) {
                    for (int x = 0; x < 224; x++) {
                        // Get pixel in RGB order (inverse of BGR)
                        tensor[idx++] = indexer.get(y, x, 2 - c) / 255.0f;
                    }
                }
            }
            
            // Debug
            System.out.println("Created 1D tensor with length: " + tensor.length);
            System.out.println("Tensor class: " + tensor.getClass().getName());
            
            // Release OpenCV resources
            img.release();
            resized.release();
            lab.release();
            labEq.release();
            bgrEq.release();
            for (int i = 0; i < labChannels.size(); i++) {
                labChannels.get(i).release();
            }
            for (int i = 0; i < channels.size(); i++) {
                channels.get(i).release();
            }

            // Delete temp file
            tempFile.delete();
            
            return tensor;
        } catch (Exception e) {
            // Clean up temp file in case of error
            tempFile.delete();
            throw e;
        }
    }
}