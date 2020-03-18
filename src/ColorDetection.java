import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;


public class ColorDetection {

    public BufferedImage imgIn, imgOut;
    public JFrame frame;
    public JPanel panel;
    public JLabel input;
    public JLabel output;
    public Mat hsvImage;
    public Mat mask, mask1, mask2;
    private ImageIcon icon1, icon2;


    public ColorDetection(String imgname){

        Mat in;
        Mat blurredImage = new Mat();
        Mat outputImage = new Mat();
        mask = new Mat();
        mask1 = new Mat();
        mask2 = new Mat();
        hsvImage = new Mat();
        try {
            imgIn = ImageIO.read(new File(imgname));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        in = Imgcodecs.imread(imgname);
        Imgproc.blur(in, blurredImage, new Size(3, 3));
        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsvImage,  new Scalar(0,50, 155), new Scalar(5, 255, 255), mask1);
        Core.inRange(hsvImage, new Scalar(175, 50, 155), new Scalar(180, 255, 255), mask2);

        Core.bitwise_or(mask1, mask2, mask);
        //mask1 = mask1 + mask2;

        //Imgproc.cvtColor(mask, outputImage, Imgproc.COLOR_HSV2BGR);
        imgOut = matToBufferedImage(mask);

        input = new JLabel();
        output = new JLabel();

        icon1 = new ImageIcon(imgIn);
        icon2 = new ImageIcon(imgOut);
        input.setIcon(icon1);
        output.setIcon(icon2);
        panel = new JPanel();
        panel.add(input);
        panel.add(output);

        frame = new JFrame("Color Detection");
        frame.add(panel);

    }

    /*https://github.com/opencv-java/object-detection/commit/b6c2afe355c34ff6b103961142f5f0e2601d024f*/
    private BufferedImage matToBufferedImage(Mat original)
    {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1)
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }
        else
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    public static void main (String[] args){


        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ColorDetection cd = new ColorDetection("res/cards.jpg");
        cd.frame.setPreferredSize(new Dimension(1360, 640));
        cd.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // reagér paa luk
        cd.frame.pack();                       // saet vinduets stoerrelse
        cd.frame.setVisible(true);                      // aabn vinduet

    }

}
