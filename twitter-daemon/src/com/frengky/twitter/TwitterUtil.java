package com.frengky.twitter;
import java.util.AbstractCollection;
import java.util.Iterator;

public class TwitterUtil {
	public static String join(AbstractCollection<String> s, String delimiter) {
	    if (s == null || s.isEmpty()) return "";
	    Iterator<String> iter = s.iterator();
	    StringBuilder builder = new StringBuilder(iter.next());
	    while( iter.hasNext() ) {
	      builder.append(delimiter).append(iter.next());
	    }
	    return builder.toString();
	 }		
}
