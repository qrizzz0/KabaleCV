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



    public ContourDetection (String title, String imgname) {
        in = new Mat();
        org = new Mat();
        hsvImage = new Mat();
        mask = new Mat();
        Mat allcontoursimg = new Mat();
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

        Imgproc.blur(in, in, new Size(5, 5));
        in.copyTo(org);
        in.copyTo(allcontoursimg);
        Imgproc.cvtColor(in, grey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(in, hsvImage, Imgproc.COLOR_BGR2HSV);
        //Imgproc.Canny(grey, canny, 10, 100);

        Core.inRange(hsvImage,  new Scalar(90,35, 50), new Scalar(150, 255, 160), mask);
        Imgproc.findContours(mask,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        /*
        for (MatOfPoint contour : contours){
            MatOfPoint2f fcontour = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f apcontour = new MatOfPoint2f();
            double epsilon = 0.009*Imgproc.arcLength(fcontour, true);
            Imgproc.approxPolyDP(fcontour, apcontour, epsilon, true);

            if(apcontour.total() == 4 && Imgproc.contourArea(apcontour) >= 1000) {
                System.out.println("Epsilon: " + epsilon + "\n");
                apcontours.add(new MatOfPoint(apcontour.toArray()));
            }
        }
        */

        for (MatOfPoint cnt : contours){
            Point [] vertices = new Point [4];
            MatOfPoint2f fcontour = new MatOfPoint2f(cnt.toArray());
            MatOfPoint apcontour;
            RotatedRect rect = Imgproc.minAreaRect(fcontour);
            rect.points(vertices);
            apcontour = new MatOfPoint(vertices);

            if(Imgproc.contourArea(apcontour) >= 6000) apcontours.add(apcontour);
        }
        Imgproc.drawContours(allcontoursimg, contours, -1, new Scalar(0,0,0), 2);
        Imgcodecs.imwrite("res/output.png", allcontoursimg);
        imgOut = matToBufferedImage(in);

        input = new JLabel();
        bw = new JLabel();
        output = new JLabel();

        icon0 = new ImageIcon(imgIn.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
        icon1 = new ImageIcon(matToBufferedImage(mask).getScaledInstance(800, 480, Image.SCALE_SMOOTH));
        icon2 = new ImageIcon(imgOut.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
        input.setIcon(icon0);
        bw.setIcon(icon1);
        output.setIcon(icon2);
        panel = new JPanel();
        panel.add(input);
        panel.add(bw);
        panel.add(output);
        this.setTitle(title);
        this.add(panel);
    }

    public void nextContour(){
        org.copyTo(in);
        if (i >= apcontours.size()) i = 0;

            Imgproc.drawContours(in, apcontours, i, new Scalar(0, 0, 0), 2);
            imgOut = matToBufferedImage(in);
            icon2 = new ImageIcon(imgOut.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
            output.setIcon(icon2);
            System.out.println("Now printing contour: " + i);
            System.out.println("Total number: " + apcontours.get(i).total());
            System.out.println("Area of contour: " + Imgproc.contourArea(apcontours.get(i)));
            System.out.println();

        i++;
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

        ContourDetection cd = new ContourDetection("Contour Detection","res/realboard_pic/realboard2.png");
        cd.setPreferredSize(new Dimension(1800, 900));
        cd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // reagér paa luk
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
