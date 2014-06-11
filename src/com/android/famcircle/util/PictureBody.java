package com.android.famcircle.util;

import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.mime.content.AbstractContentBody;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class PictureBody extends AbstractContentBody
{
	String filename;
	byte[]  picBytes;

  public PictureBody(Bitmap bitmap,  CompressFormat format, String filename) {
	  super("application/octet-stream");
	  this.filename=filename;
	  ByteArrayOutputStream stream=new ByteArrayOutputStream();
	  bitmap.compress(format, 100, stream);
	  picBytes=stream.toByteArray();
  }


  @Deprecated
  public void writeTo(OutputStream out, int mode)
    throws IOException
  {
    writeTo(out);
  }

  public void writeTo(OutputStream out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }
    
    try {
//      byte[] tmp = new byte[4096];
//      int l;
//      while ((l = in.read(tmp)) != -1) {
//        out.write(tmp, 0, l);
//      }
    	out.write(picBytes);
      out.flush();
    } finally {
//    	in.close();
    }
  }

  public String getTransferEncoding() {
    return "binary";
  }


  public String getFilename() {
    return this.filename;
  }


	public String getCharset() {
		return "UTF-8";
	}

	public long getContentLength() {
		return picBytes.length;
	}


}