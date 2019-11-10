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
	 * Letaroljuk a terkepen levo dolgokat egy altalunk definialt pontrendszer alapjan azert,
	 * hogy ezek alapjan szamoljuk majd ki, hogy merre eri meg a legjobban menni. 
	 */
	private final static Map<Integer, Integer> TILE_SCORE;
	static {
		TILE_SCORE = new HashMap<>();
		TILE_SCORE.put(TILES.EMPTY, -1);
		TILE_SCORE.put(TILES.FOOD, 100);
		TILE_SCORE.put(TILES.FRIGHT_BLINKY, 410);
		TILE_SCORE.put(TILES.FRIGHT_CLYDE, 410);
		TILE_SCORE.put(TILES.FRIGHT_INKY, 410);
		TILE_SCORE.put(TILES.FRIGHT_PINKY, 410);
		TILE_SCORE.put(TILES.ENERGIZER, 20);
		TILE_SCORE.put(TILES.BLINKY, -10);
		TILE_SCORE.put(TILES.CLYDE, -10);
		TILE_SCORE.put(TILES.INKY, -10);
		TILE_SCORE.put(TILES.PINKY, -10);
	}
	/**
	 * Sulyozzuk vele azt az iranyt ahonnan jottunk, hogy ne forogjon oda vissza egyhelyben. 
	 */
	private int lastDirection = -1;

	@Override
	public int getDirection(int id, PMGame game) {
		final Set<Integer> scoredDirections = this.scoreDirections(id, game);

		/**
		 * Vegigiteralunk a bescoreozott utvonalakon, 
		 * es az elso olyan legtobb pontot ero fele fogunk menni ahol nem halna meg pacman.
		 */
		for (Integer direction : scoredDirections) {
			PMGame cloned = game.clone();
			for (int j = 0; j < PMGame.TILES.SIZE / cloned.pacmans[id].getSpeed() + 1; j++) {
				cloned.setAction(cloned.pacmans[id], new PMAction(direction), 0);
			}
			/**
			 * Ha Pacman oda lepne ahol meghal, akkor nem lep oda. 
			 */
			if (game.lives > cloned.lives) {
				continue;
			}
			lastDirection = direction;
			return direction;
		}
		/**
		 * Ha ide elerunk, akkor nem tud elmenekülni a szellemektõl igy adunk egy iranyt neki felfelé, hisz ugyis meghal. 
		 */
		return 0;
	}

	/**
	 * Besulyozzuk az iranyokat aszerint, hogy egyes arra levo kajak stb mennyit ernek.
	 * (Ezt taroltuk le fent egy HashMapben.) 
	 * @param id
	 * @param game
	 * @return
	 */
	private Set<Integer> scoreDirections(int id, PMGame game) {
		final Pair<Integer, Integer> position = game.pacmans[id].getTilePosition();
		List<Pair<Integer, Integer>> scorePairs = new ArrayList<>(4);

		for (int direction = 0; direction < 4; direction++) {
			int score = 0;
			int steps = 0;

			/**
			 * Jelenlegi poziciotol elnezunk a falakig a negy iranyba
			 * es osszeszamoljuk hany pontot erhetnenk el addig a mi 
			 * ertekelesunk alapjan. 
			 */
			switch (direction) {
			case DIRECTION.UP:
				for (int i = position.first - 1; game.getTileAt(i, position.second) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					steps++;				
				}
				break;
			case DIRECTION.DOWN:
				for (int i = position.first + 1; game.getTileAt(i, position.second) != TILES.WALL; i++) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(i, position.second), 0);
					steps++;
				}
				break;
			case DIRECTION.LEFT:
				for (int i = position.second - 1; game.getTileAt(position.first, i) != TILES.WALL; i--) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(position.first, i), 0);
					steps++;
				}
				break;
			case DIRECTION.RIGHT:
				for (int i = position.second + 1; game.getTileAt(position.first, i) != TILES.WALL; i++) {
					score += TILE_SCORE.getOrDefault(game.getTileAt(position.first, i), 0);
					steps++;
				}
				break;
			default:
				break;
			}
			
			/**
			 * Ertekeljuk ha ugyanabba az iranyba tartunk, mint amerre  eddig. Ezzel 
			 * csökkentjük az oda vissza cikazasokat. A step-s !=0 pedig azert kell, mert igy nem allunk meg üres folyoson. 
			 */
			score += steps * 2;
			if (direction == lastDirection && steps != 0) {
				score += 90;
			}
			scorePairs.add(new Pair<Integer, Integer>(direction,score));
		}
		/**
		 * Csokkenosorban adjuk vissza az utvonalakat score szerint azert, 
		 * hogy a legjobban megero fele menjunk ha lehet. 
		 */
		return new LinkedHashSet<>(scorePairs.stream()
				.sorted((p1, p2) -> p2.second - p1.second)
				.map(p -> p.first)
				.collect(Collectors.toList()));
	}
}