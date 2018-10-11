import java.util.*;

public class BFS {
    static Map<Gameboard.Point, LinkedList<Gameboard.Point>> BFS(Gameboard.Point start, List<Gameboard.Point> finish){
        Queue<Gameboard.Point> queue = new LinkedList<>();
        List<Gameboard.Point> visited = new ArrayList<>();
        Map<Gameboard.Point, Gameboard.Point> parentPoints = new HashMap<>();
        Map<Gameboard.Point, LinkedList<Gameboard.Point>> shortestPath = new HashMap<>();
        queue.add(start);
        visited.add(start);
        int i = 1000;
        while(!queue.isEmpty() && i > 0){
            Gameboard.Point next = queue.poll();
            for(Gameboard.Point neighbor : next.getNeighbors()){
                if(!visited.contains(neighbor)){
                    visited.add(neighbor);
                    parentPoints.put(neighbor, next);
                    queue.add(neighbor);
                }
            }
            if(finish.contains(next)){
                Gameboard.Point point = next;
                LinkedList<Gameboard.Point> shortPath = new LinkedList<>();
                while (point != null){
                    shortPath.add(point);
                    point = parentPoints.get(point);
                }
                Collections.reverse(shortPath);
                shortestPath.put(next, shortPath);
            }
            i--;
        }
        return shortestPath;
    }
}
