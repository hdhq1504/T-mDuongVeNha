import java.awt.Image;
import java.io.IOException;
import java.util.Random;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 11a5h
 */
public class MazeGenerator {
    private int rows;
    private int cols;
    private Cell[][] grid;
    private Image[][] tileImages;
    private Image wallImg, floorImg, startImg, exitImg;
    private int startRow, startCol; // Vị trí điểm bắt đầu
    private int exitRow, exitCol;   // Vị trí điểm kết thúc
    private Random random;
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public int getStartRow() {
        return startRow;
    }
    
    public int getStartCol() {
        return startCol;
    }
    
    public int getExitRow() {
        return exitRow;
    }
    
    public int getExitCol() {
        return exitCol;
    }
    
    // Lớp Cell để lưu trữ thông tin của mỗi ô trong mê cung
    private static class Cell {
        int value;      // 0: tường, 1: đường đi
        boolean visited;
        
        Cell() {
            value = 0;  // Mặc định là tường
            visited = false;
        }
    }
    
    public MazeGenerator(int rows, int cols, String wallImgPath, String floorImgPath, 
            String startImgPath, String exitImgPath) throws IOException {
        this.rows = rows;
        this.cols = cols;
        this.random = new Random();

        loadImages(wallImgPath, floorImgPath, startImgPath, exitImgPath);
        
        // Khởi tạo lưới mê cung
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
        
        // Tạo mê cung bằng thuật toán DFS
        generateMaze();
        
        // Chọn ngẫu nhiên điểm kết thúc (phải khác với điểm bắt đầu)
        do {
            exitRow = random.nextInt(rows);
            exitCol = random.nextInt(cols);
        } while (exitRow == startRow && exitCol == startCol);
        
        // Đảm bảo điểm kết thúc luôn là đường đi
        grid[exitRow][exitCol].value = 1;
        
        // Đảm bảo có đường đi đến điểm kết thúc
        ensurePathToExit();
        
        // Tạo mảng hình ảnh để hiển thị
        createTileImages();
    }
    
    private void loadImages(String wallImgPath, String floorImgPath, 
            String startImgPath, String exitImgPath) throws IOException {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            wallImg = new ImageIcon(classLoader.getResource(wallImgPath)).getImage();
            floorImg = new ImageIcon(classLoader.getResource(floorImgPath)).getImage();
            startImg = new ImageIcon(classLoader.getResource(startImgPath)).getImage();
            exitImg = new ImageIcon(classLoader.getResource(exitImgPath)).getImage();
        } catch (Exception e) {
            throw new IOException("Không thể đọc file hình ảnh: " + e.getMessage());
        }
    }
      
    private void generateMaze() {
        grid[startRow][startCol].value = 1; // Đánh dấu là đường đi
        dfs(startRow, startCol);
    }
    
    private void dfs(int r, int c) {
        // Đánh dấu ô hiện tại đã thăm
        grid[r][c].visited = true;
        grid[r][c].value = 1; // Đánh dấu là đường đi
        
        // Các hướng di chuyển có thể (Bắc, Đông, Nam, Tây)
        int[][] directions = {{-2,0}, {0,2}, {2,0}, {0,-2}};
        
        // Xáo trộn các hướng để tạo mê cung ngẫu nhiên
        shuffleArray(directions);
        
        // Thử đi theo từng hướng
        for (int[] dir : directions) {
            int newR = r + dir[0];
            int newC = c + dir[1];
            
            // Kiểm tra ô mới có hợp lệ và chưa thăm
            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols 
                    && !grid[newR][newC].visited) {
                // Phá tường giữa hai ô (tạo đường đi)
                grid[r + dir[0]/2][c + dir[1]/2].value = 1;
                dfs(newR, newC);
            }
        }
    }
    
    private void ensurePathToExit() {
        // Tìm các ô lân cận có thể kết nối với điểm kết thúc
        int[][] neighbors = {
            {exitRow-1, exitCol}, // Trên
            {exitRow, exitCol+1}, // Phải
            {exitRow+1, exitCol}, // Dưới
            {exitRow, exitCol-1}  // Trái
        };
        
        // Xáo trộn các hướng
        shuffleNeighbors(neighbors);
        
        boolean pathCreated = false;
        
        // Thử kết nối từ một trong các ô lân cận
        for (int[] neighbor : neighbors) {
            int r = neighbor[0];
            int c = neighbor[1];
            
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                // Nếu ô lân cận là đường đi, ta đã có đường đi tới điểm kết thúc
                if (grid[r][c].value == 1) {
                    pathCreated = true;
                    break;
                }
            }
        }
        
        // Nếu chưa có đường đi, tạo một đường đi từ một ô lân cận
        if (!pathCreated) {
            for (int[] neighbor : neighbors) {
                int r = neighbor[0];
                int c = neighbor[1];
                
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    grid[r][c].value = 1; // Tạo đường đi từ ô lân cận
                    
                    // Tiếp tục tìm đường từ ô lân cận này đến phần còn lại của mê cung
                    connectToMaze(r, c);
                    pathCreated = true;
                    break;
                }
            }
        }
    }
    
    private void connectToMaze(int r, int c) {
        // Tìm ô gần nhất là đường đi trong mê cung hiện tại
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newR = r + dir[0];
            int newC = c + dir[1];
            
            // Nếu là ô hợp lệ và là đường đi và không phải là điểm kết thúc
            if (newR >= 0 && newR < rows && newC >= 0 && newC < cols 
                    && grid[newR][newC].value == 1 
                    && !(newR == exitRow && newC == exitCol)) {
                // Đã tìm thấy đường kết nối, không cần làm gì thêm
                return;
            }
        }
        
        // Nếu không tìm thấy đường kết nối gần, tiếp tục tạo đường ngẫu nhiên
        int[] dir = directions[random.nextInt(directions.length)];
        int newR = r + dir[0];
        int newC = c + dir[1];
        
        if (newR >= 0 && newR < rows && newC >= 0 && newC < cols 
                && !(newR == exitRow && newC == exitCol)) {
            grid[newR][newC].value = 1;
            connectToMaze(newR, newC);
        }
    }
    
    private void shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    
    private void shuffleNeighbors(int[][] neighbors) {
        for (int i = neighbors.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = neighbors[i];
            neighbors[i] = neighbors[j];
            neighbors[j] = temp;
        }
    }
    
    private void createTileImages() {
        tileImages = new Image[rows][cols];
        
        // Gán hình ảnh tương ứng cho từng ô
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == startRow && c == startCol) {
                    tileImages[r][c] = startImg; // Điểm bắt đầu
                } else if (r == exitRow && c == exitCol) {
                    tileImages[r][c] = exitImg;  // Điểm kết thúc
                } else {
                    tileImages[r][c] = (grid[r][c].value == 1) ? floorImg : wallImg;
                }
            }
        }
    }
    
    public Image[][] getTileImages() {
        return tileImages;
    }

    public void debugGrid() {
        System.out.println("\nMaze Grid (" + rows + "x" + cols + "):");
        System.out.println("0: Wall, 1: Path");
        System.out.println("Start: (" + startRow + "," + startCol + ")");
        System.out.println("Exit: (" + exitRow + "," + exitCol + ")");
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == startRow && c == startCol) {
                    System.out.print("S ");
                } else if (r == exitRow && c == exitCol) {
                    System.out.print("E ");
                } else {
                    System.out.print(grid[r][c].value + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
