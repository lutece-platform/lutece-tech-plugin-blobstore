package fr.paris.lutece.plugins.blobstore.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import fr.paris.lutece.plugins.blobstore.service.database.DatabaseBlobStoreService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * 
 * DatabaseBlobStoreServiceTest
 *
 */
public class DatabaseBlobStoreServiceTest extends LuteceTestCase
{
	/**
	 * Tests input stream
	 */
	public void testLoadInputStream() throws IOException
	{
		String strFilename = getResourcesDir(  ) + "../test-classes/testblob.txt";
		byte[] bStore =  IOUtils.toByteArray( new FileInputStream( strFilename ) );
		
		DatabaseBlobStoreService service = new DatabaseBlobStoreService();
		String strKey = service.store( bStore );
		
		
		InputStream is = service.getBlobInputStream( strKey );
		byte[] bRead = IOUtils.toByteArray( is );
		
		assertTrue( Arrays.equals( bRead, bStore ) );
	}
}
