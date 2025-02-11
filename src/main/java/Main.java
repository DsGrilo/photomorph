import imageprocess.ImageProcess;

public class Main {
    public static void main(String[] args) {
        var process = new ImageProcess();

        try {
            process.mapDataSet();
            process.divideImageIntoBlocks();
            process.generateImage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}