import java.util.*;

import java.util.List;

import static java.lang.Integer.*;

public class Solver {
    private Gameboard gameboard;

    public Solver(Gameboard gameboard) {
        this.gameboard = gameboard;
    }

    private LinkedList<Gameboard.Point> makeRouteMin(Gameboard.Point startPoint, List<Gameboard.Point> finishPoint) {
        int minLength = MAX_VALUE;
        Map<Gameboard.Point, LinkedList<Gameboard.Point>> shortestPaths = BFS.BFS(startPoint, finishPoint);
        LinkedList<Gameboard.Point> route = new LinkedList<>();
        if (!shortestPaths.isEmpty()) {
            for(Map.Entry<Gameboard.Point, LinkedList<Gameboard.Point>> shortestPath : shortestPaths.entrySet()) {
                int length = shortestPath.getValue().size() - 1;
                if (length < minLength && length >= 1) {
                    minLength = length;
                    route = shortestPath.getValue();
                } else if (length == minLength) {
                    minLength = length;
                    List<Gameboard.Point> pars = new ArrayList<>();
                    pars.add(route.get(route.size() - 1));
                    pars.add(shortestPath.getValue().get(shortestPath.getValue().size() - 1));
                    if (longRouteToLift(gameboard.getLift(), pars)) route = shortestPath.getValue();
                }
            }
            route.remove(gameboard.getRobot());
        } else gameboard.setState(Gameboard.State.ABORTED);
        return route;
    }

    private boolean longRouteToLift(Gameboard.Point startPoint, List<Gameboard.Point> finishPoint) {
        Map<Gameboard.Point, LinkedList<Gameboard.Point>> shortestPaths = BFS.BFS(startPoint, finishPoint);
        return shortestPaths.get(finishPoint.get(0)) == null || shortestPaths.get(finishPoint.get(1)) != null && (shortestPaths.get(finishPoint.get(0)).size() - 1 <= shortestPaths.get(finishPoint.get(1)).size() - 1);
    }

    private LinkedList<Gameboard.Point> makeRouteToPoint(Gameboard.Point start, Gameboard.Point finish) {
        List<Gameboard.Point> liftOpen = new ArrayList<>();
        liftOpen.add(finish);
        Map<Gameboard.Point, LinkedList<Gameboard.Point>> shortestPaths = BFS.BFS(start, liftOpen);
        LinkedList<Gameboard.Point> route = shortestPaths.get(gameboard.getLift());
        if(!shortestPaths.isEmpty()) route.remove(start);
        else gameboard.setState(Gameboard.State.ABORTED);
        return route;
    }

    public void makeMove() {
        while (gameboard.getState() != Gameboard.State.WIN & gameboard.getState() != Gameboard.State.ABORTED) {
            LinkedList route = new LinkedList();
            if(gameboard.getBeards().size() != 0){
                if (gameboard.getMyRazors() != 0) route = makeRouteMin(gameboard.getRobot(), gameboard.searchbeardsNeighbors());
                else if(gameboard.getMyRazors() + gameboard.getRazors().size() > gameboard.getBeards().size()) route = makeRouteMin(gameboard.getRobot(), gameboard.getRazors());
            }
            if (route.isEmpty()){
                if (gameboard.getLift().getStatePoint() == Gameboard.Point.PointState.LiftOpen) route = makeRouteToPoint(gameboard.getRobot(), gameboard.getLift());
                else route = makeRouteMin(gameboard.getRobot(), gameboard.getLambdas());
            }
            gameboard.update(route);
        }
        LinkedList trueRoute = gameboard.getTrueRoute();
        for(int i = 0; i < trueRoute.size(); i++){
            System.out.print(trueRoute.get(i));
        }
    }
}
