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
		 * V�gign�zz�k a n�gy ir�nyt 
		 */
		for (Integer direction : scoredDirections) {
			PMGame cloned = game.clone();
			for (int j = 0; j < PMGame.TILES.SIZE / cloned.pacmans[id].getSpeed() + 1; j++) {
				cloned.setAction(cloned.pacmans[id], new PMAction(direction), 0);
			}
			/**
			 * Ha Pacman olyan csemp�re l�pne, ahol meghal, akkor nem l�p oda. 
			 */
			if (game.lives > cloned.lives) {
				continue;
			}

			/**
			 * Mentj�k a legutols� ir�nyt az�rt, hogy s�lyozhassuk 
			 * �s kev�sb� akarjon visszafordulni. 
			 */
			lastDirection = direction;
			return direction;
		}
		/**
		 * Ha ide elerunk, akkor nem tud elmenek�lni a szellemekt�l igy adunk egy iranyt neki felfel�, �gyis meghal. 
		 */
		return 0;
	}

	private Set<Integer> scoreDirections(int id, PMGame game) {
		/**
		 * Pacman jelenlegi pozicioja
		 */
		final Pair<Integer, Integer> position = game.pacmans[id].getTilePosition();

		/**
		 * Ir�nyokhoz fogunk elt�rolni score-okat. 
		 */
		List<Pair<Integer, Integer>> scorePairs = new ArrayList<>(4);

		/**
		 * V�gign�zz�k a n�gy lehets�ges ir�nyt, �s bes�lyozzuk �ket aszerint, hogy melyik ir�nyban tudunk el�rni t�bb score-t.
		 */
		for (int direction = 0; direction < 4; direction++) {
			int score = 0;
			int steps = 0;

			/**
			 * Adott pozici�n �llva, megn�zz�k mind a n�gy ir�nyba a mez�ket �s score-t sz�molunk am�g falat nem �r�nk. 
			 */
			if (direction == DIRECTION.UP) {
				for (int i = position.first - 1; game.getTileAt(i, position.second) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					/**
					 * S�lyozzuk a l�p�seket, en�lk�l megtudna �llni ha egyform�k a score-ok az ir�nyokba. 
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
			 * Sz�moljuk adott ir�nyban a l�p�seket az�rt, hogy ne �lljunk meg faln�l. 
			 */
			score += steps * 2;
			
			/**
			 * �rt�kelj�k ha ugyanabba az ir�nyba tartunk, mint amerre eddig. Ezzel 
			 * cs�kkentj�k az oda vissza cik�z�sokat. A step-s != 0 pedig az�rt kell, mert �gy nem �llunk meg �res folyos�n. 
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