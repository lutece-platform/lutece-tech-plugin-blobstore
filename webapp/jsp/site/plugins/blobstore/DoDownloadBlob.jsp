<jsp:useBean id="blobstore" scope="request" class="fr.paris.lutece.plugins.blobstore.web.BlobStoreJspBean" /><% 
	 String strResult =  blobstore.doDownloadBlob(request,response);
 	 if (!response.isCommitted())
	{
 		 out.write(strResult);
	}
%>