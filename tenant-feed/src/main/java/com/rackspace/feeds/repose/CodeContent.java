package com.rackspace.feeds.repose;

/**
 * Created by rona6028 on 6/4/14.
 */
public class CodeContent {

    private int statusCode;
    private String content;

    public CodeContent() { }

    public CodeContent( int s, String c ) {

        statusCode = s;
        content = c;
    }

    public String getContent() {
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode( int statusCode ) {
        this.statusCode = statusCode;
    }
}
