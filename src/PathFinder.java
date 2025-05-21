
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 11a5h
 */
public class PathFinder {
    private MazeGenerator maze;
    private int[][] grid;
    private boolean[][] visited;
    private int rows, cols;
    private int startRow, startCol;
    private int exitRow, exitCol;
    private Image pathImg, visitedImg;
    private List<Node> path;
    private List<Node> exploredNodes;
    
    // Định nghĩa class Node để lưu thông tin mỗi bước đi
    public static class Node {
        int row, col;
        Node parent;
        int h; // Heuristic (ước tính khoảng cách đến đích)
        
        public Node(int row, int col) {
            this.row = row;
            this.col = col;
            this.parent = null;
            this.h = 0;
        }
        
        public int getH() {
            return h;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return row == node.row && col == node.col;
        }
    }
    
    public PathFinder(MazeGenerator maze) {
        this.maze = maze;
        this.rows = maze.getRows();
        this.cols = maze.getCols();
        this.startRow = maze.getStartRow();
        this.startCol = maze.getStartCol();
        this.exitRow = maze.getExitRow();
        this.exitCol = maze.getExitCol();
        
        // Tạo bản sao lưới để tìm đường
        createGridCopy();
        
        // Khởi tạo các biến khác
        this.path = new ArrayList<>();
        this.exploredNodes = new ArrayList<>();
        
        loadImages();
    }
    
    private void loadImages() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            pathImg = new ImageIcon(classLoader.getResource("images/floor.png")).getImage();
            visitedImg = new ImageIcon(classLoader.getResource("images/footprint.png")).getImage();
        } catch (Exception e) {
            System.err.println("Không thể tải hình ảnh: " + e.getMessage());
        }
    }
    
    private void createGridCopy() {
        grid = new int[rows][cols];
        Image[][] tiles = maze.getTileImages();
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Phân biệt đường đi và tường dựa trên hình ảnh
                if (r == startRow && c == startCol || r == exitRow && c == exitCol) {
                    grid[r][c] = 1; // Điểm bắt đầu và kết thúc là đường đi
                } else {
                    // Ảnh là floorImg thì là đường đi (1), ngược lại là tường (0)
                    grid[r][c] = (isTileWalkable(r, c, tiles)) ? 1 : 0;
                }
            }
        }
    }
    
    private boolean isTileWalkable(int r, int c, Image[][] tiles) {
        // Logic đơn giản để kiểm tra xem ô có phải là đường đi hay không
        return tiles[r][c] != null && !isWallImage(tiles[r][c]);
    }
    
    private boolean isWallImage(Image img) {
        // So sánh với hình ảnh tường
        return img.toString().contains("wall");
    }
    
    // Tìm đường đi bằng thuật toán Hill Climbing
    public List<Node> findPath() {
        resetSearch();
        
        visited = new boolean[rows][cols];
        Node startNode = new Node(startRow, startCol);
        startNode.h = calculateHeuristic(startRow, startCol);
        
        // Bắt đầu quá trình tìm kiếm từ điểm xuất phát
        boolean found = hillClimbingWithBacktracking(startNode);
        
        if (found) {
            return path;
        } else {
            return Collections.emptyList();
        }
    }
    
    private boolean hillClimbingWithBacktracking(Node current) {
        // Đánh dấu nút hiện tại đã thăm
        visited[current.row][current.col] = true;
        exploredNodes.add(current);
        
        // Nếu đã đến đích
        if (current.row == exitRow && current.col == exitCol) {
            reconstructPath(current);
            return true;
        }
        
        // Các hướng di chuyển có thể (Trên, Phải, Dưới, Trái)
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        
        // Tìm tất cả các nút láng giềng hợp lệ
        List<Node> neighbors = new ArrayList<>();
        
        for (int[] dir : directions) {
            int newRow = current.row + dir[0];
            int newCol = current.col + dir[1];
            
            // Kiểm tra xem ô mới có hợp lệ không và chưa thăm
            if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                Node neighbor = new Node(newRow, newCol);
                neighbor.parent = current;
                neighbor.h = calculateHeuristic(newRow, newCol);
                neighbors.add(neighbor);
            }
        }
        
        // Sắp xếp các láng giềng theo giá trị heuristic (tăng dần)
        Collections.sort(neighbors, (n1, n2) -> Integer.compare(n1.h, n2.h));
        
        // Thử từng láng giềng theo thứ tự heuristic tốt nhất
        for (Node neighbor : neighbors) {
            if (hillClimbingWithBacktracking(neighbor)) {
                return true; // Tìm thấy đường đi
            }
        }
        
        // Không tìm thấy đường đi từ nút hiện tại, quay lui
        return false;
    }
    
    // Tính toán heuristic (khoảng cách Manhattan đến đích)
    private int calculateHeuristic(int row, int col) {
        return Math.abs(row - exitRow) + Math.abs(col - exitCol);
    }
    
    // Kiểm tra xem một bước đi có hợp lệ không
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols && grid[row][col] == 1;
    }
    
    // Tạo lại đường đi từ đích về điểm bắt đầu
    private void reconstructPath(Node endNode) {
        path.clear();
        Node current = endNode;
        
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        
        Collections.reverse(path);
    }
    
    // Reset lại trạng thái tìm kiếm
    private void resetSearch() {
        path.clear();
        exploredNodes.clear();
    }
    
    // Lấy danh sách các nút đã khám phá
    public List<Node> getExploredNodes() {
        return exploredNodes;
    }
    
    // Lấy đường đi đã tìm thấy
    public List<Node> getPath() {
        return path;
    }
    
    // Cập nhật hình ảnh với đường đi
    public Image[][] getPathImages() {
        Image[][] tiles = maze.getTileImages();
        Image[][] result = new Image[rows][cols];
        
        // Sao chép mảng gốc
        for (int r = 0; r < rows; r++) {
            System.arraycopy(tiles[r], 0, result[r], 0, cols);
        }
        
        // Đánh dấu các nút đã khám phá
        for (Node node : exploredNodes) {
            if (node.row == startRow && node.col == startCol || 
                node.row == exitRow && node.col == exitCol) {
                continue; // Giữ nguyên điểm bắt đầu và kết thúc
            }
            result[node.row][node.col] = visitedImg;
        }
        
        // Đánh dấu đường đi
        for (Node node : path) {
            if (node.row == startRow && node.col == startCol || 
                node.row == exitRow && node.col == exitCol) {
                continue; // Giữ nguyên điểm bắt đầu và kết thúc
            }
            result[node.row][node.col] = pathImg;
        }
        
        return result;
    }
}
