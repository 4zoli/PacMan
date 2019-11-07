import game.pm.strategies.Strategy;
import javafx.geometry.Pos;

import javax.xml.ws.FaultAction;

import game.engine.utils.Pair;
import game.pm.PMAction;
import game.pm.PMGame;

public class Agent extends Strategy {
	public int whereToGo = 0;

	@Override
	public int getDirection(int id, PMGame game) {
		int currentDirection = game.pacmans[id].getDirection();

		if (!game.pacmans[id].canMoveTo(currentDirection)) {
			for(int direction = 0; direction < 4; direction++)	{
				if(game.pacmans[id].canTurnAndMove(direction)) {
					Pair<Integer, Integer> pacmansTilePosition = game.pacmans[id].getTilePosition();
					
					int i = game.getTileAt(pacmansTilePosition.first, pacmansTilePosition.second);
						
					
					return direction;
				}
			}
		}
		return currentDirection;

	}
}
