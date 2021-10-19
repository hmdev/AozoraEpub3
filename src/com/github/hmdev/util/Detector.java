package com.github.hmdev.util;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.universalchardet.UniversalDetector;

public class Detector {
	  public static String getCharset(InputStream is) throws IOException {
		    byte[] buf = new byte[4096];
		    // (1)
		    UniversalDetector detector = new UniversalDetector();

		    // (2)
		    int nread;
		    while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
		      detector.handleData(buf, 0, nread);
		    }
		    // (3)
		    detector.dataEnd();

		    // (4)
		    String encoding = detector.getDetectedCharset();
		    if (encoding != null) {
		      System.out.println("Detected encoding = " + encoding);
		    } else {
		      System.out.println("No encoding detected.");

		    }

		    // (5)
		    detector.reset();

		      return encoding;
		  }

	}
