import game.pm.strategies.Strategy;
import game.pm.PMAction;
import game.pm.PMGame;
import game.pm.PMGame.TILES;
import game.pm.PMPlayer;

public class Agent extends Strategy {
	public int faultCounter = 0;

	@Override
	public int getDirection(int id, PMGame game) {
		int currentDirection = game.pacmans[id].getDirection();
		int maxScore = -1;
		int maxDirection = -1;
		for (int direction = 0; direction < 4; direction++) {
			PMGame cloned = game.clone();
			for (int j = 0; j < PMGame.TILES.SIZE / cloned.pacmans[id].getSpeed() + 1; j++) {
				cloned.setAction(cloned.pacmans[id], new PMAction(direction), 0);
			}
			

			
			if (maxScore < cloned.score) {
				maxScore = cloned.score;
				maxDirection = direction;
			}
			
		}
		
		if(!game.pacmans[0].canTurnAndMove(currentDirection) && maxDirection == 0) {
			return random.nextInt(4);
		}

		return maxDirection;

	}

}