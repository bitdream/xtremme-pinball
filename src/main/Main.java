package main;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import gamestates.LoadingGameState;
import gamestates.MenuGameState;
import gamestates.PinballGameState;
import gamestates.PinballGameStateSettings;

import com.jme.app.AbstractGame.ConfigShowMode;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.MusicTrackQueue.RepeatType;
import com.jmex.editors.swing.settings.GameSettingsPanel;
import com.jmex.game.StandardGame;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;
import com.jme.app.AbstractGame;

public class Main
{
	
	private static StandardGame stdGame;
	
	private static AudioSystem audio;
	
	private static float musicVolume = 0.55f;

	/**
	 * Punto de entrada al juego
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException
	{
	    if ( true /*args[0].equals( "debug" )*/ ) // TODO y esto? -> para desactivar las estadisticas y que vaya mas rapido
	    {
	        System.setProperty( "jme.stats", "set" );
	    }
	    else
	    {
	        System.setProperty("jme.debug", "FALSE");
	    }
		/* Logueos severos desde el root logger */
		Logger.getLogger("").setLevel(Level.SEVERE);

		/* TODO Carga de bibliotecas nativas */
		//loadNativeLibraries();
		
		/* Preparo el sistema de sonido */
		audio = AudioSystem.getSystem();
		
		/* Sin fade in */
		audio.getMusicQueue().setCrossfadeinTime(0);
		
		/* Con fade out */
		audio.getMusicQueue().setCrossfadeoutTime(2f);
		
		/* No quiero que las pistas se repitan por default */
		audio.getMusicQueue().setRepeatType(RepeatType.ONE);
	
		/* Inicializacion del juego principal */
		stdGame = new StandardGame("xtremme-pinball");
		
		// (y se guarda en $HOME/.java/main/.userPrefs en linux y en windows en el registro)
		// TODO si se pone algo diferente a AlwaysShow, igual simpre lo muestra, el prompt deberia ser condicional segun se tenga o no 
		// el archivo de config, pero hay que hacerlo por codigo, jme ya lo deberia hacer...
		stdGame.setConfigShowMode(ConfigShowMode.AlwaysShow);

        // show the GameSettingsPanel
        if (!GameSettingsPanel.prompt(stdGame.getSettings()))
        {
            // user pressed Cancel
            return;
        }        
        
		gamestates.PhysicsEnhancedGameState.game = stdGame;
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
	
	public static LoadingGameState newLoading(PinballGameStateSettings settings, URL tableResource)
	{
		/* Creo un nuevo loading screen */
		LoadingGameState loadingGS = new LoadingGameState("Loading", settings, tableResource);
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(loadingGS);
		loadingGS.setActive(false);
		
		loadingGS.getRootNode().updateRenderState();
		
		return loadingGS;
	}
	
	public static PinballGameState newPinballGame(PinballGameStateSettings settings)
	{
		/* Les guardo los valores recogidos de la ventana de configuraciones */
		settings.setWidth(stdGame.getSettings().getWidth());
		settings.setHeight(stdGame.getSettings().getHeight());
		settings.setDepth(stdGame.getSettings().getDepth());
		settings.setFreq(stdGame.getSettings().getFrequency());
		settings.setFullscreen(stdGame.getSettings().isFullscreen());
		
		/* Creo un nuevo Pinball */
		PinballGameState pinballGS = new PinballGameState("Game", settings);
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(pinballGS);
		
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
			gs.cleanup();
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
		getAudioSystem().fadeOutAndClear(2);
		
//		if (AudioSystem.isCreated())
//            AudioSystem.getSystem().cleanup();
	}

	public static AudioSystem getAudioSystem()
	{
		return audio;
	}
	
	// TODO Utilizar para cargar las bibliotecas nativas desde el codigo (en vez de desde Eclipse)
	private static final void loadNativeLibraries()
	{
        try
        {
        	addDir("lib/ode/native");
        } catch (IOException e)
        {
        	System.err.println("No se pudo cargar las bibliotecas dinamicas.");
        	e.printStackTrace(System.err);
        }
	}

	private static void addDir(String s) throws IOException
	{
	    try
	    {
	    	Field field = ClassLoader.class.getDeclaredField("usr_paths");
	    	
	        field.setAccessible(true);
	        
	        String[] paths = (String[])field.get(null);
	        
	        for (int i = 0; i < paths.length; i++)
	        {
	        	if (s.equals(paths[i]))
	        	{
	        		return;
	        	}
	        }
	        
			String[] tmp = new String[paths.length+1];
			
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = s;
			
			field.set(null,tmp);
			
		} catch (IllegalAccessException e)
		{
			throw new IOException("Se produjo un fallo al obtener los permisos para definir el library path.");
		} catch (NoSuchFieldException e)
		{
			throw new IOException("Se produjo un fallo al obtener el manejador del campo para definir el library path.");
		}
    }

	public static float getMusicVolume()
	{
		return musicVolume;
	}
}
