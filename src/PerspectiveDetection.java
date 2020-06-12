import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PerspectiveDetection extends JFrame {

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



    public PerspectiveDetection (String title, String imgname) {
        int largestindex = 0;
        Mat grey = new Mat();
        Mat canny = new Mat();
        Mat cannyimg = new Mat();
        Mat cnthiarchy = new Mat();
        Mat boardimg = new Mat();
        contours = new ArrayList<>();
        Point [] vertices = new Point[4];
        MatOfPoint maxcnt = new MatOfPoint();
        double maxarea = 0;
        try {
            imgIn = ImageIO.read(new File(imgname));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        in = Imgcodecs.imread(imgname);
        in.copyTo(boardimg);

        Imgproc.GaussianBlur(in, in, new Size(5, 5), 0);

        Imgproc.cvtColor(in, grey, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("res/greypic.png", grey);
        Imgproc.Canny(grey, canny, 10, 70);
        canny.copyTo(cannyimg);
        Imgproc.cvtColor(cannyimg, cannyimg,Imgproc.COLOR_GRAY2BGR);

        Imgproc.findContours(canny,contours,cnthiarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        for(int i = 0; i < contours.size(); i++){
            double area = Imgproc.contourArea(contours.get(i));
            if (area >= maxarea) {
                maxarea = area;
                maxcnt = contours.get(i);
                largestindex = i;
            }
        }
        MatOfPoint2f m2f = new MatOfPoint2f(maxcnt.toArray());
        MatOfPoint2f approx = new MatOfPoint2f();
        double epsilon = 0.01 * Imgproc.arcLength(m2f, true);

        //If are contour has less vertices then 4, then we cannot approximate and we will never be able to isolate the board.
        if(m2f.total() < 4) return;
        //Find an epsilon which approximates the contour to an rectangle.
        if(m2f.total() > 4) {
            while (approx.total() != 4){
                Imgproc.approxPolyDP(m2f, approx, epsilon, true);
                if(approx.total() > 4){
                    epsilon += 1;
                } else if( approx.total() < 4){
                    epsilon -= 1;
                }
                if (epsilon <= 0) return;
            }

        }
        System.out.println("Vertices in approx = " + approx.total());
        System.out.println("Epsilon = " + epsilon);


        /*
        RotatedRect rect = Imgproc.minAreaRect(m2f);
        MatOfPoint2f points = new MatOfPoint2f();
        Imgproc.boxPoints(rect, points);
        System.out.println(points);
        */
        List<MatOfPoint> ls = new ArrayList<>();
        ls.add(new MatOfPoint(approx.toArray()));
        Imgproc.drawContours(cannyimg, ls, -1, new Scalar(0, 0 ,255), 5);
        imgOut = matToBufferedImage(boardimg);

        input = new JLabel();
        bw = new JLabel();
        output = new JLabel();

        icon0 = new ImageIcon(imgIn.getScaledInstance(800, 480, Image.SCALE_SMOOTH));
        icon1 = new ImageIcon(matToBufferedImage(cannyimg).getScaledInstance(800, 480, Image.SCALE_SMOOTH));
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

    public static void main (String [] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        PerspectiveDetection pd = new PerspectiveDetection("Perspective Detection", "res/boardpics/pic10.jpg");
        pd.setPreferredSize(new Dimension(1800, 900));
        pd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // reagér paa luk
        pd.pack();                       // saet vinduets stoerrelse
        pd.setVisible(true);                      // aabn vinduet
    }
}
