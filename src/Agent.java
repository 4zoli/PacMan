import game.pm.strategies.Strategy;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import game.engine.utils.Pair;
import game.pm.PMAction;
import game.pm.PMGame;
import game.pm.PMPlayer;
import game.pm.PMGame.DIRECTION;
import game.pm.PMGame.TILES;

public class Agent extends Strategy {

    private int lastDirection = -1;

    @Override
    public int getDirection(int id, PMGame game) {

        // step1
        final Set<Integer> validDirections = this.getValidDirections(game.pacmans[id]);

        // step2
        final Collection<Integer> sortedDirections = this.sortDirections(validDirections, id, game);
        // step 3
        // szimulaljunk es nezzuk meg hogy melyik a legjobb
        for (Integer direction : sortedDirections) {
            PMGame cloned = game.clone();
            for (int j = 0; j < PMGame.TILES.SIZE / cloned.pacmans[id].getSpeed() + 1; j++) {
                cloned.setAction(cloned.pacmans[id], new PMAction(direction), 0);
            }

            if (game.lives > cloned.lives) {
                continue;
            }

            lastDirection = direction;
            return direction;
        }
        // fucked nincs olyan lepes amibe nem halunk bele
        return 0;
    }

    private Set<Integer> getValidDirections(PMPlayer player) {
        return IntStream.range(0, 4)
                .boxed()
                .collect(Collectors.toSet());
    }

    private Collection<Integer> sortDirections(Set<Integer> directions, int id, PMGame game) {
        final Pair<Integer, Integer> position = game.pacmans[id].getTilePosition();
        List<Pair<Integer, Double>> collect = directions.parallelStream()
                .map(direction -> {
                    double score = firstFood(position.first, position.second, direction, game);
                    if (lastDirection == direction) {
                         score*= 0.93d;
                    }

                    return new Pair<Integer, Double>(direction, score);
                })
                .sorted((p2, p1) -> (int) (p2.second - p1.second))
                .collect(Collectors.toList());


        Optional<Pair<Integer, Double>> lastRoute = collect.stream().filter(p -> p.first == lastDirection && p.second < Integer.MAX_VALUE).findFirst();

        if(lastRoute.isPresent() && Math.abs(collect.get(0).second-lastRoute.get().second) <10){
            collect.add(0,lastRoute.get());
        }

        return collect.stream().map(p -> p.first).collect(Collectors.toList());
    }

    private int firstFood(int x, int y, int direction, PMGame game) {
        return this.firstFood(x, y, direction, direction, game, 0, new HashSet<>());
    }

    private int firstFood(int x, int y, int prevDirection, int direction, PMGame game, int depth, Collection<Integer> checked) {
        //revers direction detection
        if (Math.abs(prevDirection - direction) == 2 && prevDirection != -1) {
            return Integer.MAX_VALUE;
        }

        //step to the direction
        final int nextX;
        final int nextY;
        if (direction == DIRECTION.UP) {
            nextX = x - 1;
            nextY = y;
        } else if (direction == DIRECTION.DOWN) {
            nextX = x + 1;
            nextY = y;
        } else if (direction == DIRECTION.LEFT) {
            nextX = x;
            nextY = y - 1;
        } else {
            nextX = x;
            nextY = y + 1;
        }

        //to limit the thinking time.
        depth++;
        if (depth >= 120) {
            return Integer.MAX_VALUE;
        }

        final int check = (String.valueOf(nextX) + String.valueOf(nextY)).hashCode();
        if (checked.contains(check)) {
            return Integer.MAX_VALUE;
        }

        checked.add(check);

        //check if its a wall
        if (game.getTileAt(nextX, nextY) == TILES.WALL || game.getTileAt(nextX, nextY) == TILES.SLOW) {
            return Integer.MAX_VALUE;
        }

        //food check
        if (isEatAble(game.getTileAt(nextX, nextY))) {
            return 1;
        } else {
            //check other directions
            final int min = getMin(
                    firstFood(nextX, nextY, direction, DIRECTION.UP, game, depth, checked),
                    firstFood(nextX, nextY, direction, DIRECTION.DOWN, game, depth, checked),
                    firstFood(nextX, nextY, direction, DIRECTION.LEFT, game, depth, checked),
                    firstFood(nextX, nextY, direction, DIRECTION.RIGHT, game, depth, checked)
            );

            return min != Integer.MAX_VALUE ? min + 1 : min;
        }
    }

    private int getMin(int... i) {
        return IntStream.of(i).min().getAsInt();
    }

    private boolean isEatAble(int tileType) {

        switch (tileType) {
            case TILES.ENERGIZER:
            case TILES.FOOD:
            case TILES.FRIGHT_BLINKY:
            case TILES.FRIGHT_CLYDE:
            case TILES.FRIGHT_INKY:
            case TILES.FRIGHT_PINKY:
                return true;
            default:
                return false;
        }

    }
}
