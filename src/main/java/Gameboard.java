
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Gameboard {

    private static ArrayList<ArrayList<Point>> field = new ArrayList<ArrayList<Point>>();
    private List<Point> rocks = new ArrayList<>();
    private List<Point> horocks = new ArrayList<>();
    private List<Point> lambdas = new ArrayList<>();
    private List<Point> beards = new ArrayList<>();
    private List<Point> razors = new ArrayList<>();
    private static Map<Point, Character> trampolinesInp = new HashMap<>();
    private static Map<Character, Point> trampolinesOut = new HashMap<>();
    private static Map<Character, Character> trampolinesConnections = new HashMap<>();
    private int growth = 25;
    private int growthOnField = 0;
    private int myRazors = 0;
    private int water = 0;
    private int flooding = 0;
    private int waterproof = 10;
    private int score = 0;
    private LinkedList<Character> trueRoute = new LinkedList<>();
    private int numberLambdas = 0;

    private Point robot = new Point(-1, -1, Point.PointState.Robot);
    private Point lift = new Point(-1, -1, Point.PointState.LiftClosed);

    enum State {
        WIN, DEAD, ABORTED, LIVE, WAIT,
    }

    public static State state = State.LIVE;

    public State getState() { return state; }

    public void setState(State newState) { state = newState; }

    public void init(String nameField) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(nameField), StandardCharsets.UTF_8);
            int sizeField = -1;
            for (String line : lines) {
                if (line.length() != 0) sizeField++;
                else break;
            }
            for (int i = 0; i <= sizeField; i++) field.add(new ArrayList());
            for (String line : lines) {
                if (sizeField >= 0) {
                    for (int i = 0; i < line.length(); i++) {
                        switch (line.charAt(i)) {
                            case 'R': {
                                robot.setData(sizeField, i);
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Robot));
                                break;
                            }
                            case 'L': {
                                lift.setData(sizeField, i);
                                field.get(sizeField).add(lift);
                                break;
                            }
                            case '*': {
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Rock));
                                rocks.add(field.get(sizeField).get(i));
                                break;
                            }
                            case '\\': {
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Lambda));
                                lambdas.add(field.get(sizeField).get(i));
                                break;
                            }
                            case '@': {
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Horock));
                                horocks.add(field.get(sizeField).get(i));
                                break;
                            }
                            case 'W': {
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Beard));
                                beards.add(field.get(sizeField).get(i));
                                break;
                            }
                            case '!':
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Razor));
                                razors.add(field.get(sizeField).get(i));
                                break;
                            case '#':
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Wall));
                                break;
                            case ' ':
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Empty));
                                break;
                            case '.':
                                field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Earth));
                                break;
                        }
                        if (line.charAt(i) >= '1' && line.charAt(i) <= '9'){
                            field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Trampoline));
                            trampolinesOut.put(line.charAt(i), field.get(sizeField).get(i));
                        }
                        else if (line.charAt(i) >= 'A' && line.charAt(i) <= 'I'){
                            field.get(sizeField).add(new Point(sizeField, i, Point.PointState.Trampoline));
                            trampolinesInp.put(field.get(sizeField).get(i), line.charAt(i));
                        }
                    }
                } else {
                    if (line.contains("Growth ")) growth = selectionInt(line, 7);
                    else if (line.contains("Razors ")) myRazors = selectionInt(line, 7);
                    else if (line.contains("Water ")) water = selectionInt(line, 6);
                    else if (line.contains("Flooding ")) flooding = selectionInt(line, 9);
                    else if (line.contains("Waterproof ")) waterproof = selectionInt(line, 11);
                    else if (line.contains("Trampoline ")) trampolinesConnections.put(line.charAt(11), line.charAt(21));
                }
                sizeField--;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private int selectionInt(String line, int i) {
        int result = 0;
        while (i != line.length()) {
            result = result * 10 + Character.getNumericValue(line.charAt(i));
            i++;
        }
        return result;
    }

    LinkedList<Point> searchbeardsNeighbors(){
        LinkedList<Point> beardsNeighbors = new LinkedList<Point>();
        for(Point beard: getBeards()){
            beardsNeighbors.add(field.get(beard.getY() + 1).get(beard.getX()));
            beardsNeighbors.add(field.get(beard.getY() - 1).get(beard.getX()));
            beardsNeighbors.add(field.get(beard.getY()).get(beard.getX() + 1));
            beardsNeighbors.add(field.get(beard.getY()).get(beard.getX() - 1));
        }
        return beardsNeighbors;
    }

    void update(LinkedList<Gameboard.Point> route) {
        boolean moveRock = false;
        boolean dropRock = false;
        boolean updateCounters = false;
        if (state == State.ABORTED) gameover();
        if (route != null) {
            for (Point act : route) {
                if (!checkDropNeighbors(act.getY(), act.getX(), robot.getY(), robot.getX())) break;
                if(beards.size() != 0 && searchbeardsNeighbors().contains(field.get(robot.getY()).get(robot.getX())) && myRazors > 0) {
                    deleteBeard();
                    updateCounters();
                    dropRock();
                    break;
                }
                trueRoute.add(convertCommand(act));
                if(trampolinesInp.containsKey(field.get(act.getY()).get(act.getX()))){
                    deleteTrampolines(act);
                    updateCounters();
                    dropRock();
                    break;
                }
                if (field.get(act.getY()).get(act.getX()).getStatePoint() == Point.PointState.Razor){
                    razors.remove(field.get(act.getY()).get(act.getX()));
                    myRazors++;
                }
                for (Point lambda : lambdas) {
                    if (act.getX() == lambda.getX() && act.getY() == lambda.getY()) {
                        lambdas.remove(lambda);
                        numberLambdas++;
                        score += 25;
                        break;
                    }
                }
                moveRock = movingRock(act);
                field.get(act.getY()).get(act.getX()).setStatePoint(Point.PointState.Robot);
                field.get(robot.getY()).get(robot.getX()).setStatePoint(Point.PointState.Empty);
                robot.setData(act.getY(), act.getX());
                dropRock = dropRock();
                if (state == State.DEAD) {
                    trueRoute.remove(trueRoute.size() - 1);
                    state = State.ABORTED;
                    gameover();
                    break;
                }
                updateCounters = updateCounters();
                if (moveRock || dropRock || updateCounters) break;
            }
        }
    }

    private void deleteBeard() {
        trueRoute.add('S');
        for (int j = -1; j < 2; j++) {
            for (int i = -1; i < 2; i++) {
                if (field.get(robot.getY() + j).get(robot.getX() + i).getStatePoint() == Point.PointState.Beard) {
                    beards.remove(field.get(robot.getY() + j).get(robot.getX() + i));
                    field.get(robot.getY() + j).get(robot.getX() + i).setStatePoint(Point.PointState.Empty);
                }
            }
        }
        myRazors--;
    }

    private void deleteTrampolines(Point act){
        Character signTrampOut = trampolinesConnections.get(trampolinesInp.get((field.get(act.y).get(act.x))));
        Point trampOut = trampolinesOut.get(signTrampOut);
        trampolinesOut.remove(signTrampOut);
        Map<Point, Character> trampolinesInput1 = new HashMap<>(trampolinesInp);
        for(Map.Entry<Character, Character> trampolinesRoute : trampolinesConnections.entrySet()) {
            if(trampolinesRoute.getValue() == signTrampOut){
                for(Map.Entry<Point, Character>  input : trampolinesInp.entrySet()){
                    if(input.getValue() == trampolinesRoute.getKey()) {
                        field.get(input.getKey().getY()).get(input.getKey().getX()).setStatePoint(Point.PointState.Empty);
                        trampolinesInput1.remove(input.getKey());
                    }
                }
            }
        }
        trampolinesInp = trampolinesInput1;
        field.get(trampOut.getY()).get(trampOut.getX()).setStatePoint(Point.PointState.Robot);
        field.get(robot.getY()).get(robot.getX()).setStatePoint(Point.PointState.Empty);
        robot.setData(trampOut.getY(), trampOut.getX());
    }

    private boolean movingRock(Point act) {
        boolean moveRock = false;
        List<Point> rocks1 = new ArrayList<>(rocks);
        for (Point rock : rocks) {
            if (rock.getY() == act.getY() && rock.getX() == act.getX()) {
                if (robot.getX() + 1 == act.getX()) {
                    field.get(rock.getY()).get(rock.getX() + 1).setStatePoint(Point.PointState.Rock);
                    rocks1.remove(rock);
                    rocks1.add(field.get(rock.getY()).get(rock.getX() + 1));
                } else {
                    field.get(rock.getY()).get(rock.getX() - 1).setStatePoint(Point.PointState.Rock);
                    rocks1.remove(rock);
                    rocks1.add(field.get(rock.getY()).get(rock.getX() - 1));
                }
                moveRock = true;
                break;
            }
        }
        rocks = rocks1;
        return moveRock;
    }

    private boolean updateCounters() {
        boolean update = false;
        score--;
        growthOnField++;
        if (lambdas.size() == 0) {
            lift.setStatePoint(Point.PointState.LiftOpen);
            field.get(lift.getY()).get(lift.getX()).setStatePoint(Point.PointState.LiftOpen);
            if (robot.getX() == lift.getX() && robot.getY() == lift.getY()) {
                state = State.WIN;
            }
        }
        if (state == State.WIN || state == State.ABORTED) gameover();
        if(growthOnField % growth == 0){
            List<Point> beards1 = new ArrayList<>(beards);
            for(Point beard: beards){
                for(int j = -1; j < 2; j++){
                    for(int i = -1; i < 2; i++){
                        if(field.get(beard.getY() + j).get(beard.getX() + i).getStatePoint() == Point.PointState.Empty) {
                            beards1.add(field.get(beard.getY() + j).get(beard.getX() + i));
                            field.get(beard.getY() + j).get(beard.getX() + i).setStatePoint(Point.PointState.Beard);
                            update = true;
                        }
                    }
                }
            }
            beards = beards1;
        }

        return update;
    }

    private boolean dropRock() {
        List<Point> rocks1 = new ArrayList<>(rocks);
        boolean dropRock = false;
        for (Point rock : rocks) {
            if (field.get(rock.getY() - 1).get(rock.getX()).getStatePoint() == Point.PointState.Empty) {
                if ((rock.getY() - 2 == robot.getY()) & (rock.getX() == robot.getX())) state = State.DEAD;
                else {
                    dropRock = true;
                    field.get(rock.getY() - 1).get(rock.getX()).setStatePoint(Point.PointState.Rock);
                    field.get(rock.getY()).get(rock.getX()).setStatePoint(Point.PointState.Empty);
                    rocks1.remove(rock);
                    rocks1.add(field.get(rock.getY() - 1).get(rock.getX()));
                }
            } else if (field.get(rock.getY() - 1).get(rock.getX()).getStatePoint() == Point.PointState.Rock) {
                if ((field.get(rock.getY() - 1).get(rock.getX() + 1).getStatePoint() == Point.PointState.Empty) &&
                        (field.get(rock.getY()).get(rock.getX() + 1).getStatePoint() == Point.PointState.Empty)) {
                    if ((rock.getY() - 2 == robot.getY()) & (rock.getX() + 1 == robot.getX())) state = State.DEAD;
                    else {
                        dropRock = true;
                        field.get(rock.getY() - 1).get(rock.getX() + 1).setStatePoint(Point.PointState.Rock);
                        field.get(rock.getY()).get(rock.getX()).setStatePoint(Point.PointState.Empty);
                        rocks1.remove(rock);
                        rocks1.add(field.get(rock.getY() - 1).get(rock.getX() + 1));
                    }
                } else if ((field.get(rock.getY() - 1).get(rock.getX() - 1).getStatePoint() == Point.PointState.Empty) &&
                        (field.get(rock.getY()).get(rock.getX() - 1).getStatePoint() == Point.PointState.Empty)) {
                    if ((rock.getY() - 2 == robot.getY()) & (rock.getX() - 1 == robot.getX())) state = State.DEAD;
                    else {
                        dropRock = true;
                        field.get(rock.getY() - 1).get(rock.getX() - 1).setStatePoint(Point.PointState.Rock);
                        field.get(rock.getY()).get(rock.getX()).setStatePoint(Point.PointState.Empty);
                        rocks1.remove(rock);
                        rocks1.add(field.get(rock.getY() - 1).get(rock.getX() - 1));
                    }
                }
            } else if ((field.get(rock.getY() - 1).get(rock.getX()).getStatePoint() == Point.PointState.Lambda) &&
                    (field.get(rock.getY() - 1).get(rock.getX() + 1).getStatePoint() == Point.PointState.Empty) &&
                    (field.get(rock.getY()).get(rock.getX() + 1).getStatePoint() == Point.PointState.Empty)) {
                if ((rock.getY() - 2 == robot.getY()) & (rock.getX() + 1 == robot.getX())) state = State.DEAD;
                else {
                    dropRock = true;
                    field.get(rock.getY() - 1).get(rock.getX() + 1).setStatePoint(Point.PointState.Rock);
                    field.get(rock.getY()).get(rock.getX()).setStatePoint(Point.PointState.Empty);
                    rocks1.remove(rock);
                    rocks1.add(field.get(rock.getY() - 1).get(rock.getX() + 1));
                }
            }
        }
        rocks = rocks1;
        return dropRock;
    }

    private static boolean checkDropNeighbors(int y, int x, int yRobot, int xRobot) {
        boolean dropRock = true;
        if (y <= field.size() - 3) {
            if (x < field.get(y + 2).size() && field.get(yRobot).get(xRobot).getStatePoint() == Point.PointState.Robot) {
                if (x < field.get(y + 1).size() && (field.get(y + 1).get(x).getStatePoint() == Point.PointState.Empty || field.get(y + 1).get(x).getStatePoint() == Point.PointState.Robot)) {
                    if (field.get(y + 2).get(x).getStatePoint() == Point.PointState.Empty) {
                        if (x < field.get(y + 2).size() - 1 && field.get(y + 2).get(x + 1).getStatePoint() == Point.PointState.Rock && field.get(y + 1).get(x + 1).getStatePoint() == Point.PointState.Rock)
                            dropRock = false;
                        if (field.get(y + 2).get(x - 1).getStatePoint() == Point.PointState.Rock && (field.get(y + 1).get(x - 1).getStatePoint() == Point.PointState.Lambda || field.get(y + 1).get(x - 1).getStatePoint() == Point.PointState.Rock))
                            dropRock = false;
                    }
                    if (field.get(y + 2).get(x).getStatePoint() == Point.PointState.Rock)
                        dropRock = false;
                }
            }
        }
        return dropRock;
    }

    private static boolean checkNeighbors(int y, int x, boolean RL, boolean sideMovement){
        if(field.get(y).get(x).getStatePoint() == Point.PointState.LiftClosed) return false;
        else if(field.get(y).get(x).getStatePoint() == Point.PointState.Wall) return false;
        else if(field.get(y).get(x).getStatePoint() == Point.PointState.Beard) return false;
        else if(trampolinesOut.containsValue(field.get(y).get(x))) return false;
        else if(!RL && field.get(y).get(x).getStatePoint() == Point.PointState.Rock) return false;
        else if(RL){
            if(sideMovement){
                if(field.get(y).get(x).getStatePoint() == Point.PointState.Rock){
                    if(x < field.get(y).size() - 1 && field.get(y).get(x + 1).getStatePoint() != Point.PointState.Empty) return false;
                    else if(x < field.get(y).size() - 2 && field.get(y).get(x + 1).getStatePoint() == Point.PointState.Empty &&
                            (field.get(y).get(x + 2).getStatePoint() == Point.PointState.LiftClosed || field.get(y).get(x + 2).getStatePoint() == Point.PointState.LiftOpen)) return false;
                }
            }else{
                if(field.get(y).get(x).getStatePoint() == Point.PointState.Rock){
                    if(x > 0 && field.get(y).get(x - 1).getStatePoint() != Point.PointState.Empty) return false;
                    else if(x > 1 && field.get(y).get(x - 1).getStatePoint() == Point.PointState.Empty &&
                            (field.get(y).get(x - 2).getStatePoint() == Point.PointState.LiftClosed || field.get(y).get(x - 2).getStatePoint() == Point.PointState.LiftOpen)) return false;
                }
            }
        }
        return true;
    }

    private Character convertCommand(Point act) {
        if(robot.getY() + 1 == act.getY()) return 'U';
        else if (robot.getY() - 1 == act.getY()) return 'D';
        else if (robot.getX() + 1 == act.getX()) return 'R';
        else return 'L';
    }

    private void gameover() {
        field.clear();
        lambdas.clear();
        rocks.clear();
        horocks.clear();
        if (state == State.ABORTED) {
            trueRoute.add('A');
            score += numberLambdas * 25;
        }
        else if (state == State.WIN) score += numberLambdas * 50;
        System.out.println(score);
    }

    LinkedList<Character> getTrueRoute() { return trueRoute; }

    Point getLift() { return lift; }

    List<Point> getLambdas() { return lambdas; }

    List<Point> getBeards() { return beards; }

    List<Point> getRazors() { return razors; }

    int getMyRazors() { return myRazors; }

    Point getRobot() { return robot; }

    static class Point {

        enum PointState {
            Robot, Rock, LiftClosed, LiftOpen, Earth, Wall, Lambda, Empty, Horock, Beard, Razor, Trampoline,
        }

        private PointState state;
        private int x;
        private int y;

        public Point(int y, int x, PointState state) {
            this.y = y;
            this.x = x;
            this.state = state;
        }

        public int getX() { return x; }

        public int getY() { return y; }

        public PointState getStatePoint() { return state; }

        public void setData(int newY, int newX) {
            y = newY;
            x = newX;
        }

        public void setStatePoint(PointState newState) {
            state = newState;
        }

        List<Point> getNeighbors() {
            List<Point> neighbor = new ArrayList<>();
            if(trampolinesInp.containsKey(field.get(this.y).get(this.x))){
                neighbor.add(trampolinesOut.get(trampolinesConnections.get(trampolinesInp.get((field.get(this.y).get(this.x))))));
            }else{
                if ((this.y < field.size() - 1) && checkDropNeighbors(this.y + 1, this.x, this.y, this.x) && checkNeighbors(this.y + 1, this.x, false, true))
                    neighbor.add(field.get(this.y + 1).get(this.x));
                if (this.y > 0 && checkNeighbors(this.y - 1, this.x, false, true) && checkDropNeighbors(this.y - 1, this.x, this.y, this.x)){
                    if(this.y < field.size() - 1 && field.get(this.y + 1).get(this.x).getStatePoint() != PointState.Rock) neighbor.add(field.get(this.y - 1).get(this.x));
                    if(this.y == field.size() - 1) neighbor.add(field.get(this.y - 1).get(this.x));
                }
                if ((this.x != field.get(this.y).size() - 1) && checkDropNeighbors(this.y, this.x + 1, this.y, this.x) && checkNeighbors(this.y, this.x + 1, true, true))
                    neighbor.add(field.get(this.y).get(this.x + 1));
                if ((this.x != 0) && checkDropNeighbors(this.y, this.x - 1, this.y, this.x) && checkNeighbors(this.y, this.x - 1, true, false))
                    neighbor.add(field.get(this.y).get(this.x - 1));
            }
            return neighbor;
        }
    }
}
