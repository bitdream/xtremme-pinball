package main;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import gamestates.LoadingGameState;
import gamestates.MenuGameState;
import gamestates.PinballGameState;
import gamestates.PinballGameStateSettings;

import com.jme.app.AbstractGame.ConfigShowMode;
import com.jmex.audio.AudioSystem;
import com.jmex.game.StandardGame;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;


public class Main
{
	
	private static StandardGame stdGame;
	
	private static AudioSystem audio;
	

	/**
	 * Punto de entrada al juego
	 */
	public static void main(String[] args)
	{
	    if ( true /*args[0].equals( "debug" )*/ )
	    {
	        System.setProperty( "jme.stats", "set" );
	    }
	    else
	    {
	        System.setProperty("jme.debug", "FALSE");
	    }
		/* Logueos severos desde el root logger */
		Logger.getLogger("").setLevel(Level.SEVERE);
		
		/* Preparo el sistema de sonido */
		audio = AudioSystem.getSystem();
		
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
	
	public static LoadingGameState newLoading(float inclinationAngle, URL tableResource)
	{
		/* Creo un nuevo loading screen */
		LoadingGameState loadingGS = new LoadingGameState("Loading", inclinationAngle, tableResource);
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(loadingGS);
		loadingGS.setActive(false);
		
		loadingGS.getRootNode().updateRenderState();
		
		return loadingGS;
	}
	
	public static PinballGameState newPinballGame(float inclinationAngle)
	{
		/* Creo las configuraciones */
		PinballGameStateSettings pinballSettings = new PinballGameStateSettings();
		
		// TODO ver que hacer con los settings, probablemente vuelen de aca (deberian ser directamente gameSettings y ser globales...)
		
		/* Les guardo los valores recogidos de la ventana de configuraciones */
		pinballSettings.setWidth(stdGame.getSettings().getWidth());
		pinballSettings.setHeight(stdGame.getSettings().getHeight());
		pinballSettings.setDepth(stdGame.getSettings().getDepth());
		pinballSettings.setFreq(stdGame.getSettings().getFrequency());
		pinballSettings.setFullscreen(stdGame.getSettings().isFullscreen());
		pinballSettings.setRenderer(stdGame.getSettings().getRenderer());
		
		pinballSettings.setInclinationAngle(inclinationAngle);
		
		/* Creo un nuevo Pinball */
		PinballGameState pinballGS = new PinballGameState("Game", pinballSettings);
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(pinballGS);
		pinballGS.setActive(false);
		
		pinballGS.getRootNode().updateRenderState();
		
		return pinballGS;
	}
	
	public static boolean hasInCourseGame()
	{
		return GameStateManager.getInstance().getChild("Game") != null;
	}
	
	public static void pauseCurrentPinballGame()
	{
		if (GameStateManager.getInstance().getChild("Game") != null)
			GameStateManager.getInstance().getChild("Game").setActive(false);
	}
	
	public static void endCurrentPinballGame()
	{
		GameState gs = GameStateManager.getInstance().getChild("Game");
		
		if (gs != null)
		{
			gs.setActive(false);
			GameStateManager.getInstance().detachChild(gs);
		}
	}
	
	public static void continueCurrentPinballGame()
	{
		if (GameStateManager.getInstance().getChild("Game") != null)
			GameStateManager.getInstance().activateChildNamed("Game");
	}
	
	public static void endMenu()
	{
		GameState gs = GameStateManager.getInstance().getChild("Menu");
		
		if (gs != null)
		{
			gs.setActive(false);
			GameStateManager.getInstance().detachChild(gs);
		}
	}

	public static void endLoading()
	{
		GameState gs = GameStateManager.getInstance().getChild("Loading");
		
		if (gs != null)
		{
			gs.setActive(false);
			GameStateManager.getInstance().detachChild("Loading");
		}
	}
	
	public static void shutdownGame()
	{
		/* Termino con el juego */
		stdGame.shutdown();
		
		/* Termino con el sistema de sonido TODO no anda? */
		getAudioSystem().fadeOutAndClear(2f);
	}

	public static AudioSystem getAudioSystem()
	{
		return audio;
	}
}
