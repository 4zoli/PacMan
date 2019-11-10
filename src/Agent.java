import game.pm.strategies.Strategy;
import javafx.geometry.Pos;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.ws.FaultAction;

import game.engine.utils.Pair;
import game.pm.PMAction;
import game.pm.PMGame;
import game.pm.PMPlayer;
import game.pm.PMGame.DIRECTION;
import game.pm.PMGame.TILES;

public class Agent extends Strategy {

	private final static Map<Integer, Integer> TILE_SCORE;

	private int lastDirection = -1;

	static {
		TILE_SCORE = new HashMap<>();
		TILE_SCORE.put(TILES.EMPTY, -1);
		TILE_SCORE.put(TILES.FOOD, 100);
		TILE_SCORE.put(TILES.FRIGHT_BLINKY, 210);
		TILE_SCORE.put(TILES.FRIGHT_CLYDE, 210);
		TILE_SCORE.put(TILES.FRIGHT_INKY, 210);
		TILE_SCORE.put(TILES.FRIGHT_PINKY, 210);
		TILE_SCORE.put(TILES.ENERGIZER, 130);
	}

	@Override
	public int getDirection(int id, PMGame game) {

		// step1
		final Set<Integer> validDirections = this.getValidDirections(game.pacmans[id]);

		// step2
		final Set<Integer> sortedDirections = this.sortDirections(validDirections, id, game);

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
		final Set<Integer> directions = new HashSet<>();
		directions.add(0);
		directions.add(1);
		directions.add(2);
		directions.add(3);

//		for (int direction = 0; direction < 4; direction++) {
//			if (player.canTurnAndMove(direction)) {
//				directions.add(direction);
//			}
//		}
		return directions;
	}

	private Set<Integer> sortDirections(Set<Integer> directions, int id, PMGame game) {
		final Set<Integer> sortedDirections = new LinkedHashSet<>(4);

		final Pair<Integer, Integer> position = game.pacmans[id].getTilePosition();

		List<Pair<Integer, Integer>> scorePairs = new ArrayList<>(directions.size());

		for (final Integer direction : directions) {
			int score = 0;
			int steps = 0;
			int distance = 0;
			if (direction == DIRECTION.UP) {
				for (int i = position.first - 1; game.getTileAt(i, position.second) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					steps++;

					if (score > 0) {
						distance = steps;
					}
				}
			}

			else if (direction == DIRECTION.DOWN) {
				for (int i = position.first + 1; game.getTileAt(i, position.second) != TILES.WALL; i++) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					steps++;

					if (score > 0) {
						distance = steps;
					}
				}
			}

			else if (direction == DIRECTION.LEFT) {
				for (int i = position.second - 1; game.getTileAt(position.first, i) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(position.first, i), 0);
					steps++;

					if (score > 0) {
						distance = steps;
					}
				}
			}

			else if (direction == DIRECTION.RIGHT) {
				for (int i = position.second + 1; game.getTileAt(position.first, i) != TILES.WALL; i++) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(position.first, i), 0);
					steps++;

					if (score > 0) {
						distance = steps;
					}
				}
			}

			score += steps * 2;
			if (direction == lastDirection && steps != 0) {
				score += 90;
			}
			scorePairs.add(new Pair<Integer, Integer>(direction, (int) (score + score * 0.11f)));
		}

		// System.out.println(String.valueOf(position) +String.valueOf(scorePairs));
		return new LinkedHashSet<>(scorePairs.stream().sorted((p1, p2) -> p2.second - p1.second).map(p -> p.first)
				.collect(Collectors.toList()));
	}
}
