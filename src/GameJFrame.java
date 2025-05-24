import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author 11a5h
 */
public class GameJFrame extends javax.swing.JFrame implements KeyListener {
    private MazeGenerator maze;
    private Player player;
    private PathFinder pathFinder;
    private int size = 50;
    private int cellSize = 60;
    private int score = 0;
    private int moves = 0;
    private int timeRemaining = 120;
    private int initialTime = 120;
    private Timer gameTimer;
    private boolean hintUsed = false;
    private List<Collectible> collectibles = new ArrayList<>();
    private int playerRow = 0;
    private int playerCol = 0;
    private boolean gameStarted = false;
    private boolean gameWon = false;
    private String username;
    private int win = 0;
    private int level = 1;
    
    private final String wallImgPath = "images/wall.png";
    private final String floorImgPath = "images/floor.png";
    private final String startImgPath = "";
    private final String exitImgPath = "images/DoorWin.png";
    private final int PANEL_WIDTH = 700;
    private final int PANEL_HEIGHT = 720;

    /**
     * Creates new form GameJFrame
     */
    public GameJFrame() { 
        initComponents();
        setUpMazePanel();
        this.setLocationRelativeTo(null);
        this.addKeyListener(this);
        this.setFocusable(true);
    }
    
    private void setUpMazePanel() {
        mazePanel.removeAll();
        
        calculateCellSize();
        
        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (maze != null) {
                    Image[][] tiles = maze.getTileImages();
                    
                    int rows = maze.getRows();
                    int cols = maze.getCols();
                    
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            if (tiles[r][c] != null) {
                                g.drawImage(tiles[r][c], c * cellSize, r * cellSize, cellSize, cellSize, null);
                            }
                        }
                    }
                    
                    if (pathFinder != null) {
                        pathFinder.drawPathHighlights(g, cellSize);
                    }
                    
                    for (Collectible collectible : collectibles) {
                        collectible.draw(g, cellSize);
                    }
                    
