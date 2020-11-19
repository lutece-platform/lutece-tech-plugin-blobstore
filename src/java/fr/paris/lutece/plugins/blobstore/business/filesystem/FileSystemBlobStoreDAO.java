/*
 * Copyright (c) 2002-2020, City of Paris
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
package fr.paris.lutece.plugins.blobstore.business.filesystem;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uses filesystem to store blob. <i>Note that <code>strBasePath</code> is the path were blobs are put.</i>
 */
public class FileSystemBlobStoreDAO implements IFileSystemBlobStoreDAO
{
    /** The Constant WORD_SIZE. */
    private static final Integer WORD_SIZE = AppPropertiesService.getPropertyInt( "blobstore.folder.split.word_size", 3 );

    /**
     * Creates the dir.
     *
     * @param fileLevel1
     *            the file level1
     */
    private void createDir( final File fileLevel1 )
    {
        if ( !fileLevel1.exists( ) )
        {
            fileLevel1.mkdir( );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO #delete(java.lang.String, java.lang.String)
     */
    @Override
    public boolean delete( final String strKey, final String strBasePath, final Integer depth ) throws IOException
    {
        final File file = this.getPath( strKey, strBasePath, depth );

        boolean ret = file.delete( );

        // cleans up useless remaining folders.
        if ( depth.equals( 1 ) )
        {
            final File folderLevel1 = file.getParentFile( );

            if ( folderLevel1.list( ).length == 0 )
            {
                folderLevel1.delete( );
            }
        }
        else
            if ( depth.equals( 2 ) )
            {
                final File folderLevel2 = file.getParentFile( );

                if ( folderLevel2.list( ).length == 0 )
                {
                    folderLevel2.delete( );
                }

                File folderLevel1 = folderLevel2.getParentFile( );

                if ( folderLevel1.list( ).length == 0 )
                {
                    folderLevel1.delete( );
                }
            }

        return ret;
    }

    /**
     * Gets the path.
     *
     * @param blobstoeId
     *            the blobstoe id
     * @param strBasePath
     *            the str base path
     * @param depth
     * @return the path
     */
    private File getPath( final String blobstoeId, final String strBasePath, Integer depth )
    {
        final File ret;

        if ( depth.equals( 2 ) )
        {
            final String level1 = blobstoeId.substring( 0, WORD_SIZE );
            final String level2 = blobstoeId.substring( WORD_SIZE, WORD_SIZE * 2 );
            final File folderLevel1 = new File( strBasePath, level1 );
            createDir( folderLevel1 );

            final File folderLevel2 = new File( folderLevel1.getAbsolutePath( ), level2 );
            createDir( folderLevel2 );
            ret = new File( folderLevel2.getAbsolutePath( ), blobstoeId );
        }
        else
            if ( depth.equals( 1 ) )
            {
                final String level1 = blobstoeId.substring( 0, WORD_SIZE );
                final File folderLevel1 = new File( strBasePath, level1 );
                createDir( folderLevel1 );
                ret = new File( folderLevel1.getAbsolutePath( ), blobstoeId );
            }
            else
            {
                ret = new File( strBasePath, blobstoeId );
            }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO #insert(fr.paris.lutece.plugins.blobstore.business.BytesBlobStore,
     * java.lang.String)
     */
    @Override
    public void insert( final BytesBlobStore blobStore, final String strBasePath, final Integer depth ) throws IOException, FileAlreadyExistsException
    {
        final File file = getPath( blobStore.getId( ), strBasePath, depth );

        // create parents directories if they does not exist.
        file.getParentFile( ).mkdirs( );

        if ( file.exists( ) )
        {
            throw new FileAlreadyExistsException( "File " + blobStore.getId( ) + " already exists." );
        }

        FileUtils.writeByteArrayToFile( file, blobStore.getValue( ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO
     * #insert(fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore, java.lang.String)
     */
    @Override
    public void insert( final InputStreamBlobStore blobStore, final String strBasePath, final Integer depth ) throws FileAlreadyExistsException, IOException
    {
        final File file = this.getPath( blobStore.getId( ), strBasePath, depth );

        // create parents directories if they does not exist.
        file.getParentFile( ).mkdirs( );

        if ( file.exists( ) )
        {
            throw new FileAlreadyExistsException( "File " + blobStore.getId( ) + " already exists." );
        }

        final OutputStream out = new FileOutputStream( file );
        final InputStream in = blobStore.getInputStream( );

        try
        {
            IOUtils.copyLarge( in, out );
        }
        finally
        {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( in );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO #load(java.lang.String, java.lang.String)
     */
    @Override
    public BytesBlobStore load( final String strId, final String strBasePath, final Integer depth ) throws IOException
    {
        final File file = this.getPath( strId, strBasePath, depth );

        if ( !file.exists( ) )
        {
            return null;
        }

        final BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strId );
        blobStore.setValue( FileUtils.readFileToByteArray( file ) );

        return blobStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO #loadInputStream(java.lang.String, java.lang.String)
     */
    @Override
    public InputStream loadInputStream( final String strId, final String strBasePath, final Integer depth ) throws IOException
    {
        final File file = this.getPath( strId, strBasePath, depth );

        if ( !file.exists( ) )
        {
            return null;
        }

        return new FileInputStream( file );
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO #store(fr.paris.lutece.plugins.blobstore.business.BytesBlobStore,
     * java.lang.String)
     */
    @Override
    public void store( final BytesBlobStore blobStore, final String strBasePath, final Integer depth ) throws IOException
    {
        final File file = getPath( blobStore.getId( ), strBasePath, depth );
        FileUtils.writeByteArrayToFile( file, blobStore.getValue( ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreDAO #storeInputStream(fr.paris.lutece.plugins.blobstore.business.
     * InputStreamBlobStore, java.lang.String)
     */
    @Override
    public void storeInputStream( final InputStreamBlobStore blobStore, final String strBasePath, final Integer depth ) throws IOException
    {
        final File file = this.getPath( blobStore.getId( ), strBasePath, depth );
        final OutputStream out = new FileOutputStream( file );
        final InputStream in = blobStore.getInputStream( );

        try
        {
            IOUtils.copyLarge( blobStore.getInputStream( ), out );
        }
        finally
        {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( in );
        }
    }
}
