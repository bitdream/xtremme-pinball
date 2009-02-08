package main;

import gamestates.LoadingGameState;
import gamestates.MenuGameState;
import gamestates.PinballGameState;
import gamestates.PinballGameStateSettings;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.system.GameSettings;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.MusicTrackQueue.RepeatType;
import com.jmex.game.StandardGame;
import com.jmex.game.StandardGame.GameType;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;


public class Main
{
	
    private static StandardGame stdGame;
	
	private static AudioSystem audio;
	
	private static float musicVolume = 0.55f;
	
	private static LoadingGameState loadingGS = null;

	/**
	 * Punto de entrada al juego
	 * @throws InterruptedException 
	 */
	public static void main(String[] args)
	{
	    if ( args.length == 0 )
	    {
	        System.setProperty("jme.debug", "FALSE");
	    }
	    else
	    {
	        if (args[0].equals( "debug" ))
	            System.setProperty( "jme.stats", "set" );
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
		stdGame = new StandardGame("xtremme-pinball", GameType.GRAPHICAL, getNewSettings( "xtremme-pinball" ));
       
        /* Para multithreading */
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
		loadingGS = new LoadingGameState(settings, tableResource);
		
		/* Lo agrego al GameStateManager */
		GameStateManager.getInstance().attachChild(loadingGS);
		loadingGS.setActive(false);
		
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
		/* Debe estar en el manager de gamestates y tener carga de mesa completa */
		PinballGameState pinballGS = (PinballGameState) GameStateManager.getInstance().getChild("Game");
		
		return pinballGS != null && pinballGS.isLoadingComplete();
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
		if (loadingGS != null)
		{
		    // esto lo hace el fade
			//loadingGS.setActive(false);
			loadingGS.cleanup();
			GameStateManager.getInstance().detachChild(loadingGS);
		}
	}
	
	public static void shutdownGame()
	{
		/* Termino con el sistema de sonido TODO no anda? */
		getAudioSystem().fadeOutAndClear(2);
		
//		if (AudioSystem.isCreated())
//            AudioSystem.getSystem().cleanup();
		
		/* Termino con el juego */
		stdGame.shutdown();
	}

	public static AudioSystem getAudioSystem()
	{
		return audio;
	}
	
	// TODO Utilizar para cargar las bibliotecas nativas desde el codigo (en vez de desde Eclipse)
	private static final void loadNativeLibraries()
	{
        String os = System.getProperty( "os.name" );
        
        if (os.matches( "Linux.*" ))
            os = "linux";
        else if (os.matches( "Mac.*" ))
            os = "macosx";
        else if (os.matches( "Windows.*"))
            os = "win32";
        else
            throw new RuntimeException("Unsupported system");
	    
        try
        {
            String odeLibDir = "lib/ode/native/";
            String lwjglLibDir = "lib/lwjgl/native/";
            
        	addDir(odeLibDir + os);
        	addDir(lwjglLibDir + os );
        	
        } catch (IOException e)
        {
        	System.err.println("Unable to load dynamic libraries.");
        	throw new RuntimeException(e);
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
	
	public static void toggleMuteAudio()
	{
		if (!audio.isMuted())
			audio.mute();
		else
			audio.unmute();
	}
	
	public static int getGameScreenWidth()
	{
		return stdGame.getSettings().getWidth();
	}
	
	public static int getGameScreenHeight()
	{
		return stdGame.getSettings().getHeight();
	}
	
    private static GameSettings getNewSettings( String gameName ) 
    {
        //XXX NOTA GIGANTE: ojo al editar la case de las pref,
        //que fue autogenerada por el visual editor
        XtremmePreferences prefs = new XtremmePreferences();

        try
        {
//            System.out.println("waiting");
            synchronized ( prefs )
            {
                prefs.wait();    
            }
//            System.out.println("notified");
        }
        catch (InterruptedException e)
        {
            
        }
        return prefs;

    }
}
