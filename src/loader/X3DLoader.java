package loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private PhysicsSpace             physicsSpace  = null;

    private LightState               lightState    = null;

    private String                   x3dFileName   = null;

    public X3DLoader( String x3dFilename ) throws FileNotFoundException
    {
        try
        {
            this.x3d = new FileInputStream( x3dFilename );
            this.x3dFileName = x3dFilename;
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
        converter.setProperty( "physicsSpace", physicsSpace );

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

    public PhysicsSpace getPhysicsSpace()
    {
        return physicsSpace;
    }

    public void setPhysicsSpace( PhysicsSpace physicsSpace )
    {
        this.physicsSpace = physicsSpace;
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
    public void putDTD( String key, String path ) throws FileNotFoundException
    {
        if ( this.dtds == null )
        {
            this.dtds = new HashMap<String, InputStream>();
        }
        this.dtds.put( key, new FileInputStream( path ) );
    }

    /**
     * Setter del directorio de texturas
     * 
     * @param path
     */
    public void setTextureDir( String path ) throws MalformedURLException
    {
        this.textureDirURL = new URL( "file://" + path + '/' );
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

        dtds.put( "x3d-3.0.dtd", new FileInputStream( "./resources/models/dtd/x3d-3.0.dtd" ) );
        dtds.put( "x3d-3.0-InputOutputFields.dtd", new FileInputStream(
            "./resources/models/dtd/x3d-3.0-InputOutputFields.dtd" ) );
        dtds.put( "x3d-3.0-Web3dExtensionsPrivate.dtd", new FileInputStream(
            "./resources/models/dtd/x3d-3.0-Web3dExtensionsPrivate.dtd" ) );
        dtds.put( "x3d-3.0-Web3dExtensionsPublic.dtd", new FileInputStream(
            "./resources/models/dtd/x3d-3.0-Web3dExtensionsPublic.dtd" ) );
        dtds.put( "x3d-3.0.xsd", new FileInputStream( "./resources/models/dtd/x3d-3.0.xsd" ) );

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
        String texDir = "file://" + System.getProperty( "user.dir" ) + "/resources/textures/";
        try
        {
            return new URL( texDir );
        }
        catch ( MalformedURLException e )
        {
            return null;
        }
    }

}
