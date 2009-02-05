package gamestates;

import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

import input.FengJMEInputHandler;

import main.Main;

import org.fenggui.Button;
import org.fenggui.ComboBox;
import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.Label;
import org.fenggui.ListItem;
import org.fenggui.Slider;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.decorator.background.PlainBackground;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;
import org.lwjgl.opengl.GL13;

import com.jme.input.MouseInput;
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
	
	/* El contenedor general de los menues */
	private Container c;

	/* Control de slider para la inclinacion */
	private Slider slider;
	
	/* Control de dropdown para la mesa elegida */
	private ComboBox<Theme> tableList;

	/* Directorio donde se guardan los temas */
	private static final String THEMES_DIRECTORY = "resources/models/themes/";

	
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
		
		/* Inicializo el input handler de FengGUI */
		fengGUIInputHandler = new FengJMEInputHandler(fengGUIdisplay);
 
		/* Creo el contenedor general del menu */
		c = new Container();
		c.getAppearance().add(new PlainBackground(Color.BLUE));
		fengGUIdisplay.addWidget(c);
		c.getAppearance().setPadding(new Spacing(10, 10));
		c.setLayoutManager(new RowLayout(false));
		
		/* Inicializo los botones de los menues */
		initButtons();
	}

	/**
	 * Inicializa el menu.
	 */
	private void buildMainMenu()
	{
		//TODO darle algo de estilo al menu y agregarle un fondo
		
		/* Remuevo todo lo que esta en el contenedor */
		c.removeAllWidgets();
		
		/* Coloco los botones */
		c.addWidget(continueButton);
		c.addWidget(newGameButton);
		c.addWidget(exitButton);
	
		c.pack();
		
		StaticLayout.center(c, fengGUIdisplay);
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
	}
	
	/**
	 * Inicializa el menu de opciones para un juego.
	 */
	private void buildGameOptionsMenu()
	{
		/* Remuevo todo lo que esta en el contenedor */
		c.removeAllWidgets();
		
		/* Creo las configuraciones del pinball que generaria */
		settings = new PinballGameStateSettings();
		
		/* Titulo de elegir mesa */
	    Label labelTable = FengGUI.createLabel(c, "Select game table");
		//labelInclination.getAppearance().setAlignment(Alignment.MIDDLE);
	    
		/* Creo el dropdown con las mesas para seleccionar */
		tableList = FengGUI.createComboBox(c);
		populateTableList(tableList);
		
	    /* Titulo de elegir inclinacion */
	    Label labelInclination = FengGUI.createLabel(c, "Select inclination level");
		//labelInclination.getAppearance().setAlignment(Alignment.MIDDLE);
		
		/* Creo el slider con los niveles de inclinacion posibles */
	    slider = FengGUI.createSlider(c, true);
		slider.updateMinSize();
		slider.setValue(0.5);
		slider.setClickJump(0.1);
		
		/* Estado del slider */
	    final Label labelInclinationStatus = FengGUI.createLabel(c, getLevelMessage(getInclinationLevel(slider.getValue())));
	    //labelInclination.getAppearance().setAlignment(Alignment.MIDDLE);
		
		slider.addSliderMovedListener(new ISliderMovedListener()
		{
			public void sliderMoved(SliderMovedEvent sliderMovedEvent)
			{
				labelInclinationStatus.setText(getLevelMessage(getInclinationLevel(sliderMovedEvent.getPosition())));
			}
		});
		
		/* Coloco los botones */
		c.addWidget(startButton);
		c.addWidget(cancelButton);
	
		c.pack();
		
		StaticLayout.center(c, fengGUIdisplay);
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
	}
	
	private void initButtons()
	{
		/* Boton de continuar */
		continueButton = FengGUI.createButton(c, "Continue");
		
		continueButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Continuo el juego actual */
				Main.continueCurrentPinballGame();
				
				/* Desactivo el gamestate de menu */
				Main.endMenu();

			}
		});

		/* Boton de juego nuevo */
		newGameButton = FengGUI.createButton(c, "New game");
		
		newGameButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Creo la pantalla de opciones de juego */
				buildGameOptionsMenu();
			}
		});
		
		/* Boton de salir */
		exitButton = FengGUI.createButton(c, "Exit");
		
		exitButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0)
			{
				/* Acabo con todo el juego */
				Main.shutdownGame();
			}
		});
		
		/* Boton de cancelar */
		cancelButton = FengGUI.createButton(c, "Cancel");
		
		cancelButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Vuelvo a construir el menu principal */
				buildMainMenu();
			}
		});
		
		/* Boton de comenzar juego */
		startButton = FengGUI.createButton(c, "Start");
		
		startButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Destruyo el gamestate de menu */
				Main.endMenu();

				/* Mato el juego actual si hay alguno */
				Main.endCurrentPinballGame();
				
				/* Creo un loading nuevo y lo inicio */
				settings.setInclinationLevel(getInclinationLevel(slider.getValue()));
				
				/* Creo el gamestate de loading con la configuracion del pinball a crear y el recurso de su mesa */
				LoadingGameState lgs = Main.newLoading(settings, tableList.getSelectedItem().getValue().getResource());
				lgs.setActive(true);
				lgs.startLoading();
			}
		});
		
		/*
		TODO 
		play = new GameMenuButton("src/resources/images/menu/play0.png", "src/resources/images/menu/play1.png");
		cont = new GameMenuButton("resources/images/menu/credits0.png", "resources/images/menu/credits1.png");
		quit = new GameMenuButton("resources/images/menu/quit0.png", "resources/images/menu/quit1.png");
		
		play.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
				MessageWindow mw = new MessageWindow("Nothing to play. Just a demo.");
				mw.pack();
				fengGUIdisplay.addWidget(mw);
				StaticLayout.center(mw, fengGUIdisplay);
			}
		});

		cont.addButtonPressedListener(new IButtonPressedListener()
		{
			public void buttonPressed(ButtonPressedEvent e)
			{
				MessageWindow mw = new MessageWindow("We dont take credit for FengGUI :)");
				mw.pack();
				fengGUIdisplay.addWidget(mw);
				StaticLayout.center(mw, fengGUIdisplay);
			}
		});

		quit.addButtonPressedListener(new IButtonPressedListener()
		{

			public void buttonPressed(ButtonPressedEvent e)
			{
				Main.shutdownGame();
			}
		});*/
	}
	
	@Override
	public void setActive(boolean active)
	{
		super.setActive(active);
		// TODO si hay juego en transcurso, el action (que falta agregar) en ESC me deberia hacer continue, sino salir
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
			c.removeAllWidgets();
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
		
		/* Para que la GUI se muestre bien */
		//GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
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
	
	private void populateTableList(ComboBox<Theme> list)
	{
		URL themesDirURL = MenuGameState.class.getClassLoader().getResource(THEMES_DIRECTORY);
		File themesDir = new File(themesDirURL.getPath().replace("%20", " ")); //TODO aca toque, agregue el replace
		
		/* Obtengo todos los archivos en el directorio de themes */
		File[] files = themesDir.listFiles();
		
		/* Voy a buscar aquellos archivos que sean x3d */
		String themeFilename;
		Theme theme;
		Pattern x3dFiles = Pattern.compile(".*\\.x3d", Pattern.CASE_INSENSITIVE);

		for (int i = 0; i < files.length; i++)
		{
			/* Obtengo el nombre de archivo del theme */
			themeFilename = files[i].getName();
		
			/* Si corresponde a un archivo x3d lo incorporo al menu */
			if (x3dFiles.matcher(themeFilename).find())
			{
				/* Creo el theme obteniendo su nombre del nombre de archivo y formando su recurso */
				theme = new Theme(themeFilename.substring(0, themeFilename.length() - 4),
						MenuGameState.class.getClassLoader().getResource(THEMES_DIRECTORY + themeFilename));
				
				/* Lo agrego a la lista */
				list.addItem(new ListItem<Theme>(theme.getName(), theme));
			}
		}
	}
	
	private class Theme
	{
		private String name;
		
		private URL resource;
		
		public Theme(String name, URL resource)
		{
			this.name = name;
			this.resource = resource;
		}

		public String getName()
		{
			return name;
		}

		public URL getResource()
		{
			return resource;
		}
	}
}
