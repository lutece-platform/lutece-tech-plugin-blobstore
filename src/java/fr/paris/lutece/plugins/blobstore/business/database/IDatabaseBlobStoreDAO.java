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
package fr.paris.lutece.plugins.blobstore.business.database;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.portal.service.plugin.Plugin;

import java.io.InputStream;


/**
 *
 * IBlobStoreDAO
 *
 */
public interface IDatabaseBlobStoreDAO
{
    /**
     * Get the last primary key
     * @param plugin {@link Plugin}
     * @return The last primary key
     */
    String loadLastPrimaryKey( Plugin plugin );

    /**
     * Insert a new record in the table.
     * @param blobStore  instance of the object to insert
     * @param plugin {@link Plugin}
     */
    void insert( BytesBlobStore blobStore, Plugin plugin );

    /**
     * Load the data from the table
     * @param strId The identifier
     * @param plugin {@link Plugin}
     * @return the instance of the PhysicalFile
     */
    BytesBlobStore load( String strId, Plugin plugin );

    /**
     * Load the inputstream from the table
     * @param strId The identifier
     * @param plugin {@link Plugin}
     * @return the instance of the PhysicalFile
     */
    InputStream loadInputStream( String strId, Plugin plugin );

    /**
     * Delete a record from the table
     * @param strId The identifier
     * @param plugin {@link Plugin}
     */
    void delete( String strId, Plugin plugin );

    /**
     * Update the data in the table
     * @param blobStore instance of the object to update
     * @param plugin {@link Plugin}
     */
    void store( BytesBlobStore blobStore, Plugin plugin );

    /**
     * Insert the InputStreamDatabaseBlobStore
     * @param blobStore the InputStreamDatabaseBlobStore
     * @param plugin the {@link Plugin}
     */
    void insert( InputStreamBlobStore blobStore, Plugin plugin );

    /**
     * Update the data in the table
     * @param blobStore instance of the object to update
     * @param plugin {@link Plugin}
     */
    void store( InputStreamBlobStore blobStore, Plugin plugin );
}
