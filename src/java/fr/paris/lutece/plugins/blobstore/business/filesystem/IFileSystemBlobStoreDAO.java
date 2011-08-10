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

import java.io.IOException;
import java.io.InputStream;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;

/**
 * 
 * Uses filesystem to store blobs.
 *
 */
public interface IFileSystemBlobStoreDAO
{

	/**
	 * Inserts the blobStore
	 * @param blobStore blobStore
	 * @param strBasePath base directory
	 * @throws IOException ioexception
	 * @throws FileAlreadyExistsException if the file already exists
	 */
	void insert(BytesBlobStore blobStore, String strBasePath)
			throws IOException, FileAlreadyExistsException;

	/**
	 * Inserts the InputStreamDatabaseBlobStore
	 * @param blobStore blobStore
	 * @param strBasePath base directory
	 * @throws IOException ioexception
	 * @throws FileAlreadyExistsException if the file already exists
	 */
	void insert(InputStreamBlobStore blobStore,
			String strBasePath) throws FileAlreadyExistsException, IOException;

	/**
	 * Load the data from the table
	 * 
	 * @param strId
	 *            The identifier
	 * @param strBasePath
	 *            base directory
	 * @return the instance of the DatabaseBlobStore
	 * @throws IOException
	 *             ioexception
	 */
	BytesBlobStore load(String strId, String strBasePath)
			throws IOException;

	/**
	 * Loads the InputStreamDatabaseBlobStore
	 * 
	 * @param strId
	 *            id
	 * @param strBasePath
	 *            base path
	 * @return the InputStreamDatabaseBlobStore
	 * @throws IOException
	 *             ioexception
	 */
	InputStream loadInputStream(String strId,
			String strBasePath) throws IOException;

	/**
	 * Updates the file
	 * @param blobStore the blob
	 * @param strBasePath the base directory
	 * @throws IOException ioexception
	 */
	void store(BytesBlobStore blobStore, String strBasePath)
			throws IOException;

	/**
	 * Updates the file
	 * @param blobStore the blob
	 * @param strBasePath the base directory
	 * @throws IOException ioexception
	 */
	void storeInputStream(
			InputStreamBlobStore blobStore, String strBasePath)
			throws IOException;

	/**
	 * Removes the file
	 * @param strKey the key
	 * @param strBasePath the base directory
	 * @return <code>true</code> if the file is deleted, <code>false</code> otherwise
	 * @throws IOException ioexception
	 */
	boolean delete(String strKey, String strBasePath)
			throws IOException;

}