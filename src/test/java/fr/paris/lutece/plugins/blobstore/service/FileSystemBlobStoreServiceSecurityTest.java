/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.blobstore.service;

import fr.paris.lutece.plugins.blobstore.service.filesystem.FileSystemBlobStoreService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.test.LuteceTestCase;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;

/**
 * Security tests for {@link FileSystemBlobStoreService} : ensures a blob key cannot be used to read or delete files located outside of the blobstore
 * base directory through path traversal.
 */
public class FileSystemBlobStoreServiceSecurityTest extends LuteceTestCase
{
    /** Content of the file placed outside of the base directory, that must never be served by the blobstore. */
    private static final String SECRET_CONTENT = "TOP SECRET - must never be served by the blobstore";

    /** Name of the file placed outside of the base directory. */
    private static final String SECRET_FILE_NAME = "blobstore-secret.txt";

    /** Blob keys attempting to escape the base directory : all must be rejected. */
    private static final String [ ] TRAVERSAL_KEYS = {
            "../" + SECRET_FILE_NAME, "..\\" + SECRET_FILE_NAME, "foo/../../" + SECRET_FILE_NAME, ".." + File.separator + SECRET_FILE_NAME,
    };

    /**
     * Gets the blobstore base directory.
     *
     * @return the base directory
     */
    private String getBaseDirectory( )
    {
        return getResourcesDir( ) + "../test-classes/blobstore/";
    }

    /**
     * Gets the file placed just outside of the base directory, targeted by the traversal keys.
     *
     * @return the secret file
     */
    private File getSecretFile( )
    {
        return new File( getBaseDirectory( ) + "../" + SECRET_FILE_NAME );
    }

    /**
     * Builds a file system blobstore service on the test base directory.
     *
     * @param depth
     *            the depth
     * @return the service
     */
    private FileSystemBlobStoreService getService( final Integer depth )
    {
        FileSystemBlobStoreService service = new FileSystemBlobStoreService( );
        service.setBasePath( getBaseDirectory( ) );
        service.setDepth( depth );

        return service;
    }

    /**
     * Creates a clean base directory and writes the secret file outside of it.
     *
     * @throws Exception
     *             if the fixture cannot be prepared
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        final File baseFile = new File( getBaseDirectory( ) );

        if ( baseFile.exists( ) )
        {
            FileUtils.cleanDirectory( baseFile );
        }
        else
        {
            baseFile.mkdirs( );
        }

        FileUtils.writeStringToFile( getSecretFile( ), SECRET_CONTENT, "UTF-8" );
    }

    /**
     * Removes the secret file and cleans the base directory.
     *
     * @throws Exception
     *             if the fixture cannot be cleaned up
     */
    @Override
    protected void tearDown( ) throws Exception
    {
        FileUtils.deleteQuietly( getSecretFile( ) );

        final File baseFile = new File( getBaseDirectory( ) );

        if ( baseFile.exists( ) )
        {
            FileUtils.cleanDirectory( baseFile );
        }

        super.tearDown( );
    }

    /**
     * A traversal key must not allow reading the bytes of a file located outside of the base directory.
     *
     * @throws Exception
     *             if the test fails
     */
    public void testGetBlobRejectsPathTraversal( ) throws Exception
    {
        assertTrue( "Fixture error : the secret file must exist", getSecretFile( ).exists( ) );

        for ( Integer depth : new Integer [ ] {
                0, 1, 2
        } )
        {
            final FileSystemBlobStoreService service = getService( depth );

            for ( String strKey : TRAVERSAL_KEYS )
            {
                assertNull( "getBlob leaked a file outside the base directory with key '" + strKey + "' (depth " + depth + ")",
                        service.getBlob( strKey ) );

                final InputStream is = service.getBlobInputStream( strKey );

                if ( is != null )
                {
                    is.close( );
                    fail( "getBlobInputStream leaked a file outside the base directory with key '" + strKey + "' (depth " + depth + ")" );
                }
            }
        }
    }

    /**
     * A traversal key must not allow deleting a file located outside of the base directory.
     *
     * @throws Exception
     *             if the test fails
     */
    public void testDeleteRejectsPathTraversal( ) throws Exception
    {
        final FileSystemBlobStoreService service = getService( 0 );

        for ( String strKey : TRAVERSAL_KEYS )
        {
            try
            {
                service.delete( strKey );
            }
            catch( AppException e )
            {
                // a rejected traversal attempt is allowed to surface as an AppException
            }
        }

        assertTrue( "delete removed a file outside the base directory", getSecretFile( ).exists( ) );
        assertEquals( "delete altered a file outside the base directory", SECRET_CONTENT, FileUtils.readFileToString( getSecretFile( ), "UTF-8" ) );
    }

    /**
     * A legitimate blob must still be stored and retrieved after the traversal hardening.
     *
     * @throws Exception
     *             if the test fails
     */
    public void testLegitimateBlobStillRetrievable( ) throws Exception
    {
        final FileSystemBlobStoreService service = getService( 1 );

        final byte [ ] bStore = "legitimate content".getBytes( "UTF-8" );
        final String strKey = service.store( bStore );

        final byte [ ] bRead = service.getBlob( strKey );

        assertNotNull( "a legitimate blob must be retrievable", bRead );
        assertEquals( "legitimate content", new String( bRead, "UTF-8" ) );
    }
}
