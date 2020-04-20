import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;


public class ColorDetection {

    public BufferedImage imgIn, imgOut;
    public JFrame frame;
    public JPanel panel;
    public JLabel input;
    public JLabel output;
    public Mat hsvImage;
    public Mat mask;
    public Mat temp;
    private ImageIcon icon1, icon2;
    private enum Type{
        RIGHT, LEFT, UP, DOWN;
    }

    public ColorDetection (String imgname) {

        Mat in;
        Mat blurredImage = new Mat();
        Mat resized_mask = new Mat();
        Mat matchres = new Mat();
        mask = new Mat();
        hsvImage = new Mat();
        try {
            imgIn = ImageIO.read(new File(imgname));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        in = Imgcodecs.imread(imgname);
        temp = Imgcodecs.imread("res/Templates/t1.png");

        Imgproc.blur(in, blurredImage, new Size(5, 5));

        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2GRAY);

        Core.inRange(hsvImage,  new Scalar(100,150, 150), new Scalar(120, 255, 255), mask);


        //Imgproc.cvtColor(mask, outputImage, Imgproc.COLOR_HSV2BGR);

        Imgproc.resize(mask, resized_mask, new Size(1280, 720), 0, 0, Imgproc.INTER_LINEAR);

        Imgproc.matchTemplate(resized_mask, temp, matchres, Imgproc.TM_CCOEFF);

        Core.MinMaxLocResult mmr = Core.minMaxLoc(matchres);
        Point matchLoc=mmr.maxLoc;
        double [] datapt = new double[resized_mask.channels()];
        double xs = matchLoc.x - 10;
        double ys = matchLoc.y;
        while(datapt[0] == 0.0){
            datapt = resized_mask.get((int) ys, (int) xs);
            ys += 1.0;
            if(resized_mask.rows() <= ys) {
                System.out.println("break");
                break;
            }
        }
        Rect t1 = getSubSqr(resized_mask, (int) ys, (int) xs);

/*
        ArrayList values = new ArrayList<>();

        for (int i = 0; i < resized_mask.rows(); i++) {
            for (int j = 0; j < resized_mask.cols(); j++) {
                for(int k = 0; k < resized_mask.channels(); k++) {
                    values.add((int) resized_mask.get(i, j)[k]);
                }
            }
        }
        try {
            FileOutputStream maskdump = new FileOutputStream("maskdump.txt");
            maskdump.write(values.toString().getBytes());
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
*/

        //Draw rectangle on result image
        Imgproc.rectangle(resized_mask, matchLoc, new Point(matchLoc.x + temp.cols(),
                matchLoc.y + temp.rows()), new Scalar(255, 255, 255));

        Imgproc.rectangle(resized_mask, t1, new Scalar(80, 255, 255));

        imgOut = matToBufferedImage(resized_mask);

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
    private Rect getSubSqr(Mat mask, int y, int x){
        //find right most point
        int right = dfs(mask, y, x, Type.RIGHT).x;
        //find left most point
        int left = dfs(mask, y, x, Type.LEFT).x;
        //find highest point
        int up = dfs(mask, y, x, Type.UP).y;
        //find lowest point
        int down = dfs(mask, y, x, Type.DOWN).y;
        //create dimension
        return new Rect(left, up, right - left, down - up);
    }
    private java.awt.Point dfs(Mat mask, int y, int x, Type type){
        java.awt.Point thispt = new java.awt.Point(x, y);
        double rightdata, leftdata, updata, downdata;
        switch (type){
            case UP:
                //Get data from up neighbour
                updata = mask.get(y-1, x)[0];

                //Get data from left neighbour
                leftdata = mask.get(y, x-1)[0];

                //check neighbour data for valid pixel
                if (updata > 0) return dfs(mask, y-1, x, type);
                else if(leftdata > 0) return dfs(mask, y, x-1, type);

                //return point of right most valid pixel.
                return thispt;
            case DOWN:
                //Get data from left neighbour
                rightdata = mask.get(y, x+1)[0];

                //Get data from lower neighbour
                downdata = mask.get(y+1, x)[0];

                //check neighbour data for valid pixel
                if (downdata > 0) return dfs(mask, y+1, x, type);
                else if(rightdata > 0) return dfs(mask, y, x+1, type);

                //return point of right most valid pixel.
                return thispt;
            case LEFT:
                //Get data from right neighbour
                leftdata = mask.get(y, x-1)[0];

                //Get data from lower neighbour
                updata = mask.get(y-1, x)[0];

                //check neighbour data for valid pixel
                if (leftdata > 0) return dfs(mask, y, x-1, type);
                else if(updata > 0) return dfs(mask, y-1, x, type);

                //return point of right most valid pixel.
                return thispt;
            case RIGHT:
                //Get data from right neighbour
                rightdata = mask.get(y, x+1)[0];

                //Get data from lower neighbour
                downdata = mask.get(y+1, x)[0];


                //check neighbour data for valid pixel
                if (rightdata > 0) return dfs(mask, y, x+1, type);
                else if(downdata > 0) return dfs(mask, y+1, x, type);

                //return point of right most valid pixel.
                return thispt;

            default:
                return null;
        }
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
