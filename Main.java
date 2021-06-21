package new_snacke;

import java.awt.Dimension;
import javax.swing.JFrame;
/*
* @author: Van Trung Vo
* @Version: 1.0
* */
public class Main {

    public static void main(String[]args){
        JFrame frame = new JFrame("Classic Snake");
        frame.setContentPane(new GamePanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();

        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
