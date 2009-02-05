package gamestates;

import input.FengJMEInputHandler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import main.Main;

import org.fenggui.Button;
import org.fenggui.ComboBox;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.Label;
import org.fenggui.ListItem;
import org.fenggui.Slider;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.composite.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.event.ISliderMovedListener;
import org.fenggui.event.SliderMovedEvent;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.util.Alignment;
import org.fenggui.util.Spacing;

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
	
	/* La ventana de los menues */
	private Window menu;

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
 	}

	/**
	 * Creo la ventana.
	 */
	private void buildWindow()
	{
		//TODO darle algo de estilo al menu y agregarle un fondo
		
		/* Remuevo todos los componentes que puedan haber en la pantalla */
		fengGUIdisplay.removeAllWidgets();
		
		/* Creo la ventana del menu */
		menu = FengGUI.createWindow(fengGUIdisplay, false, false, false, true);
		menu.setExpandable(false);
		menu.setResizable(false);
		menu.setShrinkable(false);
		menu.setMovable(false);
		menu.setTitle("Main menu");
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

				/* Cierro la ventana */
				menu.close();
				
				/* Continuo el juego actual */
				Main.continueCurrentPinballGame();
				
				/* Desactivo el gamestate de menu */
				Main.endMenu();

			}
		});
		continueButton.setVisible(false);

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
				/* Acabo con todo el juego */
				Main.shutdownGame();
			}
		});

		/* Lo centro */
		StaticLayout.center(menu, fengGUIdisplay);
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
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
				LoadingGameState lgs = Main.newLoading(settings, tableList.getSelectedItem().getValue().getResource());
				lgs.setActive(true);
				lgs.startLoading();
			}
		});
		
		/* Boton de cancelar */
		cancelButton = FengGUI.createButton(menu.getContentContainer(), "Cancel");
		
		cancelButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {
				
				/* Cierro la ventana */
				menu.close();
				
				/* Vuelvo a construir el menu principal */
				buildMainMenu();
			}
		});
		
		/* Lo centro */
		StaticLayout.center(menu, fengGUIdisplay);
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
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
	    URI themesDirURI;
	    try 
	    {
	        themesDirURI = MenuGameState.class.getClassLoader().getResource(THEMES_DIRECTORY).toURI();
	    } catch ( URISyntaxException e )
	    {
	        System.out.println("Error que no deberia pasar");
	        themesDirURI = null;
	    }
		
		File themesDir = new File(themesDirURI.getPath()); //TODO aca toque, agregue el replace
		
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
