/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.io.IOException;
import java.io.InputStream;


/**
 *
 * FileSystemBlobStoreHome
 *
 */
public final class FileSystemBlobStoreHome
{
    private static IFileSystemBlobStoreDAO _dao = (IFileSystemBlobStoreDAO) SpringContextService.getBean( 
            "blobstore.fileSystemBlobStoreDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private FileSystemBlobStoreHome(  )
    {
    }

    /**
     * Creation of an instance of record physical file
     * @param blobStore The instance of the physical file which contains the informations to store
     * @param strBasePath base directory
     * @throws FileAlreadyExistsException already exists
     * @throws IOException ioe
     */
    public static void create( BytesBlobStore blobStore, String strBasePath )
        throws IOException, FileAlreadyExistsException
    {
        _dao.insert( blobStore, strBasePath );
    }

    /**
     * Update of physical file which is specified in parameter
     * @param  blobStore The instance of the  record physicalFile which contains the informations to update
     * @param strBasePath base directory
     * @throws IOException ioe
     */
    public static void update( BytesBlobStore blobStore, String strBasePath )
        throws IOException
    {
        _dao.store( blobStore, strBasePath );
    }

    /**
     * Update of physical file which is specified in parameter
     * @param  blobStore The instance of the  record physicalFile which contains the informations to update
     * @param strBasePath base directory
     * @throws IOException ioe
     */
    public static void updateInputStream( InputStreamBlobStore blobStore, String strBasePath )
        throws IOException
    {
        _dao.storeInputStream( blobStore, strBasePath );
    }

    /**
     * Delete the physical file whose identifier is specified in parameter
     * @param strKey The identifier of the record physical file
     * @param strBasePath base directory
     * @return <code>true</code> if the file is removed, <code>false</code> otherwise.
     * @throws IOException ioe
     */
    public static boolean remove( String strKey, String strBasePath )
        throws IOException
    {
        return _dao.delete( strKey, strBasePath );
    }

    /**
     * Returns an instance of a physical file whose identifier is specified in parameter
     *
     * @param strKey The file  primary key
     * @param strBasePath base directory
     * @return an instance of physical file
     * @throws IOException ioe
     */
    public static BytesBlobStore findByPrimaryKey( String strKey, String strBasePath )
        throws IOException
    {
        return _dao.load( strKey, strBasePath );
    }

    /**
     * Returns an instance of a physical file whose identifier is specified in parameter
     *
     * @param strKey The file  primary key
     * @param strBasePath base directory
     * @return an instance of physical file
     * @throws IOException ioe
     */
    public static InputStream findByPrimaryKeyInputStream( String strKey, String strBasePath )
        throws IOException
    {
        return _dao.loadInputStream( strKey, strBasePath );
    }

    /**
     * Creation of an instance of record physical file
     * @param blobStore The instance of the physical file which contains the inputstream to store
     * @param strBasePath base directory
     * @throws IOException  ioe
     * @throws FileAlreadyExistsException  already exists
     */
    public static void createInputStream( InputStreamBlobStore blobStore, String strBasePath )
        throws FileAlreadyExistsException, IOException
    {
        _dao.insert( blobStore, strBasePath );
    }
}
