package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jme.system.GameSettings;
import com.jme.system.PropertiesGameSettings;

//XXX NOTA GIGANTE: ojo al editar, que esto fue autogenerado por el visual editor
public class XtremmePreferences extends JFrame
{
    
    static final int[][] RESOLUTIONS = {
        {640, 480},
        {800, 600},
        {1024, 768},
        {1280, 1024},
        {1280, 800},
        {1440, 900},
        {1600, 1024},
        {1600, 1200},
        {1920, 1200}
    };
    
    static final int[] DEPTHS = { 16, 24, 32 };
    
    static final int[] FREQUENCIES = new int[] { 60, 70, 72, 75, 85, 100, 120, 140 };
    
    private static final long serialVersionUID = 1L;
    
    private PropertiesGameSettings preferences;
    private JPanel jPanel = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private JCheckBox fullScreenCheckBox = null;
    private JComboBox sizeCombo = null;
    private JComboBox freqCombo = null;
    private JComboBox depthCombo = null;
    private JLabel sizeLabel = null;
    private JLabel frequencyLabel = null;
    private JLabel depthLabel = null;
    private JLabel titleLabel = null;
    private JButton restoreButton = null;
    private JImagePanel imagePanel = null;

    private static void setDefaults( PropertiesGameSettings preferences )
    {
        preferences.setDepth( 24 );
        preferences.setFrequency( 60 );
        preferences.setWidth( 800 );
        preferences.setHeight( 600 );
        preferences.setFullscreen( false );
    }
    
    /**
     * This method initializes 
     * 
     */
    public XtremmePreferences() {
    	super();
        preferences = new PropertiesGameSettings("xtremme-pinball.properties", null);
        if (!preferences.load())
        {
            setDefaults( preferences );
        } 
    	
    	initialize();
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    	this.pack();
    	this.setVisible( true );

    }

    public GameSettings getSettings()
    {
        return this.preferences;
    }
    
    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(null);
        this.setName("Preferences");
        this.setTitle("Preferences");
        this.setSize(new Dimension(465, 360));
        this.setPreferredSize(this.getSize());
        this.setResizable(false);
        this.setContentPane(getJPanel());
        this.setLocationRelativeTo(null);
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
            titleLabel = new JLabel();
            titleLabel.setBounds(new Rectangle(20, 117, 205, 20));
            titleLabel.setText("Choose your display preferences");
            sizeLabel = new JLabel();
            sizeLabel.setBounds(new Rectangle(20, 140, 205, 20));
            sizeLabel.setText("Screen Size");
            frequencyLabel = new JLabel();
            frequencyLabel.setBounds(new Rectangle(240, 140, 205, 20));
            frequencyLabel.setText("Frequency");
            depthLabel = new JLabel();
            depthLabel.setBounds(new Rectangle(240, 200, 205, 20));
            depthLabel.setText("Depth");
            
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setBounds(new Rectangle(0, 100, 465, 260));
            
