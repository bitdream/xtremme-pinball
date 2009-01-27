package loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jmex.model.converters.DTDResolver;
import com.jmex.physics.PhysicsSpace;

/**
 * Una clase que wrappea el loader de jme para trabajar todos los x3d igual
 * 
 * @author Mariano
 */
public class X3DLoader
{
    // TODO poner loggers posta
    private static final Logger      logger        = Logger.getAnonymousLogger();

    private InputStream              x3d;

    private URL                      textureDirURL = null;

    private Map<String, InputStream> dtds          = null;

    private PhysicsSpace             pinball  = null;

    private LightState               lightState    = null;

    private String                   x3dFileName   = null;

    public X3DLoader( URL x3dFilename ) throws FileNotFoundException
    {
        //        System.out.println(x3dFilename.getFile());
        //        System.out.println(x3dFilename.getPath());
        //        try{System.out.println(x3dFilename.toURI());}catch(URISyntaxException e){e.printStackTrace();}
        //        try{System.out.println(x3dFilename.getContent());}catch(Exception e) {e.printStackTrace();}

        try
        {
            this.x3d = new FileInputStream( x3dFilename.getFile() );
            this.x3dFileName = x3dFilename.getPath();
        }
        catch ( FileNotFoundException e )
        {
            logger.severe( "file: " + x3dFilename + " does not exist" );
            throw e;
        }
    }

    /**
     * Esta funcion carga el archivo especificado buscando texturas en el directorio textureDir
     * 
     * @return la escena cargada en formato jme
     */
    public Node loadScene()
    {
        //logger.info( "Entering loadScene" );

        // creamos el nodo de la escena
        Node node = null;

        // instancia del conversor
        X3dToJme converter = null;
        try
        {
            converter = new X3dToJme();
        }
        catch ( InstantiationException e1 )
        {
            logger.severe( "Can't instantiate X3D converter" );
            return new Node();
        }

        // tratamos de usar dtds locales para evitar la conexion a internet
        if ( this.dtds != null )
        {
            DTDResolver resolver = new DTDResolver( dtds );
            converter.setDTDResolver( resolver );
        }
        else
        {
            try
            {
                converter.setDTDResolver( X3DLoader.getStandardDTDResolver() );
            }
            catch ( FileNotFoundException e )
            {
                logger.info( "Could not find local dtds, switching to remote..." );
            }
        }

        // seteamos las properties en el conversor
        if ( this.textureDirURL != null )
        {
            converter.setProperty( "textures", textureDirURL );
        }
        else
        {
            converter.setProperty( "textures", X3DLoader.getStandardTextureDir() );
        }
        converter.setProperty( "pinball", pinball );

        //logger.info( "Starting to convert .x3d to .jme" );
        try
        {
            node = (Node) converter.loadScene( this.x3d, null, this.lightState );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            logger.severe( "Converting " + this.x3dFileName + ": could not parse file." );
            return new Node();
        }
        //logger.info( "Done converting" );

        return node;
    }

    public PhysicsSpace getPinball()
    {
        return pinball;
    }

    public void setPinball( PhysicsSpace pinball )
    {
        this.pinball = pinball;
    }

    public LightState getLightState()
    {
        return lightState;
    }

    public void setLightState( LightState lightState )
    {
        this.lightState = lightState;
    }

    /**
     * Setter del mapa de dtds e inputstreams
     * 
     * @param dtds
     */
    public void setDTDMap( Map<String, InputStream> dtds )
    {
        this.dtds = dtds;
    }

    /**
     * Getter del mapa de dtds
     * 
     * @return
     */
    public Map<String, InputStream> getDTDMap()
    {
        return this.dtds;
    }

    /**
     * Con esta funcion se evita tener que agregar entradas al mapa desde afuera Se pueden poner dtds y xsd
     * 
     * @param key
     *            el nombre del dtd/xsd
     * @param path
     *            la ruta al archivo en el sistema
     * @throws FileNotFoundException
     *             si no encuentra el archivo lanza file not found
     */
    public void putDTD( String key, URL path ) throws FileNotFoundException
    {
        if ( this.dtds == null )
        {
            this.dtds = new HashMap<String, InputStream>();
        }
        try
        {
            this.dtds.put( key, (InputStream)path.getContent() );
        }
        catch (IOException e)
        {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    /**
     * Setter del directorio de texturas
     * 
     * @param path
     */
    public void setTextureDir( URL path )
    {
        this.textureDirURL = path;
    }

    /**
     * Armamos un hash de archivos que esperamos que estén localmente para resolver los dtds más rápido.
     * 
     * @return
     * @throws FileNotFoundException
     */
    public static DTDResolver getStandardDTDResolver() throws FileNotFoundException
    {
        DTDResolver ret;

        HashMap<String, InputStream> dtds = new HashMap<String, InputStream>();

        try
        {
            dtds.put( "x3d-3.0.dtd", (InputStream) X3DLoader.class.getClassLoader().getResource(
                "resources/models/dtd/x3d-3.0.dtd" ).getContent() );
            dtds.put( "x3d-3.0-InputOutputFields.dtd", (InputStream) X3DLoader.class.getClassLoader().getResource(
                "resources/models/dtd/x3d-3.0-InputOutputFields.dtd" ).getContent() );
            dtds.put( "x3d-3.0-Web3dExtensionsPrivate.dtd", (InputStream) X3DLoader.class.getClassLoader().getResource(
                "resources/models/dtd/x3d-3.0-Web3dExtensionsPrivate.dtd" ).getContent() );
            dtds.put( "x3d-3.0-Web3dExtensionsPublic.dtd", (InputStream) X3DLoader.class.getClassLoader().getResource(
                "resources/models/dtd/x3d-3.0-Web3dExtensionsPublic.dtd" ).getContent() );
            dtds.put( "x3d-3.0.xsd", (InputStream) X3DLoader.class.getClassLoader().getResource(
                "resources/models/dtd/x3d-3.0.xsd" ).getContent() );
        }
        catch ( IOException e )
        {
            throw new FileNotFoundException(e.getMessage());
        }

        ret = new DTDResolver( dtds );

        return ret;
    }

    /**
     * Retorna los el directorio default para las texturas
     * 
     * @return
     */
    public static URL getStandardTextureDir()
    {
        //System.out.println( X3DLoader.class.getClassLoader().getResource( "resources/textures/" ) );
        return X3DLoader.class.getClassLoader().getResource( "resources/textures/" );
    }

}
