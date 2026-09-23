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
package fr.paris.lutece.plugins.blobstore.business.filesystem;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * Uses filesystem to store blob. <i>Note that <code>strBasePath</code> is the path were blobs are put.</i>
 */
@ApplicationScoped
public class FileSystemBlobStoreDAO implements IFileSystemBlobStoreDAO
{
    /** The Constant WORD_SIZE. */
    private static final Integer WORD_SIZE = AppPropertiesService.getPropertyInt( "blobstore.folder.split.word_size", 3 );

    private static final Pattern PATTERN_KEY = Pattern.compile( "[A-Za-z0-9_-]+" );

    /**
     * Deletes a folder when it is empty.
     *
     * @param folder
     *            the folder
     */
    private static void deleteIfEmpty( final File folder )
    {
        final String [ ] children = folder.list( );

        if ( ( children != null ) && ( children.length == 0 ) )
        {
            folder.delete( );
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
            deleteIfEmpty( file.getParentFile( ) );
        }
        else
            if ( depth.equals( 2 ) )
            {
                final File folderLevel2 = file.getParentFile( );
                deleteIfEmpty( folderLevel2 );
                deleteIfEmpty( folderLevel2.getParentFile( ) );
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
     *            the depth
     * @return the path
     * @throws IOException
     *             when the key is not a blob key: characters other than letters, digits, '-' and '_', or too short for the depth
     */
    private File getPath( final String blobstoeId, final String strBasePath, Integer depth ) throws IOException
    {
        if ( ( blobstoeId == null ) || !PATTERN_KEY.matcher( blobstoeId ).matches( ) || ( blobstoeId.length( ) < ( WORD_SIZE * depth ) ) )
        {
            throw new IOException( "Invalid blob key" );
        }

        final File ret;

        if ( depth.equals( 2 ) )
        {
            final String level1 = blobstoeId.substring( 0, WORD_SIZE );
            final String level2 = blobstoeId.substring( WORD_SIZE, WORD_SIZE * 2 );
            ret = new File( new File( new File( strBasePath, level1 ), level2 ), blobstoeId );
        }
        else
            if ( depth.equals( 1 ) )
            {
                final String level1 = blobstoeId.substring( 0, WORD_SIZE );
                ret = new File( new File( strBasePath, level1 ), blobstoeId );
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
        file.getParentFile( ).mkdirs( );
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