            jPanel.add(getOkButton(), null);
			jPanel.add(getCancelButton(), null);
			jPanel.add(getFullScreenCheckBox(), null);
			jPanel.add(getSizeCombo(), null);
			jPanel.add(getFrequencyCombo(), null);
			jPanel.add(getDepthCombo(), null);
			jPanel.add(sizeLabel, null);
			jPanel.add(frequencyLabel, null);
			jPanel.add(depthLabel, null);
			jPanel.add(titleLabel, null);
			jPanel.add(getRestoreButton(), null);
			jPanel.add(getImagePanel(), null);
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
            okButton.setBounds(new Rectangle(15, 280, 100, 40));
            okButton.setText("OK");
            okButton.addActionListener( new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent e )
                {
                    try
                    {
                        preferences.save();
                    }
                    catch( IOException ioe )
                    {
                        System.err.println("Could not save preferences");
                        ioe.printStackTrace();
                    }
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
            cancelButton.setBounds(new Rectangle(340, 280, 100, 40));
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
     * This method initializes fullScreenCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getFullScreenCheckBox()
    {
        if ( fullScreenCheckBox == null )
        {
            fullScreenCheckBox = new JCheckBox();
            fullScreenCheckBox.setBounds(new Rectangle(20, 220, 205, 20));
            fullScreenCheckBox.setSelected( preferences.isFullscreen() );
            fullScreenCheckBox.setText("Fullscreen");
            fullScreenCheckBox.addChangeListener( new ChangeListener() {

                
                public void stateChanged( ChangeEvent e )
                {
                    preferences.setFullscreen( !preferences.isFullscreen() );
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
            sizeCombo.setBounds(new Rectangle(20, 160, 205, 20));
            
            for ( int[] size : RESOLUTIONS )
            {
                sizeCombo.addItem( new ScreenSize(size[0],size[1]) );
            }

            sizeCombo.setSelectedItem( new ScreenSize(preferences.getWidth(),preferences.getHeight()) );
            sizeCombo.addItemListener( new ItemListener() {

                
                public void itemStateChanged( ItemEvent e )
                {
                    ScreenSize ss = (ScreenSize)e.getItem();
                    preferences.setWidth(ss.w);
                    preferences.setHeight(ss.h);
                }
                
            });
        }
        return sizeCombo;
    }
    
    /**
     * This method initializes frequencyCombo    
     *  
     * @return javax.swing.JComboBox    
     */
    private JComboBox getFrequencyCombo()
    {
        if ( freqCombo == null )
        {
            freqCombo = new JComboBox();
            freqCombo.setBounds(new Rectangle(240, 160, 205, 20));
            
            for ( int freq : FREQUENCIES )
            {
                freqCombo.addItem( freq );
            }

            freqCombo.setSelectedItem( preferences.getFrequency() );
            freqCombo.addItemListener( new ItemListener() {

                
                public void itemStateChanged( ItemEvent e )
                {
                    preferences.setFrequency( (Integer)e.getItem() );
                }
                
            });
        }
        return freqCombo;
    }
    
    /**
     * This method initializes depthCombo    
     *  
     * @return javax.swing.JComboBox    
     */
    private JComboBox getDepthCombo()
    {
        if ( depthCombo == null )
        {
            depthCombo = new JComboBox();
            depthCombo.setBounds(new Rectangle(240, 220, 205, 20));
            
            for ( int depth : DEPTHS )
            {
                depthCombo.addItem( depth );
            }

            depthCombo.setSelectedItem( preferences.getDepth() );
            depthCombo.addItemListener( new ItemListener() {

                
                public void itemStateChanged( ItemEvent e )
                {
                   preferences.setDepth( (Integer)e.getItem() );
                }
                
            });
        }
        return depthCombo;
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


        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + h;
            result = prime * result + w;
            return result;
        }


        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( !( obj instanceof ScreenSize ) ) return false;
            final ScreenSize other = (ScreenSize) obj;
            if ( h != other.h ) return false;
            if ( w != other.w ) return false;
            return true;
        }
    }
    
    public void dispose() {
        super.dispose();
        synchronized ( this )
        {
            this.notifyAll();    
        }
        
    }
    

    /**
     * This method initializes restoreButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getRestoreButton()
    {
        if ( restoreButton == null )
        {
            restoreButton = new JButton();
            restoreButton.setBounds(new Rectangle(177, 280, 100, 40));
            restoreButton.setText("Restore");
            restoreButton.addActionListener( new java.awt.event.ActionListener()
            {
                public void actionPerformed( java.awt.event.ActionEvent e )
                {
                    if (!preferences.load())
                        setDefaults( preferences );
                    sizeCombo.setSelectedItem( new ScreenSize(preferences.getWidth(),preferences.getHeight() ));
                    depthCombo.setSelectedItem( preferences.getDepth() );
                    freqCombo.setSelectedItem( preferences.getFrequency());
                    fullScreenCheckBox.setSelected( preferences.isFullscreen() );
                }
            } );
        }
        return restoreButton;
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
    
    private JImagePanel getImagePanel()
    {
        if (imagePanel == null)
        {
            imagePanel = new JImagePanel();
            imagePanel.setLayout(null);
            imagePanel.setBounds(new Rectangle(0,0, 465, 112));
            try
            {
                imagePanel.openImage( "resources/textures/preferences-logo.jpg" );
            }
            catch (IOException e)
            {
                System.err.println("error");
            }
        }
        
        return imagePanel;
    }
    
    private class JImagePanel extends JPanel 
    {
        private static final long serialVersionUID = 1L;
        private BufferedImage image;
        private String imageFileName;
        
        public JImagePanel() 
        {
            super();
        }
        
        public void openImage( String imageFileName ) throws IOException
        {
            this.imageFileName = imageFileName;

            InputStream input = this.getClass().getClassLoader().getResource(imageFileName).openStream();
            
            image = ImageIO.read(input);
        }

        public String getImageFileName()
        {
            return this.imageFileName;
        }
        
        public void paint(Graphics g) 
        {
            g.drawImage( image, 0, 0, null);
        }
      
    }
   
}  //  @jve:decl-index=0:visual-constraint="15,15"
