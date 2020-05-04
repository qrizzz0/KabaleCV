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
    public JLabel input, bw, output;
    public Mat hsvImage;
    public Mat mask;
    private ImageIcon icon0, icon1, icon2;
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

        double start = System.currentTimeMillis();
        Imgproc.blur(in, blurredImage, new Size(5, 5));

        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsvImage,  new Scalar(100,150, 150), new Scalar(120, 255, 255), mask);


        //Imgproc.cvtColor(mask, outputImage, Imgproc.COLOR_HSV2BGR);

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

        Imgproc.resize(mask, resized_mask, new Size(1280, 720), 0, 0, Imgproc.INTER_NEAREST);
        ArrayList<Rect> rects = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        File[] files = new File("res/Templates/").listFiles();
        for (File file : files){
            System.out.println(file.getPath());
            ArrayList<Rect> found = matchTemplates(resized_mask, file.getPath());
            for(Rect rect : found){
                rects.add(rect);
                labels.add(file.getName());
            }

        }

        rescaleRectToFit(resized_mask, in, rects);

        drawRectangles(in, rects, labels);

        Imgcodecs.imwrite("res/output.png", in);
        Imgcodecs.imwrite("res/output2.png", mask);

        double end = System.currentTimeMillis();

        System.out.println("Time spent (ms): " + (end - start));
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

        frame = new JFrame("Color Detection");
        frame.add(panel);

    }
    private void rescaleRectToFit(Mat org, Mat comp, ArrayList<Rect> rects){
        for(int i = 0; i < rects.size(); i++){
            double width = rects.get(i).width*((double) comp.cols()/org.cols());
            double height = rects.get(i).height*((double) comp.rows()/org.rows());
            double x =  comp.cols()*((double)rects.get(i).x / org.cols());
            double y = comp.rows()*((double)rects.get(i).y / org.rows());
            rects.set(i, new Rect((int)x, (int)y, (int)width, (int)height));
        }

    }
    private void drawRectangles(Mat resized_mask, ArrayList<Rect> rects, ArrayList<String> labels){
        //Draw rectangle on result image
        if(resized_mask.channels() <= 1) Imgproc.cvtColor(resized_mask, resized_mask, Imgproc.COLOR_GRAY2BGR);

        for(int i = 0; i < rects.size(); i++) {
            Rect rect = rects.get(i);
            String label = labels.get(i);
            Imgproc.rectangle(resized_mask, rect, new Scalar(0, 0, 255));
            Imgproc.putText(resized_mask, label,new Point(rect.x, rect.y-10), Imgproc.FONT_ITALIC, 1.5, new Scalar(0,0,255));
        }
    }

    private ArrayList<Rect> matchTemplates (Mat resized_mask, String stemp){
        ArrayList<Rect> rects = new ArrayList<>();
        Mat temp = Imgcodecs.imread(stemp);
        Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2GRAY);
        Mat matchres = new Mat();

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
        Rect rect = getSubSqr(resized_mask, (int) ys, (int) xs);
        rects.add(new Rect(matchLoc, new Point(matchLoc.x + temp.cols(), matchLoc.y + temp.rows())));
        rects.add(rect);
        return rects;
    }

    private Rect getSubSqr(Mat mask, int y, int x){
        //find right most point
        java.awt.Point right = dfs(mask, y, x, Type.RIGHT);
        //find left most point
        java.awt.Point left = dfs(mask, y, x, Type.LEFT);
        //find highest point
        java.awt.Point up = dfs(mask, y, x, Type.UP);
        //find lowest point
        java.awt.Point down = dfs(mask, y, x, Type.DOWN);

        String s = new String();
        s = "Right: " + right.toString()+"\n";
        s += "Left: " + left.toString()+"\n";
        s += "Up: " + up.toString()+"\n";
        s += "Down: " + down.toString()+"\n";
        System.out.println(s);

        //create dimension
        return new Rect(left.x, up.y, right.x - left.x, down.y - up.y);
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
                downdata = mask.get(y+1, x)[0];

                //check neighbour data for valid pixel
                if (leftdata > 0) return dfs(mask, y, x-1, type);
                else if(downdata > 0) return dfs(mask, y+1, x, type);

                //return point of right most valid pixel.
                return thispt;
            case RIGHT:
                //Get data from right neighbour
                rightdata = mask.get(y, x+1)[0];

                //Get data from lower neighbour
                updata = mask.get(y-1, x)[0];

                //check neighbour data for valid pixel
                if (rightdata > 0) return dfs(mask, y, x+1, type);
                else if(updata > 0) return dfs(mask, y-1, x, type);

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

        ColorDetection cd = new ColorDetection("res/board.png");
        cd.frame.setPreferredSize(new Dimension(1800, 900));
        cd.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // reagér paa luk
        cd.frame.pack();                       // saet vinduets stoerrelse
        cd.frame.setVisible(true);                      // aabn vinduet

    }

}
