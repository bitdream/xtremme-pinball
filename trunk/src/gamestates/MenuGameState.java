package gamestates;

import input.FengJMEInputHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import main.Main;

import org.fenggui.Button;
import org.fenggui.ComboBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.Item;
import org.fenggui.Label;
import org.fenggui.Slider;
import org.fenggui.appearance.LabelAppearance;
import org.fenggui.appearance.TextAppearance;
import org.fenggui.binding.render.Binding;
import org.fenggui.binding.render.Font;
import org.fenggui.binding.render.ITexture;
import org.fenggui.binding.render.Pixmap;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.composite.Window;
import org.fenggui.decorator.background.PixmapBackground;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.layout.RowExLayout;
import org.fenggui.layout.RowLayout;
import org.fenggui.theme.DefaultTheme;
import org.fenggui.util.Alignment;
import org.fenggui.util.Spacing;
import org.fenggui.util.fonttoolkit.FontFactory;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.util.GameTaskQueueManager;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;
import com.jmex.game.state.BasicGameState;


public class MenuGameState extends BasicGameState
{
	/* Pantalla para FengGUI */
	private Display fengGUIdisplay;
	
	/* Input Handler de FengGUI, sirve para que FengGUI capture las teclas
	 * y las envie al display de JME para ser capturadas por los otros handlers */
	private FengJMEInputHandler fengGUIInputHandler;

	/* Botones del menu */
	private Button continueButton, newGameButton, exitButton, cancelButton, startButton;
	
	/* Musica del menu */
	private AudioTrack music;
	
	/* Settings del pinball */
	private PinballGameStateSettings settings;
	
	/* La ventana de los menues */
	private Window menu;

	/* Control de slider para la inclinacion */
	private Slider slider;
	
	/* Control de dropdown para la mesa elegida */
	private ComboBox tableList;

	/* Directorio donde se guardan los temas */
	private static final String THEMES_LIST = "themeslist";
	
	/* Indica si esta en el menu principal o no */
	private boolean inMainMenu;
	
	/* Contenedor general de FengGUI */
	private Container baseContainer;
	
	/* Path de la imagen de fondo del menu */
	private static final String BACKGROUND_IMAGE_PATH = "resources/textures/menu-background.jpg";
	private static PixmapBackground pmBckg = null;
	
