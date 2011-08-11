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
package fr.paris.lutece.plugins.blobstore.business.database;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.sql.DAOUtil;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;


/**
 *
 * BlobStoreDAO
 *
 */
public final class DatabaseBlobStoreDAO implements IDatabaseBlobStoreDAO
{
    // SQL QUERIES
    private static final String SQL_QUERY_SELECT_LAST_PRIMARY_KEY = " SELECT max( id_blob + 0 ) FROM blobstore_blobstore ";
    private static final String SQL_QUERY_FIND_BY_PRIMARY_KEY = " SELECT id_blob, blob_value FROM blobstore_blobstore WHERE id_blob = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO blobstore_blobstore( id_blob, blob_value ) VALUES( ?,? ) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM blobstore_blobstore WHERE id_blob = ? ";
    private static final String SQL_QUERY_UPDATE = " UPDATE  blobstore_blobstore SET blob_value = ? WHERE id_blob = ? ";

    /**
     * {@inheritDoc}
     */
    public String loadLastPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LAST_PRIMARY_KEY );
        daoUtil.executeQuery(  );

        String strKey = StringUtils.EMPTY;

        if ( daoUtil.next(  ) )
        {
            strKey = daoUtil.getString( 1 );
        }

        daoUtil.free(  );

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void insert( BytesBlobStore blobStore, Plugin plugin )
    {
        int nIndex = 1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT );
        daoUtil.setString( nIndex++, blobStore.getId(  ) );
        daoUtil.setBytes( nIndex++, blobStore.getValue(  ) );
        daoUtil.executeUpdate(  );

        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public BytesBlobStore load( String strId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY );
        daoUtil.setString( 1, strId );
        daoUtil.executeQuery(  );

        BytesBlobStore blobStore = null;

        if ( daoUtil.next(  ) )
        {
            int nIndex = 1;
            blobStore = new BytesBlobStore(  );
            blobStore.setId( daoUtil.getString( nIndex++ ) );
            blobStore.setValue( daoUtil.getBytes( nIndex++ ) );
        }

        daoUtil.free(  );

        return blobStore;
    }

    /**
     * {@inheritDoc}
     */
    public void delete( String strId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE );
        daoUtil.setString( 1, strId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public void store( BytesBlobStore blobStore, Plugin plugin )
    {
        int nIndex = 1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE );
        daoUtil.setBytes( nIndex++, blobStore.getValue(  ) );
        daoUtil.setString( nIndex++, blobStore.getId(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public void insert( InputStreamBlobStore blobStore, Plugin plugin )
    {
        int nIndex = 1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        try
        {
            daoUtil.setString( nIndex++, blobStore.getId(  ) );
            daoUtil.setBinaryStream( nIndex++, blobStore.getInputStream(  ), -1 );
            daoUtil.executeUpdate(  );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
            throw new AppException( e.getMessage(  ), e );
        }
        finally
        {
            daoUtil.free(  );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void store( InputStreamBlobStore blobStore, Plugin plugin )
    {
        int nIndex = 1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE );
        daoUtil.setBinaryStream( nIndex++, blobStore.getInputStream(  ), -1 );
        daoUtil.setString( nIndex++, blobStore.getId(  ) );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public InputStream loadInputStream( String strId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_BY_PRIMARY_KEY );
        daoUtil.setString( 1, strId );
        daoUtil.executeQuery(  );

        InputStream inputStream = null;

        if ( daoUtil.next(  ) )
        {
            inputStream = daoUtil.getBinaryStream( 2 );
        }

        daoUtil.free(  );

        return inputStream;
    }
}
