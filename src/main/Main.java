package main;

import gamestates.MenuGameState;
import mainloop.PinballGameState;
import mainloop.PinballGameStateSettings;

import com.jme.app.AbstractGame.ConfigShowMode;
import com.jmex.game.StandardGame;
import com.jmex.game.state.GameStateManager;

public class Main
{
	
	private static StandardGame stdGame;

	/**
	 * Punto de entrada al juego
	 */
	public static void main(String[] args)
	{
		
		stdGame = new StandardGame("xtremme-pinball");
		
		// TODO cablear que siempre use LWJGL, y ver porque solo aparece 1 vez (y se guarda en $HOME/.java/main/.userPrefs en linux y en windows en el registro)
		stdGame.setConfigShowMode(ConfigShowMode.AlwaysShow);
		
		/* Doy comienzo al juego */
		stdGame.start();
		
		/* Creo e inicio un nuevo menu */
		newMenu().setActive(true);
	}
	
	public static MenuGameState newMenu()
	{
		/* Creo un nuevo Menu */
		MenuGameState menuGS = new MenuGameState("Menu");
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(menuGS);
		
		return menuGS;
	}
	
	public static PinballGameState newPinballGame()
	{
		/* Creo las configuraciones */
		PinballGameStateSettings pinballSettings = new PinballGameStateSettings();
		
		// TODO ver que hacer con los settings, probablemente vuelen de aca
		
		/* Les guardo los valores recogidos de la ventana de configuraciones */
		pinballSettings.setWidth(stdGame.getSettings().getWidth());
		pinballSettings.setHeight(stdGame.getSettings().getHeight());
		pinballSettings.setDepth(stdGame.getSettings().getDepth());
		pinballSettings.setFreq(stdGame.getSettings().getFrequency());
		pinballSettings.setFullscreen(stdGame.getSettings().isFullscreen());
		pinballSettings.setRenderer(stdGame.getSettings().getRenderer());
		
		/* Creo un nuevo Pinball */
		PinballGameState pinballGS = new PinballGameState("Game", pinballSettings);
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(pinballGS);
		pinballGS.setActive(false);
		
		pinballGS.getRootNode().updateRenderState();
		
		return pinballGS;
	}
	
	public static void pauseCurrentPinballGame()
	{
		if (GameStateManager.getInstance().getChild("Game") != null)
			GameStateManager.getInstance().getChild("Game").setActive(false);
	}
	
	public static void endCurrentPinballGame()
	{
		if (GameStateManager.getInstance().getChild("Game") != null)
			GameStateManager.getInstance().detachChild("Game");
	}
	
	public static void deactivateMenu()
	{
		GameStateManager.getInstance().getChild("Menu").setActive(false);
	}
	
	public static void activateMenu()
	{
		GameStateManager.getInstance().getChild("Menu").setActive(true);
	}
	
	public static void shutdownGame()
	{
		stdGame.shutdown();
	}
}
