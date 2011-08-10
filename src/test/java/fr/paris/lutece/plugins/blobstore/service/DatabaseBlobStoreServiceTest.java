package fr.paris.lutece.plugins.blobstore.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import fr.paris.lutece.plugins.blobstore.service.database.DatabaseBlobStoreService;
import fr.paris.lutece.portal.service.blobstore.BlobStoreFileItem;
import fr.paris.lutece.portal.service.blobstore.NoSuchBlobException;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * 
 * DatabaseBlobStoreServiceTest
 *
 */
public class DatabaseBlobStoreServiceTest extends LuteceTestCase
{
	private static final String FILE_NAME = "testblob.txt";
	/**
	 * Tests input stream
	 */
	public void testLoadInputStream() throws IOException
	{
		String strFilename = getResourcesDir(  ) + "../test-classes/" + FILE_NAME;
		byte[] bStore =  IOUtils.toByteArray( new FileInputStream( strFilename ) );
		
		DatabaseBlobStoreService service = new DatabaseBlobStoreService();
		String strKey = service.store( bStore );
		
		
		InputStream is = service.getBlobInputStream( strKey );
		byte[] bRead = IOUtils.toByteArray( is );
		
		assertTrue( Arrays.equals( bRead, bStore ) );
	}
	
	public void testBlobStoreFileItem(  ) throws IOException, NoSuchBlobException
	{
		String strFilename = getResourcesDir(  ) + "../test-classes/testblob.txt";
		byte[] bStore =  IOUtils.toByteArray( new FileInputStream( strFilename ) );
		
		DatabaseBlobStoreService service = new DatabaseBlobStoreService();
		String strFileKey = service.store( bStore );
		File file = new File( strFilename );
		
		String strMetadata = BlobStoreFileItem.buildFileMetadata( FILE_NAME, file.length(), strFileKey );
		
		String strMetadataKey = service.store( strMetadata.getBytes(  ) );
		
		BlobStoreFileItem fileItem = new BlobStoreFileItem( strMetadataKey, service );
		
		assertEquals( strFileKey, fileItem.getFileBlobId(  ) );
		
		assertTrue( Arrays.equals( bStore, fileItem.get(  ) ) );
	}
}
