
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

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
    private List<Node> path;
    private List<Node> exploredNodes;
    
    // Định nghĩa class Node để lưu thông tin mỗi bước đi
    public static class Node {
        int row, col;
        Node parent;
        int g; // Khoảng cách từ điểm bắt đầu
        int h; // Heuristic (ước tính khoảng cách đến đích)
        int f; // f = g + h
        
        public Node(int row, int col) {
            this.row = row;
            this.col = col;
            this.parent = null;
            this.g = 0;
            this.h = 0;
            this.f = 0;
        }
        
        public int getF() {
            return f;
        }
        
        public int getG() {
            return g;
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
        // Giả sử vị trí (r,c) không phải tường khi tiles[r][c] không phải là wallImg
        // Đây là một phương pháp đơn giản, có thể cần điều chỉnh dựa trên cách lưu trữ thực tế
        return tiles[r][c] != null && !isWallImage(tiles[r][c]);
    }
    
    private boolean isWallImage(Image img) {
        // So sánh với hình ảnh tường
        // Thực tế cần kiểm tra nội dung hình ảnh hoặc sử dụng thuộc tính khác để phân biệt
        // Giả sử hình ảnh tường có kích thước khác với các hình ảnh khác (đây chỉ là giả định)
        return img.toString().contains("wall"); // Giả định tên file chứa "wall"
    }
    
    // Tìm đường đi bằng thuật toán BFS
    public List<Node> findPathBFS() {
        resetSearch();
        
        Queue<Node> queue = new LinkedList<>();
        visited = new boolean[rows][cols];
        
        // Thêm điểm bắt đầu vào hàng đợi
        Node startNode = new Node(startRow, startCol);
        queue.add(startNode);
        visited[startRow][startCol] = true;
        
        // Các hướng di chuyển có thể (Trên, Phải, Dưới, Trái)
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            exploredNodes.add(current);
            
            // Nếu đã đến đích
            if (current.row == exitRow && current.col == exitCol) {
                reconstructPath(current);
                return path;
            }
            
            // Thử đi theo từng hướng
            for (int[] dir : directions) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                // Kiểm tra xem ô mới có hợp lệ không
                if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                    Node neighbor = new Node(newRow, newCol);
                    neighbor.parent = current;
                    
                    queue.add(neighbor);
                    visited[newRow][newCol] = true;
                }
            }
        }
        
        return Collections.emptyList(); // Không tìm thấy đường đi
    }
    
    // Tìm đường đi bằng thuật toán DFS
    public List<Node> findPathDFS() {
        resetSearch();
        
        Stack<Node> stack = new Stack<>();
        visited = new boolean[rows][cols];
        
        // Thêm điểm bắt đầu vào ngăn xếp
        Node startNode = new Node(startRow, startCol);
        stack.push(startNode);
        
        // Các hướng di chuyển có thể (Trên, Phải, Dưới, Trái)
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        
        while (!stack.isEmpty()) {
            Node current = stack.pop();
            
            // Nếu đã thăm nút này rồi, bỏ qua
            if (visited[current.row][current.col]) {
                continue;
            }
            
            visited[current.row][current.col] = true;
            exploredNodes.add(current);
            
            // Nếu đã đến đích
            if (current.row == exitRow && current.col == exitCol) {
                reconstructPath(current);
                return path;
            }
            
            // Thử đi theo từng hướng
            for (int[] dir : directions) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                // Kiểm tra xem ô mới có hợp lệ không
                if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                    Node neighbor = new Node(newRow, newCol);
                    neighbor.parent = current;
                    
                    stack.push(neighbor);
                }
            }
        }
        
        return Collections.emptyList(); // Không tìm thấy đường đi
    }
    
    // Tìm đường đi bằng thuật toán A*
    public List<Node> findPathAStar() {
        resetSearch();
        
        // Sử dụng PriorityQueue để luôn lấy nút có f = g + h thấp nhất
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        visited = new boolean[rows][cols];
        
        // Thêm điểm bắt đầu vào openSet
        Node startNode = new Node(startRow, startCol);
        startNode.g = 0;
        startNode.h = calculateHeuristic(startRow, startCol);
        startNode.f = startNode.g + startNode.h;
        
        openSet.add(startNode);
        
        // Các hướng di chuyển có thể (Trên, Phải, Dưới, Trái)
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            exploredNodes.add(current);
            
            // Nếu đã thăm nút này rồi, bỏ qua
            if (visited[current.row][current.col]) {
                continue;
            }
            
            visited[current.row][current.col] = true;
            
            // Nếu đã đến đích
            if (current.row == exitRow && current.col == exitCol) {
                reconstructPath(current);
                return path;
            }
            
            // Thử đi theo từng hướng
            for (int[] dir : directions) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                // Kiểm tra xem ô mới có hợp lệ không
                if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                    Node neighbor = new Node(newRow, newCol);
                    neighbor.parent = current;
                    
                    // Tính toán g, h, f cho nút láng giềng
                    neighbor.g = current.g + 1; // Khoảng cách từ điểm bắt đầu
                    neighbor.h = calculateHeuristic(newRow, newCol); // Heuristic
                    neighbor.f = neighbor.g + neighbor.h;
                    
                    openSet.add(neighbor);
                }
            }
        }
        
        return Collections.emptyList(); // Không tìm thấy đường đi
    }
    
    // Tìm đường đi bằng thuật toán Hill Climbing
    public List<Node> findPathHillClimbing() {
        resetSearch();
        
        visited = new boolean[rows][cols];
        Node current = new Node(startRow, startCol);
        current.h = calculateHeuristic(startRow, startCol);
        
        // Các hướng di chuyển có thể (Trên, Phải, Dưới, Trái)
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        
        while (true) {
            visited[current.row][current.col] = true;
            exploredNodes.add(current);
            
            // Nếu đã đến đích
            if (current.row == exitRow && current.col == exitCol) {
                reconstructPath(current);
                return path;
            }
            
            // Tìm nút láng giềng tốt nhất (có h nhỏ nhất)
            Node bestNeighbor = null;
            int bestH = Integer.MAX_VALUE;
            
            for (int[] dir : directions) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                // Kiểm tra xem ô mới có hợp lệ không
                if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                    int h = calculateHeuristic(newRow, newCol);
                    
                    if (h < bestH) {
                        bestH = h;
                        if (bestNeighbor == null) {
                            bestNeighbor = new Node(newRow, newCol);
                            bestNeighbor.parent = current;
                            bestNeighbor.h = h;
                        } else {
                            bestNeighbor.row = newRow;
                            bestNeighbor.col = newCol;
                            bestNeighbor.parent = current;
                            bestNeighbor.h = h;
                        }
                    }
                }
            }
            
            // Nếu không tìm thấy láng giềng nào tốt hơn hoặc không còn đường đi
            if (bestNeighbor == null || bestNeighbor.h >= current.h) {
                // Thử backtrack (quay lui) hoặc dừng thuật toán
                if (current.parent != null) {
                    current = current.parent;
                } else {
                    break; // Không tìm thấy đường đi
                }
            } else {
                current = bestNeighbor;
            }
        }
        
        return Collections.emptyList(); // Không tìm thấy đường đi
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
    public Image[][] getPathImages(Image pathImg, Image visitedImg) {
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
