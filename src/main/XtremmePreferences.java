package main;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jme.system.GameSettings;

//XXX NOTA GIGANTE: ojo al editar, que esto fue autogenerado por el visual editor
public class XtremmePreferences extends JFrame implements GameSettings
{
    private static final long serialVersionUID = 1L;
    private int width = 800;
    private int height = 600;
    private boolean fullScreen = false;
    private boolean sound = true;
    private JPanel jPanel = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private JCheckBox soundCheckBox = null;
    private JCheckBox fullScreenCheckBox = null;
    private JComboBox sizeCombo = null;
    private JLabel title = null;

    /**
     * This method initializes 
     * 
     */
    public XtremmePreferences() {
    	super();
    	initialize();
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    	this.pack();
    	this.setVisible( true );
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(null);
        this.setName("Preferences");
        this.setTitle("Preferences");
        this.setSize(new Dimension(250, 260));
        this.setPreferredSize(this.getSize());
        this.setResizable(false);
        this.setContentPane(getJPanel());
    }



    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel()
    {
        if ( jPanel == null )
        {
            title = new JLabel();
            title.setBounds(new Rectangle(20, 13, 204, 34));
            title.setText("Screen Size");
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setBounds(new Rectangle(0, 0, 245, 220));
            jPanel.add(getOkButton(), null);

			jPanel.add(getCancelButton(), null);
			jPanel.add(getSoundCheckBox(), null);
			jPanel.add(getFullScreenCheckBox(), null);
			jPanel.add(getSizeCombo(), null);
			jPanel.add(title, null);
        }
        return jPanel;
    }

