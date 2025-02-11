package imageprocess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import org.imgscalr.Scalr;

public class ImageProcess {
    File referenceImage = new File("src/main/java/assets/image/image_reference_.jpg");
    HashMap<String, BufferedImage> imageMap = new HashMap<>();
    HashMap<String, Color> mappedDataset = new HashMap<>();
    File dataset = new File("src/main/java/assets/image/dataset");
    int blockSIze = 10;

    public Color calcAverageColor(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();

        long sumR = 0, sumG = 0, sumB = 0;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Color colorPixel = new Color(image.getRGB(x, y));
                sumR += colorPixel.getRed();
                sumG += colorPixel.getGreen();
                sumB += colorPixel.getBlue();
            }
        }

        int totalPixels = width * height;
        int averageR = (int) (sumR / totalPixels);
        int averageG = (int) (sumG / totalPixels);
        int averageB = (int) (sumB / totalPixels);

        return new Color(averageR, averageG, averageB);
    }

    public void addImageOnImage(int xPosition, int yPosition, BufferedImage reference, BufferedImage image) throws IOException {
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


        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ArrayList<Future<?>> futures = new ArrayList<>();

        for(File imageFile : images) {
            futures.add(executor.submit(() -> {
                try {
                    var image = ImageIO.read(imageFile);
                    Color color;
                    color = calcAverageColor(image);
                    mappedDataset.put(imageFile.getAbsolutePath(), color);
                } catch (IOException e) {
                    System.err.println("Erro ao processar a imagem: " + imageFile.getAbsolutePath());
                }
            }));
        }

        for(var future : futures) {
            try {
                future.get();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        var end = System.currentTimeMillis();
        System.out.println("Dataset loaded successfully in " + (end - start) + "ms");
    }

    public void generateImage() throws Exception {
        var start = System.currentTimeMillis();

        var image = ImageIO.read(this.referenceImage);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ArrayList<Future<?>> futures = new ArrayList<>();

        Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();

        for(var block : imageMap.entrySet()) {
            futures.add(executor.submit(() -> {
                String closestImagePath = "";
                double closestDistance = Double.MAX_VALUE;

                var key = block.getKey();
                var colorReferenceImage = block.getValue();

                try{
                    var imageMedianColor = calcAverageColor(colorReferenceImage);

                    for(var images : mappedDataset.entrySet()){
                        var path = images.getKey();
                        var colorUsedImage = images.getValue();

                        var euclidianResult = euclideanDistance(imageMedianColor, colorUsedImage);

                        if(euclidianResult < closestDistance){
                            closestDistance = euclidianResult;
                            closestImagePath = path;
                        }
                    }

                    var splitKey = key.split("_");
                    var xPosition = Integer.parseInt(splitKey[0]);
                    var yPosition = Integer.parseInt(splitKey[1]);

                    if(closestImagePath.isEmpty()){
                        throw new Exception("ERRO AO RESGATAR IMAGEM");
                    }

                    BufferedImage imageUsed = imageCache.computeIfAbsent(closestImagePath, path -> {
                        try {
                            return ImageIO.read(new File(path));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });

                    addImageOnImage(xPosition, yPosition, image, imageUsed);
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }));
        }

        for (var future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        var end = System.currentTimeMillis();
        System.out.println("Imagem gerada com sucesso em " + (end - start) + "ms");
    }

    public double euclideanDistance(Color c1, Color c2) {
        int euclidianR = c1.getRed() - c2.getRed();
        int euclidianG = c1.getGreen() - c2.getGreen();
        int euclidianB = c1.getBlue() - c2.getBlue();

        double sumEuclidian = Math.pow(euclidianR, 2) + Math.pow(euclidianG, 2) + Math.pow(euclidianB, 2);

        return Math.sqrt(sumEuclidian);
    }

}
