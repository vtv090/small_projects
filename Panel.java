package new_snacke;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;


public class Panel extends JPanel implements Runnable, KeyListener {

    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;
    //Game Stuff
    private final int SIZE = 10;
    //Render
    private Graphics2D g2d;
    private BufferedImage image;
    private boolean running;
    private long targetTime;
    private Entity head, apple;
    private ArrayList<Entity> snake;
    private int score, level, highscore;
    private boolean gameover, start;
    //movement
    private int dx, dy;
    private key currentKey;

    public GamePanel() {
        setFocusable(true);
        requestFocus();
        addKeyListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //Game Loop
        Thread thread = new Thread(this);
        thread.start();
    }

    private void setFPS(int fps) {
        targetTime = 300 / fps;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_UP:
                currentKey = key.up;
                break;
            case KeyEvent.VK_DOWN:
                currentKey = key.down;
                break;
            case KeyEvent.VK_LEFT:
                currentKey = key.left;
                break;
            case KeyEvent.VK_RIGHT:
                currentKey = key.right;
                break;
            case KeyEvent.VK_ENTER:
                currentKey = key.left;
                start = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_ENTER:
                currentKey = key.none;
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void run() {
        if (running) return;
        init();
        loadData();
        long startTime, elapsed, wait;
        while (running) {
            startTime = System.nanoTime();

            update();
            requestRender();

            elapsed = System.nanoTime() - startTime;

            wait = targetTime - elapsed / 10000000;
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void init() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        running = true;
        setUplevel();
    }

    private void loadData() {
        String home = System.getProperty("user.home");
        File save = new File(home + File.separator + ".snake.dat");
        if (save.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(save));
                highscore = (Integer) in.readObject();
                in.close();
            } catch (FileNotFoundException e) {
                System.out.println("No previous data");
            } catch (IOException | ClassCastException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Error loading data");
            }
        } else highscore = 0;
    }

    private void saveData() {
        String home = System.getProperty("user.home");
        File save = new File(home + File.separator + ".snake.dat");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(save));
            out.writeObject(highscore);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving data");
        }
    }

    private void setUplevel() {
        snake = new ArrayList<Entity>();
        head = new Entity(SIZE);
        head.setPosition(WIDTH / 2, HEIGHT / 2);
        snake.add(head);
        for (int i = 1; i < 5; i++) {
            Entity e = new Entity(SIZE);
            e.setPosition(head.getX() + (i * SIZE), head.getY());
            snake.add(e);
        }
        apple = new Entity(SIZE);
        setApple();
        score = 0;
        gameover = false;
        level = 1;
        dx = dy = 0;
        setFPS(level * 4);

    }

    public void setApple() {
        int x = (int) (Math.random() * (WIDTH - SIZE));
        int y = (int) (Math.random() * (HEIGHT - SIZE));
        x = x - (x % SIZE);
        y = y - (y % SIZE);
        apple.setPosition(x, y);
    }

    private void update() {
        if (gameover) {
            if (start) {
                setUplevel();
            }
            return;
        }
        if (currentKey == key.up && dy == 0) {
            dy = -SIZE;
            dx = 0;
        }
        if (currentKey == key.down && dy == 0) {
            dy = SIZE;
            dx = 0;
        }
        if (currentKey == key.left && dx == 0) {
            dy = 0;
            dx = -SIZE;
        }
        if (currentKey == key.right && dx == 0 && dy != 0) {
            dy = 0;
            dx = SIZE;
        }

        if (dx != 0 || dy != 0) {
            for (int i = snake.size() - 1; i > 0; i--) {

                snake.get(i).setPosition(
                        snake.get(i - 1).getX(),
                        snake.get(i - 1).getY()
                );
            }
            head.move(dx, dy);
        }

        for (Entity e : snake) {
            if (e.isCollsion(head)) {
                gameover = true;
                break;
            }

        }

        if (apple.isCollsion(head)) {
            score++;
            highscore = Math.max(highscore, score);
            setApple();

            Entity e = new Entity(SIZE);
            e.setPosition(-100, -100);
            snake.add(e);
            if (score % 5 == 0) {
                level++;
                if (level > 5) level = 5;
                setFPS(level * 4);
            }
        }
        //code for game over if snake hit wall
        gameover = (head.getX() < 0 || head.getY() < 0 || head.getX() >= WIDTH || head.getY() >= HEIGHT || gameover);
    }

    private void requestRender() {
        render(g2d);
        Graphics g = getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
    }

    public void render(Graphics2D g2d) {
        g2d.clearRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.red);
        for (Entity e : snake) {
            e.render(g2d);
        }

        g2d.setColor(Color.RED);
        apple.render(g2d);
        if (gameover) {
            g2d.drawString("GAME OVER! ", 150, 200);
            saveData();
        }

        g2d.setColor(Color.WHITE);
        String heading = String.format("%-10s%-12d%-10s%-12d%-14s%d", "Score: ", score, "Level:", level, "Highscore: ", highscore);
        g2d.drawString(heading, 10, 10);
        if (dx == 0 && dy == 0) {
            g2d.drawString("Let's go! ", 150, 200);

        }
    }

    //key input
    private enum key {up, down, right, left, none}

}