    /**
     * This method initializes okButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOkButton()
    {
        if ( okButton == null )
        {
            okButton = new JButton();
            okButton.setBounds(new Rectangle(15, 180, 100, 40));
            okButton.setText("OK");
            okButton.addActionListener( new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent e )
                {
                    dispose();
                }
            } );
        }
        return okButton;
    }

    /**
     * This method initializes cancelButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getCancelButton()
    {
        if ( cancelButton == null )
        {
            cancelButton = new JButton();
            cancelButton.setBounds(new Rectangle(130, 180, 100, 40));
            cancelButton.setText("Cancel");
            cancelButton.addActionListener( new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent e )
                {
                    System.exit( 0 );
                }
            } );
        }
        return cancelButton;
    }

    /**
     * This method initializes soundCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getSoundCheckBox()
    {
        if ( soundCheckBox == null )
        {
            soundCheckBox = new JCheckBox();
            soundCheckBox.setBounds(new Rectangle(20, 140, 205, 20));
            soundCheckBox.setSelected(true);
            soundCheckBox.setText("Enable Sound");
            soundCheckBox.addChangeListener( new ChangeListener() {

                @Override
                public void stateChanged( ChangeEvent e )
                {
                    sound = !sound;
                }
                
            });
        }
        return soundCheckBox;
    }

    /**
     * This method initializes fullScreenCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getFullScreenCheckBox()
    {
        if ( fullScreenCheckBox == null )
        {
            fullScreenCheckBox = new JCheckBox();
            fullScreenCheckBox.setBounds(new Rectangle(20, 100, 205, 20));
            fullScreenCheckBox.setText("Fullscreen");
            fullScreenCheckBox.addChangeListener( new ChangeListener() {

                @Override
                public void stateChanged( ChangeEvent e )
                {
                    fullScreen = !fullScreen;
                }
                
            });
        }
        return fullScreenCheckBox;
    }

    /**
     * This method initializes sizeCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getSizeCombo()
    {
        if ( sizeCombo == null )
        {
            sizeCombo = new JComboBox();
            sizeCombo.setBounds(new Rectangle(20, 60, 205, 20));
            sizeCombo.addItem( new ScreenSize(640,480) );
            sizeCombo.addItem( new ScreenSize(800,600) );
            sizeCombo.addItem( new ScreenSize(1024,768) );
            sizeCombo.setSelectedIndex(1);
            sizeCombo.addItemListener( new ItemListener() {

                @Override
                public void itemStateChanged( ItemEvent e )
                {
                    ScreenSize ss = (ScreenSize)e.getItem();
                    width = ss.w;
                    height = ss.h;
                }
                
            });
        }
        return sizeCombo;
    }

    private final class ScreenSize
    {
        private int w, h;
        
        private ScreenSize(int w, int h)
        {
            this.w = w;
            this.h = h;
        }

        @Override
        public String toString()
        {
            return new String( w + "x" + h );
        }
    }
    
    public void dispose() {
        super.dispose();
        synchronized ( this )
        {
            this.notifyAll();    
        }
        
    }
    
    @Override
    public void clear() throws IOException
    {
        // DO NOTHING
    }

    @Override
    public String get( String name, String defaultValue )
    {
        return null;
    }

    @Override
    public int getAlphaBits()
    {
        return DEFAULT_ALPHA_BITS;
    }

    @Override
    public boolean getBoolean( String name, boolean defaultValue )
    {
        return false;
    }

    @Override
    public byte[] getByteArray( String name, byte[] bytes )
    {
        return null;
    }

    @Override
    public String getDefaultSettingsWidgetImage()
    {
        return null;
    }

    @Override
    public int getDepth()
    {
        return DEFAULT_DEPTH;
    }

    @Override
    public int getDepthBits()
    {
        return DEFAULT_DEPTH_BITS;
    }

    @Override
    public double getDouble( String name, double defaultValue )
    {
        return 0;
    }

    @Override
    public float getFloat( String name, float defaultValue )
    {
        return 0;
    }

    @Override
    public int getFramerate()
    {
        return DEFAULT_FRAMERATE;
    }

    @Override
    public int getFrequency()
    {
        return DEFAULT_FREQUENCY;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }

    @Override
    public int getInt( String name, int defaultValue )
    {
        return 0;
    }

    @Override
    public long getLong( String name, long defaultValue )
    {
        return 0;
    }

    @Override
    public Object getObject( String name, Object obj )
    {
        return null;
    }

    @Override
    public String getRenderer()
    {
        return DEFAULT_RENDERER;
    }

    @Override
    public int getSamples()
    {
        return DEFAULT_SAMPLES;
    }

    @Override
    public int getStencilBits()
    {
        return DEFAULT_STENCIL_BITS;
    }

    @Override
    public int getWidth()
    {
        return this.width;
    }

    @Override
    public boolean isFullscreen()
    {
        return this.fullScreen;
    }

    @Override
    public boolean isMusic()
    {
        return this.sound;
    }

    @Override
    public boolean isNew()
    {
        return false;
    }

    @Override
    public boolean isSFX()
    {
        return this.sound;
    }

    @Override
    public boolean isVerticalSync()
    {
        return DEFAULT_VERTICAL_SYNC;
    }

    @Override
    public void save() throws IOException
    {
        // DO NOTHING
    }

    @Override
    public void set( String name, String value )
    {
        // DO NOTHING
    }

    @Override
    public void setAlphaBits( int alphaBits )
    {
        // DO NOTHING
    }

    @Override
    public void setBoolean( String name, boolean value )
    {
        // DO NOTHING
    }

    @Override
    public void setByteArray( String name, byte[] bytes )
    {
        // DO NOTHING
    }

    @Override
    public void setDepth( int depth )
    {
        // DO NOTHING
    }

    @Override
    public void setDepthBits( int depthBits )
    {
        // DO NOTHING
    }

    @Override
    public void setDouble( String name, double value )
    {
        // DO NOTHING
    }

    @Override
    public void setFloat( String name, float value )
    {
        // DO NOTHING
    }

    @Override
    public void setFramerate( int framerate )
    {
        // DO NOTHING
    }

    @Override
    public void setFrequency( int frequency )
    {
        // DO NOTHING
    }

    @Override
    public void setFullscreen( boolean fullscreen )
    {
        this.fullScreen = fullscreen;
    }

    @Override
    public void setHeight( int height )
    {
        this.height = height;
    }

    @Override
    public void setInt( String name, int value )
    {
        // DO NOTHING
    }

    @Override
    public void setLong( String name, long value )
    {
        // DO NOTHING
    }

    @Override
    public void setMusic( boolean musicEnabled )
    {
        this.sound = musicEnabled;
    }

    @Override
    public void setObject( String name, Object obj )
    {
        // DO NOTHING
    }

    @Override
    public void setRenderer( String renderer )
    {
        // DO NOTHING
    }

    @Override
    public void setSFX( boolean sfxEnabled )
    {
        this.sound = sfxEnabled;
    }

    @Override
    public void setSamples( int samples )
    {
        // DO NOTHING
    }

    @Override
    public void setStencilBits( int stencilBits )
    {
        // DO NOTHING
    }

    @Override
    public void setVerticalSync( boolean vsync )
    {
        // DO NOTHING
    }

    @Override
    public void setWidth( int width )
    {
        this.width = width;
    }
    
//  return new PreferencesGameSettings( userPrefsRoot.node( gameName ), false, "game-defaults.properties" );
//  boolean newNode = true;
//  Preferences userPrefsRoot = Preferences.userRoot();
//  try
//  {
//      newNode = !userPrefsRoot.nodeExists( gameName );
//  }
//  catch ( BackingStoreException bse )
//  {
//  }
    /* To persist to a .properties file instead of java.util.prefs,
       change this method like this:
    com.jme.system.PropertiesGameSettings pgs =
            new com.jme.system.PropertiesGameSettings("pgs.properties");
    pgs.load();
    return pgs;
    */
    
}  //  @jve:decl-index=0:visual-constraint="15,15"