	/* Tipografias usadas por los textos en Feng */
	private static final String fontName = "Helvetica";
	private static final int fontStyle = java.awt.Font.BOLD;
	private static final int fontSize = 20;
	
	
	public MenuGameState(String name)
	{
		super(name);
		
		/* Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/menu/music.wav"), false);
		music.setType(TrackType.MUSIC);
		music.setLooping(true);
		music.setTargetVolume(Main.getMusicVolume());
		
		/* Obtengo un display en Feng con LWJGL */
		fengGUIdisplay = new Display(new LWJGLBinding());
					
		/* Creo el contenedor general */
		baseContainer = new Container() {
			
			/* Pongo el cursor de manito para todo el menu, para evitar los cambios descentrados de cursor */
			public void mouseEntered(MouseEnteredEvent mouseEnteredEvent)
			{
				Binding.getInstance().getCursorFactory().getHandCursor().show();
			}
		};
		
		/* Defino el layout del contenedor general */
		baseContainer.setLayoutManager(new RowExLayout(false));
		
		/* Fijo el theme de Feng con la apariencia que deseo */
		FengGUI.setTheme(new DefaultTheme() {
			
			Font font = FontFactory.renderStandardFont(new java.awt.Font(fontName, fontStyle, fontSize));

			@Override
			public void setUp(Button arg0)
			{
				super.setUp(arg0);
				
				/* Textos en botones */
				arg0.getAppearance().getRenderer(TextAppearance.DEFAULTTEXTRENDERER).setFont(font);
			}
			
			@Override
			public void setUp(Label arg0)
			{
				super.setUp(arg0);
				
				/* Textos en labels */
				arg0.getAppearance().getRenderer(TextAppearance.DEFAULTTEXTRENDERER).setFont(font);
			}
		});
		
		/* Defino el tamanio al contenedor usando el de toda la pantalla de juego */
		baseContainer.setWidth(Main.getGameScreenWidth());
		baseContainer.setHeight(Main.getGameScreenHeight());
		
		/* Coloco el contenedor en la pantalla de Feng */
		fengGUIdisplay.addWidget(baseContainer);
		
		/* Hago la carga de la textura de background con una tarea en el thread de OpenGL */		
		GameTaskQueueManager.getManager().update(new Callable<Void>() {
			
	         public Void call() {
	        	 
	        	 ITexture backImg;
	        	 Binding bind = Binding.getInstance();
	             
	             /* Voy a usar el classloader para traer los recursos */
	             bind.setUseClassLoader(true);
	             
	             try
	             {
	            	 /* Si no lo tenia, lo traigo */
	                 if (pmBckg == null)
	                 {
	                	 /* La abro */
		            	 backImg = bind.getTexture(BACKGROUND_IMAGE_PATH);
		            	 
		            	 /* Creo un pixmap con ella */
		                 Pixmap pixMapBackImg = new Pixmap(backImg);
		                 
	                	 pmBckg = new PixmapBackground(pixMapBackImg, true);
	                 }
	                 
	                 /* La fijo como fondo */
	                 baseContainer.getAppearance().add(pmBckg);
	             }
	             catch (IOException e)
	             {
	             }
	             
	             return null;
	         }
		});
		
		/* Inicializo el input handler de FengGUI */
		fengGUIInputHandler = new FengJMEInputHandler(fengGUIdisplay);
		
		/* Pongo la accion de ESC durante todo el menu */
		fengGUIInputHandler.addAction( new InputAction() {

            public void performAction( InputActionEvent evt )
            {
            	if ( evt.getTriggerPressed() )
            	{
	            	if (inMainMenu)
	            	{
	            		if (continueButton.isVisible())
	            			/* Si estoy en menu principal y esta visible el boton de continue, continuo el juego */
	            			continueGame();
	            		else
	            			/* Salgo del juego si estoy en el principal y no hay juego para continuar */
	            			exitGame();
	            	}
	            	else
	            		/* Estoy en el menu de juego nuevo, lo cancelo */
	            		cancelNewGame();
            	}
            		
            }
        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE, InputHandler.AXIS_NONE, false );
		
		/* Pongo la accion de mute/unmute durante todo el menu */
		fengGUIInputHandler.addAction( new InputAction() {

            public void performAction( InputActionEvent evt )
            {
            	if ( evt.getTriggerPressed() )
            	{
	            	Main.toggleMuteAudio();
            	}
            }
        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_O, InputHandler.AXIS_NONE, false );
 	}

	/**
	 * Creo la ventana.
	 */
	private void buildWindow()
	{
		/* Remuevo todos los componentes que puedan haber en la pantalla */
		baseContainer.removeAllWidgets();

		/* Creo la ventana del menu */
		menu = new Window(false, false, false, true);
		
		/* La agrego al contenedor */
		baseContainer.addWidget(menu);
		
		/* Fijo sus propiedades */
		menu.setExpandable(false);
		menu.setResizable(false);
		menu.setShrinkable(false);
		menu.setMovable(false);
		menu.setTitle("");
		menu.setSize(300, 300);
		menu.getContentContainer().setLayoutManager(new RowLayout(false));
		menu.getContentContainer().getAppearance().setPadding(new Spacing(20, 15));
	}
	
	/**
	 * Inicializa el menu.
	 */
	private void buildMainMenu()
	{
		/* Construyo la ventana */
		buildWindow();
		
		/* Boton de continuar */
		continueButton = FengGUI.createButton(menu.getContentContainer(), "Continue");
		
		continueButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Continuo con el juego */
				continueGame();
			}
		});
		continueButton.setVisible(Main.hasInCourseGame());

		/* Boton de juego nuevo */
		newGameButton = FengGUI.createButton(menu.getContentContainer(), "New game");
		
		newGameButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Cierro la ventana */
				menu.close();
				
