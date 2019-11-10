///azoli,horvath.oliver.zoltan@stud.u-szeged.hu
import game.pm.strategies.Strategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import game.engine.utils.Pair;
import game.pm.PMAction;
import game.pm.PMGame;
import game.pm.PMGame.DIRECTION;
import game.pm.PMGame.TILES;

public class Agent extends Strategy {
	/**
	 * 
	 */
	private final static Map<Integer, Integer> TILE_SCORE;
	/**
	 * 
	 */
	static {
		TILE_SCORE = new HashMap<>();
		TILE_SCORE.put(TILES.EMPTY, 1);
		TILE_SCORE.put(TILES.FOOD, 100);
		TILE_SCORE.put(TILES.FRIGHT_BLINKY, 210);
		TILE_SCORE.put(TILES.FRIGHT_CLYDE, 210);
		TILE_SCORE.put(TILES.FRIGHT_INKY, 210);
		TILE_SCORE.put(TILES.FRIGHT_PINKY, 210);
		TILE_SCORE.put(TILES.ENERGIZER, 130);
	}
	
	/**
	 * Sulyozzuk vele azt az iranyt ahonnan jottunk, hogy ne forogjon oda vissza egyhelyben. 
	 */
	private int lastDirection = -1;

	@Override
	public int getDirection(int id, PMGame game) {
		final Set<Integer> scoredDirections = this.scoreDirections(id, game);

		/**
		 * Végignézzük a négy irányt 
		 */
		for (Integer direction : scoredDirections) {
			PMGame cloned = game.clone();
			for (int j = 0; j < PMGame.TILES.SIZE / cloned.pacmans[id].getSpeed() + 1; j++) {
				cloned.setAction(cloned.pacmans[id], new PMAction(direction), 0);
			}
			/**
			 * Ha Pacman olyan csempére lépne, ahol meghal, akkor nem lép oda. 
			 */
			if (game.lives > cloned.lives) {
				continue;
			}

			/**
			 * Mentjük a legutolsó irányt azért, hogy súlyozhassuk 
			 * és kevésbé akarjon visszafordulni. 
			 */
			lastDirection = direction;
			return direction;
		}
		/**
		 * Ha ide elerunk, akkor nem tud elmenekülni a szellemektõl igy adunk egy iranyt neki felfelé, úgyis meghal. 
		 */
		return 0;
	}

	private Set<Integer> scoreDirections(int id, PMGame game) {
		/**
		 * Pacman jelenlegi pozicioja
		 */
		final Pair<Integer, Integer> position = game.pacmans[id].getTilePosition();

		/**
		 * Irányokhoz fogunk eltárolni score-okat. 
		 */
		List<Pair<Integer, Integer>> scorePairs = new ArrayList<>(4);

		/**
		 * Végignézzük a négy lehetséges irányt, és besúlyozzuk õket aszerint, hogy melyik irányban tudunk elérni több score-t.
		 */
		for (int direction = 0; direction < 4; direction++) {
			int score = 0;
			int steps = 0;

			/**
			 * Adott pozición állva, megnézzük mind a négy irányba a mezõket és score-t számolunk amíg falat nem érünk. 
			 */
			if (direction == DIRECTION.UP) {
				for (int i = position.first - 1; game.getTileAt(i, position.second) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					/**
					 * Súlyozzuk a lépéseket, enélkül megtudna állni ha egyformák a score-ok az irányokba. 
					 */
					steps++;				
				}
			}
			else if (direction == DIRECTION.DOWN) {
				for (int i = position.first + 1; game.getTileAt(i, position.second) != TILES.WALL; i++) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					steps++;
				}
			}
			else if (direction == DIRECTION.LEFT) {
				for (int i = position.second - 1; game.getTileAt(position.first, i) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(position.first, i), 0);
					steps++;
				}
			}
			else if (direction == DIRECTION.RIGHT) {
				for (int i = position.second + 1; game.getTileAt(position.first, i) != TILES.WALL; i++) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(position.first, i), 0);
					steps++;
				}
			}
			
			/**
			 * Számoljuk adott irányban a lépéseket azért, hogy ne álljunk meg falnál. 
			 */
			score += steps * 2;
			
			/**
			 * Értékeljük ha ugyanabba az irányba tartunk, mint amerre eddig. Ezzel 
			 * csökkentjük az oda vissza cikázásokat. A step-s != 0 pedig azért kell, mert így nem állunk meg üres folyosón. 
			 */
			if (direction == lastDirection && steps != 0) {
				score += 90;
			}
			scorePairs.add(new Pair<Integer, Integer>(direction,score));
		}
		/**
		 * 
		 */
		return new LinkedHashSet<>(scorePairs.stream()
				.sorted((p1, p2) -> p2.second - p1.second)
				.map(p -> p.first)
				.collect(Collectors.toList()));
	}
}