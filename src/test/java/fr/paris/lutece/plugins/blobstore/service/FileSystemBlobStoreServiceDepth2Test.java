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
import fr.paris.lutece.test.LuteceTestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;

/**
 * FileSystemBlobStoreServiceTest.
 */
public class FileSystemBlobStoreServiceDepth2Test extends LuteceTestCase
{
    /** The Constant DEPTH. */
    private static final int DEPTH = 2;

    /** The Constant FILE_NAME. */
    private static final String FILE_NAME = "testblob.txt";

    /**
     * Gets the base directory.
     *
     * @return the base directory
     */
    private String getBaseDirectory( )
    {
        return getResourcesDir( ) + "../test-classes/blobstore/";
    }

    /**
     * Gets the service.
     *
     * @param baseDirectory
     *            the base directory
     * @param depth
     *            the depth
     * @return the service
     */
    private FileSystemBlobStoreService getService( final String baseDirectory, final Integer depth )
    {
        FileSystemBlobStoreService service = new FileSystemBlobStoreService( );
        service.setBasePath( baseDirectory );
        service.setDepth( depth );

        return service;
    }

    /**
     * Test create.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void testCreate( ) throws IOException
    {
        clearBlobStore( );

        final FileSystemBlobStoreService service = getService( getBaseDirectory( ), DEPTH );

        final String strFilename = getResourcesDir( ) + "../test-classes/" + FILE_NAME;
        final InputStream is = new FileInputStream( strFilename );
        final byte [ ] bStore = IOUtils.toByteArray( is );
        IOUtils.closeQuietly( is );

        final String strKey = service.store( bStore );
        assertTrue( new File( getFilePath( strKey ) ).exists( ) );

        final InputStream is2 = new FileInputStream( getFilePath( strKey ) );
        final byte [ ] bRead = IOUtils.toByteArray( is2 );
        IOUtils.closeQuietly( is2 );

        assertTrue( Arrays.equals( bRead, bStore ) );
    }

    /**
     * Gets the file path.
     *
     * @param strKey
     *            the str key
     * @return the file path
     */
    private String getFilePath( final String strKey )
    {
        return getBaseDirectory( ) + strKey.substring( 0, 3 ) + File.separatorChar + strKey.substring( 3, 6 ) + File.separatorChar + strKey;
    }

    /**
     * Test update.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void testUpdate( ) throws IOException
    {
        clearBlobStore( );

        final FileSystemBlobStoreService service = getService( getBaseDirectory( ), DEPTH );

        final String strFilename = getResourcesDir( ) + "../test-classes/" + FILE_NAME;

        final InputStream is = new FileInputStream( strFilename );

        final byte [ ] bStore;

        try
        {
            bStore = IOUtils.toByteArray( is );
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }

        final String strKey = service.store( bStore );

        final byte [ ] bUpdate = "Updated bytes".getBytes( );
        service.update( strKey, bUpdate );

        final byte [ ] bRead;
        InputStream is2 = new FileInputStream( getFilePath( strKey ) );

        try
        {
            bRead = IOUtils.toByteArray( is2 );
        }
        finally
        {
            IOUtils.closeQuietly( is2 );
        }

        assertTrue( Arrays.equals( bRead, bUpdate ) );
    }

    /**
     * Test delete.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void testDelete( ) throws IOException
    {
        clearBlobStore( );

        final FileSystemBlobStoreService service = getService( getBaseDirectory( ), DEPTH );

        final String strFilename = getResourcesDir( ) + "../test-classes/" + FILE_NAME;
        final InputStream is = new FileInputStream( strFilename );
        final byte [ ] bStore;

        try
        {
            bStore = IOUtils.toByteArray( is );
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }

        final String strKey = service.store( bStore );
        service.delete( strKey );
        assertFalse( new File( getFilePath( strKey ) ).exists( ) );
    }

    /**
     * Clear blob store.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void clearBlobStore( ) throws IOException
    {
        final String baseDirectory = getBaseDirectory( );
        final File baseFile = new File( baseDirectory );

        if ( baseFile.exists( ) )
        {
            if ( baseFile.isDirectory( ) )
            {
                FileUtils.cleanDirectory( baseFile );
            }
            else
            {
                baseFile.delete( );
                baseFile.mkdir( );
            }
        }
        else
        {
            baseFile.mkdir( );
        }
    }
}
