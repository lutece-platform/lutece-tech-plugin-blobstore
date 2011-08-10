/*
 * Copyright (c) 2002-2011, Mairie de Paris
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;

/**
 * Uses filesystem to store blob.
 * <i>Note that <code>strBasePath</code> is the path were blobs are put.</i>
 */
public class FileSystemBlobStoreDAO implements IFileSystemBlobStoreDAO 
{
	/**
     * {@inheritDoc}
     */
	public void insert( BytesBlobStore blobStore, String strBasePath ) throws IOException, FileAlreadyExistsException
	{
		File file = new File( strBasePath + blobStore.getId(  ) );
		if ( file.exists(  ) )
		{
			throw new FileAlreadyExistsException( "File " + blobStore.getId(  ) + " already exists." );
		}
		FileUtils.writeByteArrayToFile( file, blobStore.getValue(  ) );
	}
	
	/**
     * {@inheritDoc}
     */
	public void insert( InputStreamBlobStore blobStore, String strBasePath ) throws FileAlreadyExistsException, IOException
	{
		File file = new File( strBasePath + blobStore.getId(  ) );
		if ( file.exists(  ) )
		{
			throw new FileAlreadyExistsException( "File " + blobStore.getId(  ) + " already exists." );
		}
		
		OutputStream out = new FileOutputStream( file );
		InputStream in  = blobStore.getInputStream(  );
		
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
	
	/**
     * {@inheritDoc}
     */
    public BytesBlobStore load( String strId, String strBasePath ) throws IOException 
	{
    	File file = new File( strBasePath + strId );
    	
    	if ( !file.exists(  ) )
    	{
    		return null;
    	}
    	
    	BytesBlobStore blobStore = new BytesBlobStore(  );
    	blobStore.setId( strId );
    	blobStore.setValue( FileUtils.readFileToByteArray( file ) );
    	
    	return blobStore;
	}
    
    /**
     * {@inheritDoc}
     */
    public InputStream loadInputStream( String strId, String strBasePath ) throws IOException
    {
    	File file = new File( strBasePath + strId );
    	
    	if ( !file.exists(  ) )
    	{
    		return null;
    	}
    	
    	return new FileInputStream( file );
    }
	
    /**
     * {@inheritDoc}
     */
	public void store( BytesBlobStore blobStore, String strBasePath ) throws IOException
	{
		File file = new File( strBasePath + blobStore.getId(  ) );
		FileUtils.writeByteArrayToFile( file, blobStore.getValue(  ) );
	}
	
	/**
     * {@inheritDoc}
     */
	public void storeInputStream( InputStreamBlobStore blobStore, String strBasePath ) throws IOException
	{
		File file = new File( strBasePath + blobStore.getId(  ) );
		OutputStream out = new FileOutputStream( file );
		InputStream in  = blobStore.getInputStream(  );
		
		try
		{
			IOUtils.copyLarge( blobStore.getInputStream(  ), out );
		}
		finally
		{
			IOUtils.closeQuietly( out );
			IOUtils.closeQuietly( in );
		}
	}
	
	/**
     * {@inheritDoc}
     */
	public boolean delete( String strKey, String strBasePath ) throws IOException
	{
		File file = new File( strBasePath + strKey );
		return file.delete(  );
	}
}