                    if (gameStarted && player != null && !gameWon) {
                        try {
                            ClassLoader classLoader = getClass().getClassLoader();
                            Image playerImg = new ImageIcon(classLoader.getResource(player.getCurrentImagePath())).getImage();
                            g.drawImage(playerImg, player.getCol() * cellSize, player.getRow() * cellSize, cellSize, cellSize, null);
                        } catch (Exception e) {
                            System.err.println("Không thể tải hình ảnh player: " + e.getMessage());
                        }
                    }
                }
            }
        };
        
        drawPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        mazePanel.setLayout(new BorderLayout());
        mazePanel.add(drawPanel, BorderLayout.CENTER);
        mazePanel.revalidate();
        mazePanel.repaint();
    }
    
    private void calculateCellSize() {
        int cellWidth = PANEL_WIDTH / size;
        int cellHeight = PANEL_HEIGHT / size;
        cellSize = Math.min(cellWidth, cellHeight);
    }
    
    private void generateCollectibles() {
        collectibles.clear();
        int baseCollectibles = Math.max(3, size / 3);
        int bonusCollectibles = (level - 1) * 2;
        int numCollectibles = baseCollectibles + bonusCollectibles;

        for (int i = 0; i < numCollectibles; i++) {
            int r, c;
            int attempts = 0;
            do {
                r = (int) (Math.random() * maze.getRows());
                c = (int) (Math.random() * maze.getCols());
                attempts++;
                if (attempts > 100) {
                    break;
                }
            } while ((r == maze.getStartRow() && c == maze.getStartCol())
                    || (r == maze.getExitRow() && c == maze.getExitCol())
                    || !isWalkablePosition(r, c)
                    || isCollectibleAt(r, c));

            if (isWalkablePosition(r, c)
                    && !(r == maze.getStartRow() && c == maze.getStartCol())
                    && !(r == maze.getExitRow() && c == maze.getExitCol())
                    && !isCollectibleAt(r, c)) {
                try {
                    ClassLoader classLoader = getClass().getClassLoader();
                    Image collectibleImg = new ImageIcon(classLoader.getResource("images/coin.png")).getImage();
                    collectibles.add(new Collectible(r, c, collectibleImg, 5));
                } catch (Exception e) {
                    System.err.println("Không thể tải hình ảnh collectible: " + e.getMessage());
                }
            }
        }
    }
    
    private boolean isCollectibleAt(int row, int col) {
        for (Collectible collectible : collectibles) {
            if (collectible.isAt(row, col)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isWalkablePosition(int row, int col) {
        if (maze == null 
                || row < 0 || row >= maze.getRows() 
                || col < 0 || col >= maze.getCols()) {
            return false;
        }
        MazeGenerator.Cell[][] grid = maze.getGrid();
        return grid[row][col].getValue() == 1;
    }
    
    private void startGameTimer() {
        gameTimer = new Timer(1000, e -> {
            timeRemaining--;
            updateGameInfo();
            
            if (timeRemaining <= 0) {
                gameTimer.stop();
                gameOver(false);
            }
        });
        gameTimer.start();
    }
    
    private void updateGameInfo() {
        timeProgressBar.setValue(timeRemaining);
        timeProgressBar.setString("Thời gian: " + timeRemaining);
    }
    
    private void gameOver(boolean won) {
        gameStarted = false;
        gameWon = won;

        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }

        String message;
        if (won) {
            win++; // Increment win count when player wins
            checkDifficultyProgression(); // Check for level progression
            
            message = "Chúc mừng! Bạn đã thắng!\n"
                    + "Điểm số: " + score + "\n"
                    + "Số bước đi: " + moves + "\n"
                    + "Thời gian còn lại: " + timeRemaining + "s\n"
                    + "Tổng số màn chơi thắng: " + win;

        }
        else {
            message = "Game Over!\n"
                    + "Hết thời gian rồi!\n"
                    + "Điểm số cuối: " + score;
        }

        JOptionPane.showMessageDialog(this, message, won ? "Thắng!" : "Thua!",
                won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        btnStart.setEnabled(true);
        btnGenerate.setEnabled(true);
    }
    
    private void checkDifficultyProgression() {
        if (win % 5 == 0 && win > 0) {
            initialTime = Math.max(10, initialTime - 10);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameStarted || player == null || gameWon) return;
        
        String direction = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                direction = "up";
                break;
            case KeyEvent.VK_DOWN:
                direction = "down";
                break;
            case KeyEvent.VK_LEFT:
                direction = "left";
                break;
            case KeyEvent.VK_RIGHT:
                direction = "right";
                break;
        }
        
        if (direction != null) {
            movePlayer(direction);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    private void movePlayer(String direction) {
        int newRow = player.getRow();
        int newCol = player.getCol();
        
        switch (direction) {
            case "up":
                newRow--;
                break;
            case "down":
                newRow++;
                break;
            case "left":
                newCol--;
                break;
            case "right":
                newCol++;
                break;
        }
        
        if (isValidPlayerMove(newRow, newCol)) {
            player.move(direction, maze.getRows(), maze.getCols());
            moves++;
            score = Math.max(0, score - 1);
            
            checkCollectibles();
            
            if (player.getRow() == maze.getExitRow() && player.getCol() == maze.getExitCol()) {
                score += 50;
                gameOver(true);
            }
            
            updateGameInfo();
            drawPanel.repaint();
        }
    }
    
    private boolean isValidPlayerMove(int row, int col) {
        return isWalkablePosition(row, col);
    }
    
    private void checkCollectibles() {
        for (Collectible collectible : collectibles) {
            if (collectible.isAt(player.getRow(), player.getCol())) {
                collectible.collect();
                score += 5;
                break;
            }
        }
    }
    
    private Collectible findNearestCollectible() {
        Collectible nearest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Collectible collectible : collectibles) {
            if (!collectible.isCollected()) {
                int distance = Math.abs(collectible.getRow() - player.getRow()) + Math.abs(collectible.getCol() - player.getCol());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = collectible;
                }
            }
        }
        return nearest;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mazePanel = new javax.swing.JPanel();
        drawPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        btnGenerate = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnHint = new javax.swing.JButton();
        lblTimer = new javax.swing.JLabel();
        timeProgressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tìm đường về nhà");

        mazePanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        javax.swing.GroupLayout drawPanelLayout = new javax.swing.GroupLayout(drawPanel);
        drawPanel.setLayout(drawPanelLayout);
        drawPanelLayout.setHorizontalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        drawPanelLayout.setVerticalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mazePanelLayout = new javax.swing.GroupLayout(mazePanel);
        mazePanel.setLayout(mazePanelLayout);
        mazePanelLayout.setHorizontalGroup(
            mazePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mazePanelLayout.createSequentialGroup()
                .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(404, Short.MAX_VALUE))
        );
        mazePanelLayout.setVerticalGroup(
            mazePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        controlPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        btnGenerate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnGenerate.setText("Tạo mê cung");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        btnReset.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnStart.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnStart.setText("Bắt đầu");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnExit.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnExit.setText("Thoát");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnHint.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnHint.setText("Gợi ý");
        btnHint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHintActionPerformed(evt);
            }
        });

        lblTimer.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTimer.setText("Thời gian");

        timeProgressBar.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        timeProgressBar.setMaximum(120);
        timeProgressBar.setValue(120);
        timeProgressBar.setString("Thời gian: 120s");
        timeProgressBar.setStringPainted(true);

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHint, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(lblTimer)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(timeProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTimer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(151, 151, 151)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHint, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 295, Short.MAX_VALUE)
                .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mazePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mazePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        // TODO add your handling code here:
        try {
            playerRow = 0;
            playerCol = 0;
            gameStarted = false;
            hintUsed = false;
            maze = new MazeGenerator(size, size, wallImgPath, floorImgPath, startImgPath, exitImgPath);
            drawPanel.setPreferredSize(new Dimension(size * cellSize, size * cellSize));
            mazePanel.revalidate();
            mazePanel.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Lỗi tải ảnh","Error",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGenerateActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có muốn reset game không?\nTất cả tiến trình sẽ bị mất!", 
            "Xác nhận reset", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            if (gameTimer != null && gameTimer.isRunning()) {
                gameTimer.stop();
            }
            
            gameStarted = false;
            gameWon = false;
            score = 0;
            moves = 0;
            timeRemaining = initialTime;
            hintUsed = false;
            player = null;
            pathFinder = null;
            collectibles.clear();

            btnStart.setEnabled(true);
            btnGenerate.setEnabled(true);

            updateGameInfo();
            drawPanel.repaint();
            
            JOptionPane.showMessageDialog(this, "Game đã được reset!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here:
        if (maze == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo mê cung trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (gameStarted) {
            JOptionPane.showMessageDialog(this, "Game đã được bắt đầu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        gameStarted = true;
        gameWon = false;
        score = 0;
        moves = 0;
        timeRemaining = 120;
        hintUsed = false;
        
        player = new Player(maze.getStartRow(), maze.getStartCol());
        pathFinder = new PathFinder(maze);
        generateCollectibles();
        startGameTimer();

        btnStart.setEnabled(false);
        btnGenerate.setEnabled(false);

        updateGameInfo();
        drawPanel.repaint();

        this.requestFocus();
        
        JOptionPane.showMessageDialog(
            this, 
            "Game bắt đầu!\nSử dụng các phím mũi tên để di chuyển\n" +
            "Tìm đường đến cửa ra trong thời gian quy định!",
            "Bắt đầu game",
            JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_btnStartActionPerformed
    
    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có thoát khỏi trò chơi?","Xác nhận thoát",
            JOptionPane.YES_NO_OPTION
        );
    
        if (choice == JOptionPane.YES_OPTION) {
            this.setVisible(false);
            new Home().setVisible(true);
        }
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnHintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHintActionPerformed
        // TODO add your handling code here:
        if (!gameStarted) {
            JOptionPane.showMessageDialog(this, "Vui lòng bắt đầu game trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (hintUsed) {
            JOptionPane.showMessageDialog(this, "Bạn đã hết lượt sử dụng gợi ý!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (gameWon) {
            JOptionPane.showMessageDialog(this, "Game đã kết thúc!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        hintUsed = true;
        score = Math.max(0, score - 10);
        
        pathFinder = new PathFinder(maze, player.getRow(), player.getCol(), maze.getExitRow(), maze.getExitCol());
        
        List<PathFinder.Node> path = pathFinder.findPath();
        
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy đường đi!", "Thông báo", JOptionPane.ERROR_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(
                this,
                "Gợi ý đã được hiển thị!\nĐường đi đến lối ra được tô sáng.\n(Trừ 10 điểm)", 
                "Gợi ý", JOptionPane.INFORMATION_MESSAGE
            );
        }
        
        updateGameInfo();
        drawPanel.repaint();
        this.requestFocus();
    }//GEN-LAST:event_btnHintActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnHint;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnStart;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel drawPanel;
    private javax.swing.JLabel lblTimer;
    private javax.swing.JPanel mazePanel;
    private javax.swing.JProgressBar timeProgressBar;
    // End of variables declaration//GEN-END:variables

}