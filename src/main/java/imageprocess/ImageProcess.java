package imageprocess;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ImageProcess {
    File referenceImage = new File("src/main/java/assets/image/reference.jpg");
    HashMap<String, BufferedImage> imageMap = new HashMap<>();
    ConcurrentHashMap<String, Color> mappedDataset =  new ConcurrentHashMap<>();
    File dataset = new File("src/main/java/assets/image/dataset");
    int blockSIze = 30; // Tamanho do bloco em Pixels

    public Color calcAverageColor(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        long sumR = 0, sumG = 0, sumB = 0;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                var colorPixel = image.getRGB(x, y);
                sumR += (colorPixel >> 16) & 0xFF;
                sumG += (colorPixel >> 8) & 0xFF;
                sumB += colorPixel & 0xFF;
            }
        }

        int totalPixels = width * height;
        int averageR = (int) (sumR / totalPixels);
        int averageG = (int) (sumG / totalPixels);
        int averageB = (int) (sumB / totalPixels);

        return new Color(averageR, averageG, averageB);
    }

    public void addImageOnImage(int xPosition, int yPosition, BufferedImage reference, BufferedImage image) throws IOException {
        BufferedImage finalImage = new BufferedImage(reference.getWidth(), reference.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = finalImage.createGraphics();
        g.drawImage(reference, 0, 0, null);
        g.dispose();

        int height = blockSIze, width = blockSIze;

        var graphicG = reference.createGraphics();

        var resizedImage = resizeImage(image, width, height);

        graphicG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphicG.drawImage(resizedImage, xPosition, yPosition, width, height, null);
        graphicG.dispose();

        var resultImage = new File("src/main/java/assets/image/result", "result_image.jpg");
        ImageIO.write(reference, "jpg", resultImage);
    }

    public BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight) {
        return Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, newWidth, newHeight);
    }

    public void divideImageIntoBlocks() throws IOException {
        var reference = ImageIO.read(referenceImage);

        int blockWidth = blockSIze, blockHeight = blockSIze;
        int imageWidth = reference.getWidth();
        int imageHeight = reference.getHeight();

        for(int x = 0; x < imageWidth; x+= blockWidth) {
            for(int y = 0; y < imageHeight; y+= blockHeight) {
                int blockH = Math.min(blockHeight, imageHeight - y);
                int blockW = Math.min(blockWidth, imageWidth - x);

                var block = reference.getSubimage(x, y, blockW, blockH);

                var key = x + "_" + y;

                imageMap.put(key, block);
            }
        }

        System.out.println("Image divided into blocks");
    }

    public void mapDataSet() {
        var start = System.currentTimeMillis();
        FilenameFilter imageFilter = (dir, name) -> name.toLowerCase().endsWith(".jpg");

        File[] images = dataset.listFiles(imageFilter);

        if(images == null)
            return;

        System.out.println("Processing images in " + images.length + " images");

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

        for(File imageFile : images) {
            completionService.submit(() -> {
                try {
                    var image = ImageIO.read(imageFile);
                    Color color;
                    color = calcAverageColor(image);
                    mappedDataset.put(imageFile.getAbsolutePath(), color);
                } catch (IOException e) {
                    System.err.println("Erro ao processar a imagem: " + imageFile.getAbsolutePath());
                }
                return  null;
            });
        }

        executor.shutdown();

        var end = System.currentTimeMillis();
        System.out.println("Dataset loaded successfully in " + (end - start) + "ms");
    }

    public void generateImage() throws Exception {
        var start = System.currentTimeMillis();
        System.out.println("Initializing generating image");

        var image = ImageIO.read(this.referenceImage);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (var block : imageMap.entrySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                var key = block.getKey();
                var colorReferenceImage = block.getValue();

                try {
                    var imageMedianColor = calcAverageColor(colorReferenceImage);

                    String closestImagePath = findClosestImagePath(imageMedianColor, mappedDataset);

                    var splitKey = key.split("_");
                    var xPosition = Integer.parseInt(splitKey[0]);
                    var yPosition = Integer.parseInt(splitKey[1]);

                    BufferedImage imageUsed = imageCache.computeIfAbsent(closestImagePath, path -> {
                        try {
                            return ImageIO.read(new File(path));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });

                    if (imageUsed == null) {
                        throw new Exception("ERRO AO RESGATAR IMAGEM");
                    }

                    addImageOnImage(xPosition, yPosition, image, imageUsed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        var end = System.currentTimeMillis();
        System.out.println("Imagem gerada com sucesso em " + (end - start) + "ms");
    }

    private String findClosestImagePath(Color imageMedianColor, Map<String, Color> mappedDataset) {
        double closestDistance = Double.MAX_VALUE;
        String closestImagePath = "";

        for (var entry : mappedDataset.entrySet()) {
            double distance = euclideanDistance(imageMedianColor, entry.getValue());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestImagePath = entry.getKey();
            }
        }

        return closestImagePath;
    }

    public double euclideanDistance(Color c1, Color c2) {
        int euclidianR = c1.getRed() - c2.getRed();
        int euclidianG = c1.getGreen() - c2.getGreen();
        int euclidianB = c1.getBlue() - c2.getBlue();

        double sumEuclidean = Math.pow(euclidianR, 2) + Math.pow(euclidianG, 2) + Math.pow(euclidianB, 2);

        return Math.sqrt(sumEuclidean);
    }

}
