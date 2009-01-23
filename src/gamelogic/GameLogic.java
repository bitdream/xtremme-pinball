package gamelogic;

import components.Bumper;
import components.Door;
import components.Flipper;
import components.Plunger;

import mainloop.Pinball;

public abstract class GameLogic
{
	protected int score;
	
	protected Pinball pinball;
	
	public GameLogic(Pinball pinball)
	{
		this.pinball = pinball;
	}
	
	public abstract void bumperCollision(Bumper bumper);
	
	public abstract void doorCollision(Door door);
	
	public abstract void flipperCollision(Flipper flipper);
	
	public abstract void plungerCollision(Plunger plunger);
	
	public void showScore()
	{
		pinball.setScore(score);
	}
	
	public void showMessage(String message)
	{
		pinball.setMessage(message);
	}
}
