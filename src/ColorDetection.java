import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ColorDetection {

    public BufferedImage imgIn, imgOut;
    public JFrame frame;
    public JPanel panel;
    public JLabel input;
    public JLabel output;
    public Mat hsvImage;
    public Mat mask;
    private ImageIcon icon1, icon2;


    public ColorDetection (String imgname){

        Mat in;
        Mat blurredImage = new Mat();
        mask = new Mat();
        hsvImage = new Mat();
        try {
            imgIn = ImageIO.read(new File(imgname));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        in = Imgcodecs.imread(imgname);
        Imgproc.blur(in, blurredImage, new Size(5, 5));
        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsvImage,  new Scalar(100,150, 150), new Scalar(120, 255, 255), mask);


        //Imgproc.cvtColor(mask, outputImage, Imgproc.COLOR_HSV2BGR);

        mask = Imgcodecs.imread("res/output.png");
        imgOut = matToBufferedImage(mask);

        Imgcodecs.imwrite("res/output.png", mask);
        input = new JLabel();
        output = new JLabel();


        icon1 = new ImageIcon(imgIn.getScaledInstance(800, 600, Image.SCALE_SMOOTH));
        icon2 = new ImageIcon(imgOut.getScaledInstance(800, 600, Image.SCALE_SMOOTH));
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

    public static void main (String [] args){

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ColorDetection cd = new ColorDetection("res/Board.jpg");
        cd.frame.setPreferredSize(new Dimension(1800, 900));
        cd.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // reagér paa luk
        cd.frame.pack();                       // saet vinduets stoerrelse
        cd.frame.setVisible(true);                      // aabn vinduet

    }

}