				/* Creo la pantalla de opciones de juego */
				buildGameOptionsMenu();
			}
		});
		
		/* Boton de salir */
		exitButton = FengGUI.createButton(menu.getContentContainer(), "Exit");
		
		exitButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0)
			{
				/* Salgo del juego */
				exitGame();
			}
		});
		
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
		
		/* Indico que estoy en main menu */
		inMainMenu = true;
	}
	
	/**
	 * Inicializa el menu de opciones para un juego.
	 */
	private void buildGameOptionsMenu()
	{
		/* Construyo la ventana */
		buildWindow();
		
		/* Creo las configuraciones del pinball que generaria */
		settings = new PinballGameStateSettings();
		
		/* Titulo de elegir mesa */
		FengGUI.createLabel(menu.getContentContainer(), "Select game table");
	    
		/* Creo el dropdown con las mesas para seleccionar */
		tableList = FengGUI.createComboBox(menu.getContentContainer());
		populateTableList(tableList);
		
	    /* Titulo de elegir inclinacion */
	    FengGUI.createLabel(menu.getContentContainer(), "Select inclination level");
		
		/* Creo el slider con los niveles de inclinacion posibles */
	    slider = FengGUI.createSlider(menu.getContentContainer(), true);
		slider.updateMinSize();
		slider.setValue(0.5);
		slider.setClickJump(0.1);
		
		/* Estado del slider */
	    final Label labelInclinationStatus = FengGUI.createLabel(menu.getContentContainer(), getLevelMessage(getInclinationLevel(slider.getValue())));
	    labelInclinationStatus.getAppearance().setAlignment(Alignment.MIDDLE);
		
		slider.addSliderMovedListener(new ISliderMovedListener()
		{
			public void sliderMoved(SliderMovedEvent sliderMovedEvent)
			{
				labelInclinationStatus.setText(getLevelMessage(getInclinationLevel(sliderMovedEvent.getPosition())));
			}
		});

		/* Boton de comenzar juego */
		startButton = FengGUI.createButton(menu.getContentContainer(), "Start");
		
		startButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Cierro la ventana */
				menu.close();
				
				/* Destruyo el gamestate de menu */
				Main.endMenu();

				/* Mato el juego actual si hay alguno */
				Main.endCurrentPinballGame();
				
				/* Creo un loading nuevo y lo inicio */
				settings.setInclinationLevel(getInclinationLevel(slider.getValue()));
				
				/* Creo el gamestate de loading con la configuracion del pinball a crear y el recurso de su mesa */
				LoadingGameState lgs = Main.newLoading(settings, 
				    (((Theme)tableList.getList().getSelectedItem().getUserData()).getResource()),
				    (((Theme)tableList.getList().getSelectedItem().getUserData()).getTextureResource()));
				lgs.setActive(true);
			}
		});
		
		/* Boton de cancelar */
		cancelButton = FengGUI.createButton(menu.getContentContainer(), "Cancel");
		
		cancelButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Cancelo el juego nuevo */
				cancelNewGame();
			}
		});
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
		
		/* Indico que ya no estoy en main menu */
		inMainMenu = false;
	}
	
	private void continueGame()
	{
		/* Cierro la ventana */
		menu.close();
		
		/* Desactivo el gamestate de menu */
		Main.endMenu();
		
		/* Continuo el juego actual */
		Main.continueCurrentPinballGame();
	}
	
	private void exitGame()
	{
		/* Acabo con todo el juego */
		Main.shutdownGame();
	}
	
	private void cancelNewGame()
	{
		/* Cierro la ventana */
		menu.close();
		
		/* Vuelvo a construir el menu principal */
		buildMainMenu();
	}

	@Override
	public void setActive(boolean active)
	{
		super.setActive(active);

		if (active)
		{
			/* Hago visible al cursor */
			MouseInput.get().setCursorVisible(true);
			
			/* Inicializo el menu */
			buildMainMenu();
			
			/* Si hay juegos en transcurso, muestro el boton de continue */
			continueButton.setVisible(Main.hasInCourseGame());

			/* Inicio su musica */
			Main.getAudioSystem().getMusicQueue().addTrack(music);
			Main.getAudioSystem().getMusicQueue().setCurrentTrack(music);
			
			/* Si era la primera instancia del menu todavia no estaba en ejecucion la musica */
			if (!Main.getAudioSystem().getMusicQueue().isPlaying())
				Main.getAudioSystem().getMusicQueue().play();
		}
		else
		{
			/* Oculto el cursor */
			MouseInput.get().setCursorVisible(false);
			
			/* Remuevo todo lo que esta en el contenedor */
			fengGUIdisplay.removeAllWidgets();
		}
	}
	
	@Override
	public void update(float tpf)
	{
		super.update(tpf);
		
		/* Actualizo el controlador de input */
        fengGUIInputHandler.update(tpf);
        
        /* Actualizo el sistema de sonido */
        Main.getAudioSystem().update();
	}
	
	@Override
	public void render(float tpf)
	{
		super.render(tpf);

		/* Muestro la pantalla de FengGUI */
		fengGUIdisplay.display();
	}
	
	private static int getInclinationLevel(double position)
	{
		return (position == 0)? 1 : (int) Math.ceil(position * 10);
	}
	
	private static String getLevelMessage(int level)
	{
		String message = "Level " + level + " - ";
		String difficulty;
		
		if (level == 1)
			difficulty = "Beginner";
		else if (level < 5)
			difficulty =  "Easy";
		else if (level == 5)
			difficulty = "Normal";
		else if (level < 10)
			difficulty = "Hard";
		else
			difficulty = "Nightmare";
		
		return message + difficulty;
	}
	
	private void populateTableList(ComboBox list)
	{
		URI themeListURI;
		List<String> themes;
		
	    try 
	    {
	        themeListURI = MenuGameState.class.getClassLoader().getResource(THEMES_LIST).toURI();
	    } catch ( URISyntaxException e )
	    {
	        System.out.println("Error que no deberia pasar");
	        themeListURI = null;
	    }
		
	    File themesList = new File(themeListURI);
		
	    try
	    {
	        themes = parseThemes( themesList );
	    }
	    catch (IOException e)
	    {
	        System.err.println("Invalid themes list");
	        e.printStackTrace();
	        return;
	    }
	    
	    for ( String themeName : themes )
        {
	        URI tablesURI;
	        
	        String basePath = themeName+"/models/";
	        
	        try 
	        {
	            tablesURI = MenuGameState.class.getClassLoader().getResource(basePath).toURI();
	        } catch ( URISyntaxException e )
	        {
	            System.err.println("Malformed theme name");
	            e.printStackTrace();
	            return;
	        }
	        
	        File tablesDir = new File(tablesURI);
	        
	        /* Obtengo todos los archivos en el directorio de themes */
	        File[] files = tablesDir.listFiles();
	        
	        /* Voy a buscar aquellos archivos que sean x3d */
	        String tableFilename;
	        Theme theme;
	        Pattern x3dFiles = Pattern.compile(".*\\.x3d", Pattern.CASE_INSENSITIVE);

	        for (int i = 0; i < files.length; i++)
	        {
	            /* Obtengo el nombre de archivo del theme */
	            tableFilename = files[i].getName();
	        
	            /* Si corresponde a un archivo x3d lo incorporo al menu */
	            if (x3dFiles.matcher(tableFilename).find())
	            {
	                /* Creo el theme obteniendo su nombre del nombre de archivo y formando su recurso */
	                theme = new Theme(tableFilename.substring(0, tableFilename.length() - 4),
	                        MenuGameState.class.getClassLoader().getResource(basePath + tableFilename),
	                        MenuGameState.class.getClassLoader().getResource(themeName + "/textures/"));
	                
	                /* Lo agrego a la lista */
	                Item item = new Item(theme.getName(), new LabelAppearance(new Label(theme.getName())));
	                item.setData(theme);
	                list.addItem(item);
	            }
	        }
        }
	    
	}
	
    private static List<String> parseThemes( File themesList ) throws IOException
    {
        List<String> ret = new ArrayList<String>();
        
        LineNumberReader lnr = new LineNumberReader(new FileReader(themesList));
        
        String line;
        while ((line = lnr.readLine()) != null)
        {
            if (!line.startsWith( "#" ))
                ret.add( line );
        }
        
        
        System.out.println(ret);
        return ret;
    }
	
	private class Theme
	{
		private String name;
		
		private URL resource, textureResource;
		
		public Theme(String name, URL resource, URL texureResource)
		{
			this.name = name;
			this.resource = resource;
			this.textureResource = texureResource;
			System.out.println(name);
			System.out.println(resource);
			System.out.println(texureResource);
		}

		public String getName()
		{
			return name;
		}

		public URL getResource()
		{
			return resource;
		}
		
		public URL getTextureResource()
		{
		    return textureResource;
		}
	}
}
