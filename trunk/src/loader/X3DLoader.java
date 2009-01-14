package loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.scene.Node;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.converters.DTDResolver;

public class X3DLoader
{
    private static final Logger logger = Logger.getAnonymousLogger();

    public static Node loadScene( String x3dFilename, String textureDir )
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
        
        // tratamos de usar dtds locales para evitar la conexión a internet
        DTDResolver dtds;
        try
        {
            dtds = X3DLoader.getDTDResolver();
            converter.setDTDResolver( dtds );
        }
        catch ( FileNotFoundException e2 )
        {
            logger.info( "Could not find local dtds, switching to remote..." );
        }
        
        
        // armamos la URL del directorio de las texturas
        URL textureDirURL;
        try
        {
            textureDirURL = new URL( "file://" + textureDir + '/' );
        }
        catch ( MalformedURLException e1 )
        {
            logger.severe( "textureDir: " + textureDir + " is invalid" );
            return new Node();
        }
        
        // seteamos la url en el conversor
        converter.setProperty( "textures", textureDirURL );        
        
        InputStream IP;
        try
        {
            IP = new FileInputStream( x3dFilename );
        }
        catch ( FileNotFoundException e )
        {
            logger.severe( "file: " + x3dFilename + " does not exist" );
            return new Node();
        }

        ByteArrayOutputStream BO = new ByteArrayOutputStream();

        
        //logger.info( "Starting to convert .x3d to .jme" );
        try
        {
            converter.convert( IP, BO );
        }
        catch ( IOException e )
        {
            logger.severe( "Converting " + x3dFilename + ": could not parse file." );
            return new Node();
        }
        //logger.info( "Done converting" );

        //logger.info( "Start Loading" );
        try
        {
            node = (Node) BinaryImporter.getInstance().load( new ByteArrayInputStream( BO.toByteArray() ) );
        }
        catch ( IOException e )
        {
            logger.logp( Level.SEVERE, X3DLoader.class.toString(), "loadScene( String x3dFilename, String textureDir )", "IOException", e );
            return new Node();
        }
        //logger.info( "Finished loading" ) );
        
        return node;
    }
    
    
    private static DTDResolver getDTDResolver() throws FileNotFoundException
    {
        DTDResolver ret;

        HashMap<String, InputStream> dtds = new HashMap<String, InputStream>();

        dtds.put( "x3d-3.0.dtd", new FileInputStream( "./resources/models/dtd/x3d-3.0.dtd" ) );
        dtds.put( "x3d-3.0-InputOutputFields.dtd", new FileInputStream( "./resources/models/dtd/x3d-3.0-InputOutputFields.dtd" ) );
        dtds.put( "x3d-3.0-Web3dExtensionsPrivate.dtd", new FileInputStream( "./resources/models/dtd/x3d-3.0-Web3dExtensionsPrivate.dtd" ) );
        dtds.put( "x3d-3.0-Web3dExtensionsPublic.dtd", new FileInputStream( "./resources/models/dtd/x3d-3.0-Web3dExtensionsPublic.dtd" ) );
        dtds.put( "x3d-3.0.xsd", new FileInputStream( "./resources/models/dtd/x3d-3.0.xsd" ) );
        

        ret = new DTDResolver( dtds );

        return ret;
    }
}
