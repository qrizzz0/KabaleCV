import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Iterator;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class ContourDetection extends JFrame {
    public BufferedImage imgIn, imgOut;
    public JPanel panel;
    public JLabel input, bw, output;
    public Mat hsvImage;
    public Mat mask;
    public Mat in, org;
    public Iterator<MatOfPoint> iterator;
    public List<MatOfPoint> contours, apcontours;
    public Integer i;
    private ImageIcon icon0, icon1, icon2;
    private enum Type{
        RIGHT, LEFT, UP, DOWN;
    }



    public ContourDetection (String title, String imgname) {
        in = new Mat();
        org = new Mat();
        hsvImage = new Mat();
        mask = new Mat();
        Mat grey = new Mat();
        Mat canny = new Mat();
        contours = new ArrayList<>();
        apcontours = new ArrayList<>();
        double maxArea = 0;
        i = 0;
        MatOfPoint max_contour = new MatOfPoint();


        try {
            imgIn = ImageIO.read(new File(imgname));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        in = Imgcodecs.imread(imgname);

        Imgproc.resize(in, in, new Size(1500, 1500), 0, 0, Imgproc.INTER_NEAREST);

        Imgproc.blur(in, in, new Size(2, 2));
        in.copyTo(org);
        Imgproc.cvtColor(in, grey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(in, hsvImage, Imgproc.COLOR_BGR2HSV);
        //Imgproc.Canny(grey, canny, 10, 100);

        Core.inRange(hsvImage,  new Scalar(90,100, 100), new Scalar(120, 255, 255), mask);
        Imgproc.findContours(mask,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours){
            MatOfPoint2f fcontour = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f apcontour = new MatOfPoint2f();
            double epsilon = 0.01*Imgproc.arcLength(fcontour, true);
            Imgproc.approxPolyDP(fcontour, apcontour, epsilon, true);

            if(apcontour.total() == 4) {
                apcontours.add(new MatOfPoint(apcontour.toArray()));
            }
        }

        Imgproc.drawContours(in, contours, 0, new Scalar(0,0,0), 2);
        imgOut = matToBufferedImage(in);

        input = new JLabel();
        output = new JLabel();

        icon0 = new ImageIcon(imgIn.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
        icon1 = new ImageIcon(imgOut.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
        input.setIcon(icon0);
        output.setIcon(icon1);
        panel = new JPanel();
        panel.add(input);
        panel.add(output);
        this.setTitle(title);
        this.add(panel);
    }

    public void nextContour(){
        org.copyTo(in);
        if (i < apcontours.size()-1){
            i++;
        } else {
            i = 0;
        }
            Imgproc.drawContours(in, apcontours, i, new Scalar(0, 0, 0), 2);
            imgOut = matToBufferedImage(in);
            icon1 = new ImageIcon(imgOut.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
            output.setIcon(icon1);
            System.out.println("Now printing contour: " + i);
            System.out.println("Total number: " + apcontours.get(i).total());
            System.out.println("Area of contour: " + Imgproc.contourArea(apcontours.get(i)));
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

        ContourDetection cd = new ContourDetection("Contour Detection","res/realboard_pic/realboard8.png");
        cd.setPreferredSize(new Dimension(1800, 900));
        cd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // reagér paa luk
        cd.pack();                       // saet vinduets stoerrelse
        cd.setVisible(true);                      // aabn vinduet
        while (cd.isEnabled()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cd.nextContour();
        }
    }

}
