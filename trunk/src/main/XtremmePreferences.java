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
    
    
    public void clear() throws IOException
    {
        // DO NOTHING
    }

    
    public String get( String name, String defaultValue )
    {
        return null;
    }

    
    public int getAlphaBits()
    {
        return DEFAULT_ALPHA_BITS;
    }

    
    public boolean getBoolean( String name, boolean defaultValue )
    {
        return false;
    }

    
    public byte[] getByteArray( String name, byte[] bytes )
    {
        return null;
    }

    
    public String getDefaultSettingsWidgetImage()
    {
        return null;
    }

    
    public int getDepth()
    {
        return DEFAULT_DEPTH;
    }

    
    public int getDepthBits()
    {
        return DEFAULT_DEPTH_BITS;
    }

    
    public double getDouble( String name, double defaultValue )
    {
        return 0;
    }

    
    public float getFloat( String name, float defaultValue )
    {
        return 0;
    }

    
    public int getFramerate()
    {
        return DEFAULT_FRAMERATE;
    }

    
    public int getFrequency()
    {
        return DEFAULT_FREQUENCY;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }

    
    public int getInt( String name, int defaultValue )
    {
        return 0;
    }

    
    public long getLong( String name, long defaultValue )
    {
        return 0;
    }

    
    public Object getObject( String name, Object obj )
    {
        return null;
    }

    
    public String getRenderer()
    {
        return DEFAULT_RENDERER;
    }

    
    public int getSamples()
    {
        return DEFAULT_SAMPLES;
    }

    
    public int getStencilBits()
    {
        return DEFAULT_STENCIL_BITS;
    }

    @Override
    public int getWidth()
    {
        return this.width;
    }

    
    public boolean isFullscreen()
    {
        return this.fullScreen;
    }

    
    public boolean isMusic()
    {
        return this.sound;
    }

    
    public boolean isNew()
    {
        return false;
    }

    
    public boolean isSFX()
    {
        return this.sound;
    }

    
    public boolean isVerticalSync()
    {
        return DEFAULT_VERTICAL_SYNC;
    }

    
    public void save() throws IOException
    {
        // DO NOTHING
    }

    
    public void set( String name, String value )
    {
        // DO NOTHING
    }

    
    public void setAlphaBits( int alphaBits )
    {
        // DO NOTHING
    }

    
    public void setBoolean( String name, boolean value )
    {
        // DO NOTHING
    }

    
    public void setByteArray( String name, byte[] bytes )
    {
        // DO NOTHING
    }

    
    public void setDepth( int depth )
    {
        // DO NOTHING
    }

    
    public void setDepthBits( int depthBits )
    {
        // DO NOTHING
    }

    
    public void setDouble( String name, double value )
    {
        // DO NOTHING
    }

    
    public void setFloat( String name, float value )
    {
        // DO NOTHING
    }

    
    public void setFramerate( int framerate )
    {
        // DO NOTHING
    }

    
    public void setFrequency( int frequency )
    {
        // DO NOTHING
    }

    
    public void setFullscreen( boolean fullscreen )
    {
        this.fullScreen = fullscreen;
    }

    
    public void setHeight( int height )
    {
        this.height = height;
    }

    
    public void setInt( String name, int value )
    {
        // DO NOTHING
    }

    
    public void setLong( String name, long value )
    {
        // DO NOTHING
    }

    
    public void setMusic( boolean musicEnabled )
    {
        this.sound = musicEnabled;
    }

    
    public void setObject( String name, Object obj )
    {
        // DO NOTHING
    }

    
    public void setRenderer( String renderer )
    {
        // DO NOTHING
    }

    
    public void setSFX( boolean sfxEnabled )
    {
        this.sound = sfxEnabled;
    }

    
    public void setSamples( int samples )
    {
        // DO NOTHING
    }

    
    public void setStencilBits( int stencilBits )
    {
        // DO NOTHING
    }

    
    public void setVerticalSync( boolean vsync )
    {
        // DO NOTHING
    }

    
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
